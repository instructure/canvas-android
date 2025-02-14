/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.canvasapi2.utils.pageview

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import androidx.annotation.RequiresPermission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.zonedDateTime
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * A [JobService] which performs the task of uploading stored [PageViewEvent]s to pandata and performing any
 * associated cleanup. This service is designed to be used periodically (several times per day) and should only
 * be run while the device has a network connection.
 *
 * To test this service, launch the app on a device running Android 7.1+ and run a command in the following format:
 *     adb shell cmd jobscheduler run -f <app_package_name> <service_id>
 */
@AndroidEntryPoint
abstract class PageViewUploadService : JobService() {

    /**
     * An [AppKey][PandataInfo.AppKey] corresponding to the primary user role (student/teacher/parent) of the app in
     * which a concrete implementation of this [PageViewUploadService] is declared.
     */
    abstract val appKey: PandataInfo.AppKey
    /**
     * Called if an exception occurs while uploading events. It is recommended to log these exceptions in a
     * highly visible location (e.g. Crashlytics).
     */
    abstract fun onException(e: Throwable)

    private var uploadJob: WeaveJob? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        Logger.d("PageViewUploadService: Service Started")
        // Skip if logged out and pandata token is invalid (i.e. no way to upload data)
        if (!ApiPrefs.getValidToken().isValid() && ApiPrefs.pandataInfo?.isValid != true) return false

        uploadJob = tryWeave {
            val book = PageViewUtils.book

            // Refresh pandata token if null or expired
            if (ApiPrefs.pandataInfo?.isValid != true) {
                ApiPrefs.pandataInfo = awaitApi<PandataInfo> { PandataManager.getToken(appKey, it) }
            }

            // Grab completed events (i.e. have a duration) and events which have passed the max orphan age
            val (localEvents, oldEvents) = inBackground {
                val orphanDate = Date(System.currentTimeMillis() - MAX_ORPHAN_AGE)
                val maxAgeDate = Date(System.currentTimeMillis() - MAX_EVENT_AGE)
                book.allKeys.asSequence()
                    .mapNotNull {
                        try {
                            book.read<PageViewEvent>(it)
                        } catch (e: Exception) {
                            // This is a known issue in PaperDB that some deserializaton randomly fails due to Kyro
                            // https://github.com/pilgr/Paper/issues/4#issuecomment-740185776
                            // Suggested workaround is to try reading the object again
                            // They won't fix it because the library is not maintained anymore. Long term fix is to migrate to Room.
                            onException(Throwable("Failed to read PageViewEvent for the first try", e))
                            book.read<PageViewEvent>(it)
                        }
                    }
                    .filter { it.eventDuration > 0 || it.timestamp.before(orphanDate) }
                    .partition { it.timestamp.after(maxAgeDate) }
            }

            // Skip and delete events that are too old
            if (oldEvents.isNotEmpty()) {
                Logger.d("PageViewUploadService: Skipping/deleting ${oldEvents.size} old events")
                inBackground { localEvents.forEach { book.delete(it.key) } }
            }

            // Group events by upload url and map to the upload model
            val uploadGroups = localEvents.groupBy({ it.postUrl }) {
                val properties = mutableMapOf(
                    "app_name" to appKey.appName,
                    "page_name" to it.eventName,
                    "url" to it.url,
                    "interaction_seconds" to it.eventDuration,
                    "domain" to it.domain,
                    "session_id" to it.sessionId,
                    "context_type" to it.contextType,
                    "context_id" to it.contextId,
                    "real_user_id" to it.realUserId?.toString(),
                    "agent" to ApiPrefs.userAgent,
                    "guid" to it.key
                )
                PageViewUpload(
                    timestamp = dateFormat.format(it.timestamp.zonedDateTime),
                    eventType = "page_view",
                    appTag = appKey.key,
                    properties = properties,
                    signedProperties = it.signedProperties
                )
            }

            // Perform upload
            val token = ApiPrefs.pandataInfo!!
            if (uploadGroups.isNotEmpty()) {
                for ((url, events) in uploadGroups) {
                    Logger.d("PageViewUploadService: Uploading ${events.size} events")
                    awaitApi<Void> {
                        PandataManager.uploadPageViewEvents(url, token.authToken, PageViewUploadList(events), it)
                    }
                    Logger.d("PageViewUploadService: ${events.size} events uploaded. Performing cleanup...")
                    // Clear uploaded events
                    inBackground { localEvents.forEach { book.delete(it.key) } }
                }
            } else {
                Logger.d("PageViewUploadService: No events to upload")
            }

            Logger.d("PageViewUploadService: Service finished successfully")
            jobFinished(params, false)
        } catch {
            Logger.e("PageViewUploadService: Upload job failed!")
            it.printStackTrace()
            onException(it)
            jobFinished(params, false)
        }

        // Return true to indicate we have ongoing tasks
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        uploadJob?.cancel()
        return false
    }

    companion object {

        /** Date format including milliseconds */
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)

        /** Unique ID for the PageView upload job */
        private const val PAGE_VIEW_UPLOAD_JOB_ID = 188372

        /**
         * Events which have a timestamp but no duration are either currently ongoing or have somehow been orphaned
         * (e.g. due to an app crash, nav stack bug, etc). To avoid uploading ongoing events, we'll use an age
         * threshold of 30 minutes - a reasonable amount of time for most events to be completed.
         */
        private val MAX_ORPHAN_AGE = TimeUnit.MINUTES.toMillis(30)

        /** Don't upload events if they are more than 7 days old */
        private val MAX_EVENT_AGE = TimeUnit.DAYS.toMillis(7)

        /** The time interval at which the job should be repeated */
        private val REPEAT_INTERVAL = TimeUnit.HOURS.toMillis(6)

        /** The flexible time window in which the job may run at the end of the repeat interval (API 24+ only) */
        private val REPEAT_FLEX = TimeUnit.HOURS.toMillis(2)

        /**
         * Schedules the specified PageView upload service to run periodically via [JobScheduler]. Results in a no-op
         * if the job has already been scheduled. Requires the RECEIVE_BOOT_COMPLETED permission.
         */
        @RequiresPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED)
        fun <T : PageViewUploadService> schedule(context: Context, clazz: Class<T>) {

            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            // Return if job is already scheduled
            if (scheduler.allPendingJobs.any { it.id == PAGE_VIEW_UPLOAD_JOB_ID }) return

            val jobInfo = JobInfo.Builder(PAGE_VIEW_UPLOAD_JOB_ID, ComponentName(context, clazz)).apply {

                setPersisted(true)


                    setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    setPeriodic(TimeUnit.MINUTES.toMillis(2))

            }.build()

            scheduler.schedule(jobInfo)
        }

    }

}

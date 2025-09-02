/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.analytics.pageview

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageViewUpload
import com.instructure.canvasapi2.utils.pageview.PageViewUploadList
import com.instructure.canvasapi2.utils.pageview.PandataApi
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.canvasapi2.utils.zonedDateTime
import com.instructure.pandautils.analytics.pageview.db.PageViewDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.threeten.bp.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class PageViewUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val appKey: PandataInfo.AppKey,
    private val pageViewDao: PageViewDao,
    private val apiPrefs: ApiPrefs,
    private val pandataApi: PandataApi.PandataInterface
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            if (!ApiPrefs.getValidToken()
                    .isValid() && ApiPrefs.pandataInfo?.isValid != true
            ) return Result.failure()

            if (apiPrefs.pandataInfo?.isValid != true) {
                apiPrefs.pandataInfo =
                    pandataApi.getPandataToken(appKey.key, RestParams(shouldLoginOnTokenError = false)).dataOrThrow
            }

            val orphanDate = Date(System.currentTimeMillis() - MAX_ORPHAN_AGE)
            val maxAgeDate = Date(System.currentTimeMillis() - MAX_EVENT_AGE)

            val (localEvents, oldEvents) = pageViewDao.getAllPageViewEventsForUser(ApiPrefs.user?.id ?: 0)
                .filter { it.eventDuration > 0 || it.timestamp.before(orphanDate) }
                .partition { it.timestamp.after(maxAgeDate) }

            if (oldEvents.isNotEmpty()) {
                Logger.d("PageViewUploadService: Skipping/deleting ${oldEvents.size} old events")
                pageViewDao.delete(oldEvents)
            }

            if (localEvents.isEmpty()) return Result.success()

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

            apiPrefs.pandataInfo?.let {
                uploadGroups.forEach { (url, event) ->
                    Logger.d("PageViewUploadService: Uploading ${event.size} events")
                    pandataApi.uploadPageViewEvents(
                        url,
                        it.authToken,
                        PageViewUploadList(event),
                        RestParams(shouldIgnoreToken = true, shouldLoginOnTokenError = false)
                    ).dataOrThrow
                    Logger.d("PageViewUploadService: ${event.size} events uploaded. Performing cleanup...")
                }
            }
            pageViewDao.delete(localEvents)
            Logger.d("PageViewUploadService: Service finished successfully")
            Result.success()
        } catch (e: Exception) {
            Logger.e("PageViewUploadService: Upload job failed!")
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        private val MAX_ORPHAN_AGE = TimeUnit.MINUTES.toMillis(30)
        private val MAX_EVENT_AGE = TimeUnit.DAYS.toMillis(7)
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
    }
}
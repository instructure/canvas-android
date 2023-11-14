/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.offline.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.R
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.daos.EditDashboardItemDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.DashboardCardEntity
import com.instructure.pandautils.room.offline.entities.EditDashboardItemEntity
import com.instructure.pandautils.room.offline.entities.EnrollmentState
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import kotlin.random.Random

const val COURSE_IDS = "course-ids"

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val featureFlagProvider: FeatureFlagProvider,
    private val courseApi: CourseAPI.CoursesInterface,
    private val dashboardCardDao: DashboardCardDao,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val editDashboardItemDao: EditDashboardItemDao,
    private val courseDao: CourseDao,
    private val courseSyncProgressDao: CourseSyncProgressDao,
    private val fileSyncProgressDao: FileSyncProgressDao,
    private val apiPrefs: ApiPrefs,
    private val fileFolderDao: FileFolderDao,
    private val localFileDao: LocalFileDao,
    private val syncRouter: SyncRouter,
    private val courseSync: CourseSync
) : CoroutineWorker(context, workerParameters) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = Random.nextInt()

    override suspend fun doWork(): Result {
        courseSyncProgressDao.deleteAll()
        fileSyncProgressDao.deleteAll()

        if (!featureFlagProvider.offlineEnabled() || apiPrefs.user == null) return Result.success()

        var dashboardCards =
            courseApi.getDashboardCourses(RestParams(isForceReadFromNetwork = true)).dataOrNull.orEmpty()
        if (dashboardCards.all { it.position == Int.MAX_VALUE }) {
            dashboardCards = dashboardCards.mapIndexed { index, dashboardCard -> dashboardCard.copy(position = index) }
        }
        dashboardCardDao.updateEntities(dashboardCards.map { DashboardCardEntity(it) })

        val params = RestParams(isForceReadFromNetwork = true, usePerPageQueryParam = true)
        val currentCourses = courseApi.firstPageCoursesByEnrollmentState("active", params)
            .depaginate { nextUrl -> courseApi.next(nextUrl, params) }.dataOrNull.orEmpty()
        val pastCourses = courseApi.firstPageCoursesByEnrollmentState("completed", params)
            .depaginate { nextUrl -> courseApi.next(nextUrl, params) }.dataOrNull.orEmpty()
        val futureCourses =
            courseApi.firstPageCoursesByEnrollmentState("invited_or_pending", params)
                .depaginate { nextUrl -> courseApi.next(nextUrl, params) }.dataOrNull.orEmpty()
                .filter { it.workflowState != Course.WorkflowState.UNPUBLISHED }

        val allCourses = currentCourses.mapIndexed { index, course ->
            EditDashboardItemEntity(
                course,
                EnrollmentState.CURRENT,
                index
            )
        } +
                pastCourses.mapIndexed { index, course ->
                    EditDashboardItemEntity(
                        course,
                        EnrollmentState.PAST,
                        index
                    )
                } +
                futureCourses.mapIndexed { index, course ->
                    EditDashboardItemEntity(
                        course,
                        EnrollmentState.FUTURE,
                        index
                    )
                }
        editDashboardItemDao.updateEntities(allCourses)

        val courseIds = inputData.getLongArray(COURSE_IDS)?.toSet()
        val courses = courseSyncSettingsDao.findAll()

        var (coursesToSync, coursesToDelete) = courses.partition { it.anySyncEnabled }

        val courseIdsToRemove = coursesToDelete.map { it.courseId }
        courseDao.deleteByIds(courseIdsToRemove)
        courseIdsToRemove.forEach {
            cleanupFiles(it)
        }

        coursesToSync = coursesToSync.filter { courseIds?.contains(it.courseId) ?: true }

        coursesToSync.map { CourseSyncProgressEntity(it.courseId, it.courseName) }.let {
            courseSyncProgressDao.insertAll(it)
        }

        courseSync.syncCourses(coursesToSync.map { it.courseId })

        val courseProgresses = courseSyncProgressDao.findAll()
        val fileProgresses = fileSyncProgressDao.findAll()

        if (courseProgresses.isNotEmpty() && fileProgresses.isNotEmpty()) {
            showNotification(
                courseProgresses.size,
                courseProgresses.all { it.progressState == ProgressState.COMPLETED }
                        && fileProgresses.all { it.progressState == ProgressState.COMPLETED })
        }

        return Result.success()
    }

    private fun showNotification(itemCount: Int, success: Boolean) {
        registerNotificationChannel(context)

        val pendingIntent = syncRouter.routeToSyncProgress(context)

        val title: String
        val subtitle: String
        if (success) {
            title = context.getString(R.string.offlineContentSyncSuccessNotificationTitle)
            subtitle = context.resources.getQuantityString(
                R.plurals.offlineContentSyncSuccessNotificationBody,
                itemCount,
                itemCount
            )
        } else {
            title = context.getString(R.string.offlineContentSyncFailureNotificationTitle)
            subtitle = context.getString(R.string.syncProgress_syncErrorSubtitle)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun registerNotificationChannel(context: Context) {
        if (notificationManager.notificationChannels.any { it.id == CHANNEL_ID }) return

        val name = context.getString(R.string.notificationChannelNameSyncUpdates)
        val description = context.getString(R.string.notificationChannelNameSyncUpdatesDescription)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun cleanupFiles(courseId: Long) {
        val file = File(context.filesDir, "${apiPrefs.user?.id.toString()}/external_$courseId")
        file.deleteRecursively()

        fileFolderDao.deleteAllByCourseId(courseId)
        localFileDao.findRemovedFiles(courseId, emptyList()).forEach { localFile ->
            File(localFile.path).delete()
            localFileDao.delete(localFile)
        }
    }

    companion object {
        const val CHANNEL_ID = "syncChannel"
        const val PERIODIC_TAG = "OfflineSyncWorkerPeriodic"
        const val ONE_TIME_TAG = "OfflineSyncWorkerOneTime"
    }
}
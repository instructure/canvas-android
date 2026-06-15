/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.offline.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.R
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@HiltWorker
class HorizonOfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val featureFlagProvider: FeatureFlagProvider,
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val horizonCourseSync: HorizonCourseSync,
    private val aggregateProgressObserver: HorizonAggregateProgressObserver,
    private val syncRouter: HorizonSyncRouter,
    private val apiPrefs: ApiPrefs,
) : CoroutineWorker(context, workerParameters) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val userIdTag = apiPrefs.user?.id ?: 0L
    private val foregroundNotificationId = "horizon_offline_sync_foreground_$userIdTag".hashCode()
    private val completionNotificationId = "horizon_offline_sync_completion_$userIdTag".hashCode()

    override suspend fun doWork(): Result {
        if (!featureFlagProvider.offlineEnabled()) return Result.success()

        val enqueuedUserId = inputData.getLong(USER_ID_KEY, -1L)
        val currentUserId = apiPrefs.user?.id ?: -1L
        if (enqueuedUserId != -1L && currentUserId != -1L && enqueuedUserId != currentUserId) {
            // User switched (or masquerade entered/exited) after enqueue — abort to avoid writing
            // into the wrong user's database.
            return Result.success()
        }

        val inputCourseIds = inputData.getLongArray(COURSE_IDS_KEY)?.toList()

        val plans = if (inputCourseIds != null) {
            inputCourseIds.mapNotNull { courseSyncPlanDao.findByCourseId(it) }
        } else {
            courseSyncPlanDao.findAll()
        }

        if (plans.isEmpty()) return Result.success()

        return try {
            setForeground(createForegroundInfo(0))

            coroutineScope {
                val observer = launch {
                    aggregateProgressObserver.progressData.collect { data ->
                        if (isStopped) return@collect
                        if (data.isActive) {
                            try {
                                setForeground(createForegroundInfo((data.progress * 100).toInt()))
                            } catch (_: IllegalStateException) {
                                // Foreground service may have been torn down after cancellation
                            }
                        }
                    }
                }

                horizonCourseSync.syncCourses(plans)
                observer.cancel()
            }

            if (!isStopped) {
                showCompletionNotification(plans.size)
            }
            val allFailed = plans.all { plan ->
                courseSyncPlanDao.findByCourseId(plan.courseId)?.state == HorizonProgressState.ERROR
            }
            if (!isStopped && allFailed && runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.success()
            }
        } finally {
            notificationManager.cancel(foregroundNotificationId)
        }
    }

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        createNotificationChannel()

        val pendingIntent = syncRouter.routeToSyncProgress(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.sync)
            .setContentTitle(context.getString(R.string.offline_syncNotificationTitle))
            .setProgress(100, progress, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(foregroundNotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(foregroundNotificationId, notification)
        }
    }

    private fun showCompletionNotification(courseCount: Int) {
        if (courseCount == 0) return
        createNotificationChannel()

        val state = aggregateProgressObserver.progressData.value
        val isSuccess = state.progressState == HorizonProgressState.COMPLETED

        val title = if (isSuccess) {
            context.getString(R.string.offline_syncNotificationComplete)
        } else {
            context.getString(R.string.offline_syncNotificationFailed)
        }

        val subtitle = context.resources.getQuantityString(R.plurals.offline_syncNotificationCoursesCount, courseCount, courseCount)

        val pendingIntent = syncRouter.routeToSyncProgress(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.sync)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(completionNotificationId, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.offline_syncNotificationChannel),
            NotificationManager.IMPORTANCE_LOW,
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "horizonSyncChannel"
        const val PERIODIC_TAG = "HorizonOfflineSyncPeriodic"
        const val ONE_TIME_TAG = "HorizonOfflineSyncOneTime"
        const val COURSE_IDS_KEY = "courseIds"
        const val USER_ID_KEY = "userId"
        private const val MAX_RETRY_ATTEMPTS = 3
    }
}

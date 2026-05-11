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
import com.instructure.horizon.R
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.dao.HorizonGlobalSyncPlanDao
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltWorker
class HorizonOfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val featureFlagProvider: FeatureFlagProvider,
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val globalSyncPlanDao: HorizonGlobalSyncPlanDao,
    private val horizonCourseSync: HorizonCourseSync,
    private val aggregateProgressObserver: HorizonAggregateProgressObserver,
    private val syncRouter: HorizonSyncRouter,
) : CoroutineWorker(context, workerParameters) {

    private var observerJob: Job? = null

    override suspend fun doWork(): Result {
        if (!featureFlagProvider.offlineEnabled()) return Result.success()

        val inputCourseIds = inputData.getLongArray(COURSE_IDS_KEY)?.toList()

        val plans = if (inputCourseIds != null) {
            inputCourseIds.mapNotNull { courseSyncPlanDao.findByCourseId(it) }
        } else {
            courseSyncPlanDao.findAll()
        }

        val syncLearningLibrary = globalSyncPlanDao.getPlanOnce()?.syncLearningLibrary != false
        if (plans.isEmpty() && !syncLearningLibrary) return Result.success()

        setForeground(createForegroundInfo(0))
        observeProgress()

        horizonCourseSync.isStopped = false
        horizonCourseSync.syncCourses(plans)

        observerJob?.cancel()
        showCompletionNotification(plans.size)
        return Result.success()
    }

    private fun observeProgress() {
        observerJob = CoroutineScope(Dispatchers.Default).launch {
            aggregateProgressObserver.progressData.collect { data ->
                if (isStopped) {
                    horizonCourseSync.isStopped = true
                    observerJob?.cancel()
                    return@collect
                }
                if (data.isActive) {
                    setForeground(createForegroundInfo((data.progress * 100).toInt()))
                }
            }
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
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun showCompletionNotification(courseCount: Int) {
        createNotificationChannel()

        val allPlans = aggregateProgressObserver.progressData.value
        val isSuccess = allPlans.progressState == HorizonProgressState.COMPLETED

        val title = if (isSuccess) {
            context.getString(R.string.offline_syncNotificationComplete)
        } else {
            context.getString(R.string.offline_syncNotificationFailed)
        }

        val subtitle = context.getString(R.string.offline_syncNotificationCoursesCount, courseCount)

        val pendingIntent = syncRouter.routeToSyncProgress(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.sync)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.offline_syncNotificationChannel),
            NotificationManager.IMPORTANCE_LOW,
        )
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "horizonSyncChannel"
        const val NOTIFICATION_ID = 87654
        const val PERIODIC_TAG = "HorizonOfflineSyncPeriodic"
        const val ONE_TIME_TAG = "HorizonOfflineSyncOneTime"
        const val COURSE_IDS_KEY = "courseIds"
    }
}

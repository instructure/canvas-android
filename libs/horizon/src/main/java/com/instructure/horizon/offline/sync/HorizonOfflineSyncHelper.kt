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

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.horizon.database.dao.HorizonSyncSettingsDao
import com.instructure.horizon.database.entity.HorizonSyncSettingsEntity
import com.instructure.horizon.features.account.offlinesettings.SyncFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HorizonOfflineSyncHelper @Inject constructor(
    private val workManager: WorkManager,
    private val syncSettingsDao: HorizonSyncSettingsDao,
) {
    suspend fun syncCourses(courseIds: List<Long>) {
        val settings = syncSettingsDao.getSettingsOnce() ?: HorizonSyncSettingsEntity()
        when {
            isPeriodicWorkRunning() -> {
                schedulePeriodicWork(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
            }
            isPeriodicWorkScheduled() || !settings.autoSyncEnabled -> {
                cancelRunningOneTimeWork()
                syncOnce(courseIds, settings)
            }
            else -> {
                schedulePeriodicWork()
            }
        }
    }

    suspend fun schedulePeriodicSync() {
        val settings = syncSettingsDao.getSettingsOnce() ?: HorizonSyncSettingsEntity()
        if (!settings.autoSyncEnabled) {
            cancelPeriodicSync()
            return
        }
        schedulePeriodicWork()
    }

    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
    }

    suspend fun cancelRunningWorkers() {
        if (isPeriodicWorkRunning()) {
            schedulePeriodicWork(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, addDelay = true)
        }
        cancelRunningOneTimeWork()
    }

    fun isRunning(): Flow<Boolean> {
        return workManager.getWorkInfosByTagFlow(HorizonOfflineSyncWorker.ONE_TIME_TAG)
            .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING } }
    }

    private suspend fun syncOnce(courseIds: List<Long>, settings: HorizonSyncSettingsEntity) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putLongArray(HorizonOfflineSyncWorker.COURSE_IDS_KEY, courseIds.toLongArray())
            .build()

        val request = OneTimeWorkRequestBuilder<HorizonOfflineSyncWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(HorizonOfflineSyncWorker.ONE_TIME_TAG)
            .build()

        workManager.enqueue(request)
    }

    private suspend fun schedulePeriodicWork(
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
        addDelay: Boolean = false,
    ) {
        val settings = syncSettingsDao.getSettingsOnce() ?: HorizonSyncSettingsEntity()
        val frequencyDays = when (settings.syncFrequency) {
            SyncFrequency.DAILY -> 1L
            SyncFrequency.WEEKLY -> 7L
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val builder = PeriodicWorkRequestBuilder<HorizonOfflineSyncWorker>(frequencyDays, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag(HorizonOfflineSyncWorker.PERIODIC_TAG)

        if (addDelay) {
            builder.setInitialDelay(frequencyDays, TimeUnit.DAYS)
        }

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            policy,
            builder.build(),
        )
    }

    private suspend fun isPeriodicWorkScheduled(): Boolean {
        return workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_PERIODIC_WORK_NAME).first()
            .any { it.state != WorkInfo.State.CANCELLED }
    }

    private suspend fun isPeriodicWorkRunning(): Boolean {
        return workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_PERIODIC_WORK_NAME).first()
            .any { it.state == WorkInfo.State.RUNNING }
    }

    private suspend fun cancelRunningOneTimeWork() {
        val runningWork = workManager.getWorkInfosByTagFlow(HorizonOfflineSyncWorker.ONE_TIME_TAG).first()
            .firstOrNull { it.state == WorkInfo.State.RUNNING }
        if (runningWork != null) {
            workManager.cancelWorkById(runningWork.id)
        }
    }

    companion object {
        private const val UNIQUE_PERIODIC_WORK_NAME = "HorizonOfflinePeriodicSync"
    }
}

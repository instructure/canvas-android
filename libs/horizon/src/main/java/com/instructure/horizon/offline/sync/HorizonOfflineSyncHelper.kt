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
import com.instructure.horizon.features.account.offlinesettings.SyncFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HorizonOfflineSyncHelper @Inject constructor(
    private val workManager: WorkManager,
    private val syncSettingsDao: HorizonSyncSettingsDao,
) {
    fun syncCourses(courseIds: List<Long>) {
        val inputData = Data.Builder()
            .putLongArray(HorizonOfflineSyncWorker.COURSE_IDS_KEY, courseIds.toLongArray())
            .build()

        val request = OneTimeWorkRequestBuilder<HorizonOfflineSyncWorker>()
            .setInputData(inputData)
            .addTag(HorizonOfflineSyncWorker.ONE_TIME_TAG)
            .build()

        workManager.enqueue(request)
    }

    suspend fun schedulePeriodicSync() {
        val settings = syncSettingsDao.getSettingsOnce() ?: return
        if (!settings.autoSyncEnabled) {
            cancelPeriodicSync()
            return
        }

        val repeatInterval = when (settings.syncFrequency) {
            SyncFrequency.DAILY -> 1L to TimeUnit.DAYS
            SyncFrequency.WEEKLY -> 7L to TimeUnit.DAYS
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<HorizonOfflineSyncWorker>(
            repeatInterval.first, repeatInterval.second,
        )
            .setConstraints(constraints)
            .addTag(HorizonOfflineSyncWorker.PERIODIC_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            HorizonOfflineSyncWorker.PERIODIC_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(HorizonOfflineSyncWorker.PERIODIC_TAG)
    }

    fun cancelRunningWorkers() {
        workManager.cancelAllWorkByTag(HorizonOfflineSyncWorker.ONE_TIME_TAG)
    }

    fun isRunning(): Flow<Boolean> {
        return workManager.getWorkInfosByTagFlow(HorizonOfflineSyncWorker.ONE_TIME_TAG)
            .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING } }
    }
}

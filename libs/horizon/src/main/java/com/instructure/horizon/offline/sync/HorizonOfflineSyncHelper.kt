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

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.dao.HorizonSyncSettingsDao
import com.instructure.horizon.database.entity.HorizonSyncSettingsEntity
import com.instructure.horizon.features.account.offlinesettings.SyncFrequency
import com.instructure.pandautils.utils.FeatureFlagProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HorizonOfflineSyncHelper @Inject constructor(
    private val workManager: WorkManager,
    private val syncSettingsDao: HorizonSyncSettingsDao,
    private val apiPrefs: ApiPrefs,
    private val featureFlagProvider: FeatureFlagProvider,
) {

    suspend fun syncCoursesNow(courseIds: List<Long>) {
        if (!featureFlagProvider.offlineEnabled()) return
        val settings = settingsOrDefault()
        workManager.enqueueUniqueWork(
            uniqueOneTimeName(),
            ExistingWorkPolicy.REPLACE,
            buildOneTimeRequest(courseIds, settings),
        )
    }

    suspend fun applyPeriodicSchedule() {
        if (!featureFlagProvider.offlineEnabled()) {
            workManager.cancelUniqueWork(uniquePeriodicName())
            return
        }
        val settings = settingsOrDefault()
        if (!settings.autoSyncEnabled) {
            workManager.cancelUniqueWork(uniquePeriodicName())
            return
        }
        workManager.enqueueUniquePeriodicWork(
            uniquePeriodicName(),
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            buildPeriodicRequest(settings),
        )
    }

    suspend fun cancelCurrentSync() {
        workManager.cancelAllWorkByTag(oneTimeTag())
        val periodicInfos = workManager.getWorkInfosForUniqueWorkFlow(uniquePeriodicName()).first()
        periodicInfos
            .filter { it.state == WorkInfo.State.RUNNING }
            .forEach { workManager.cancelWorkById(it.id) }
    }

    suspend fun fullStop() {
        workManager.cancelAllWorkByTag(oneTimeTag())
        workManager.cancelUniqueWork(uniquePeriodicName())
        awaitWorkersDrained()
    }

    private suspend fun awaitWorkersDrained() {
        for (tag in listOf(oneTimeTag(), periodicTag())) {
            workManager.getWorkInfosByTagFlow(tag)
                .map { infos -> infos.all { it.state.isFinished } }
                .first { it }
        }
    }

    fun isRunning(): Flow<Boolean> {
        return workManager.getWorkInfosByTagFlow(oneTimeTag())
            .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING } }
    }

    private fun userIdSuffix() = apiPrefs.user?.id?.toString() ?: "anon"
    private fun uniqueOneTimeName() = "HorizonOfflineSyncOneTime-${userIdSuffix()}"
    private fun uniquePeriodicName() = "HorizonOfflinePeriodicSync-${userIdSuffix()}"
    private fun oneTimeTag() = "${HorizonOfflineSyncWorker.ONE_TIME_TAG}-${userIdSuffix()}"
    private fun periodicTag() = "${HorizonOfflineSyncWorker.PERIODIC_TAG}-${userIdSuffix()}"

    private suspend fun settingsOrDefault(): HorizonSyncSettingsEntity {
        return syncSettingsDao.getSettingsOnce() ?: HorizonSyncSettingsEntity()
    }

    private fun buildOneTimeRequest(courseIds: List<Long>, settings: HorizonSyncSettingsEntity) =
        OneTimeWorkRequestBuilder<HorizonOfflineSyncWorker>()
            .setInputData(
                Data.Builder()
                    .putLongArray(HorizonOfflineSyncWorker.COURSE_IDS_KEY, courseIds.toLongArray())
                    .putLong(HorizonOfflineSyncWorker.USER_ID_KEY, apiPrefs.user?.id ?: -1L)
                    .build()
            )
            .setConstraints(networkConstraints(settings))
            .addTag(HorizonOfflineSyncWorker.ONE_TIME_TAG)
            .addTag(oneTimeTag())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

    private fun buildPeriodicRequest(settings: HorizonSyncSettingsEntity) =
        PeriodicWorkRequestBuilder<HorizonOfflineSyncWorker>(periodDays(settings), TimeUnit.DAYS)
            .setInputData(
                Data.Builder()
                    .putLong(HorizonOfflineSyncWorker.USER_ID_KEY, apiPrefs.user?.id ?: -1L)
                    .build()
            )
            .setConstraints(networkConstraintsForPeriodic(settings))
            .addTag(HorizonOfflineSyncWorker.PERIODIC_TAG)
            .addTag(periodicTag())
            .build()

    private fun networkConstraints(settings: HorizonSyncSettingsEntity) = Constraints.Builder()
        .setRequiredNetworkType(if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
        .setRequiresStorageNotLow(true)
        .build()

    private fun networkConstraintsForPeriodic(settings: HorizonSyncSettingsEntity) = Constraints.Builder()
        .setRequiredNetworkType(if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()

    private fun periodDays(settings: HorizonSyncSettingsEntity): Long = when (settings.syncFrequency) {
        SyncFrequency.DAILY -> 1L
        SyncFrequency.WEEKLY -> 7L
    }

}

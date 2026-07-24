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
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonSyncSettingsDao
import com.instructure.horizon.database.entity.HorizonSyncSettingsEntity
import com.instructure.horizon.features.account.offlinesettings.SyncFrequency
import com.instructure.pandautils.utils.FeatureFlagProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HorizonOfflineSyncHelper @Inject constructor(
    private val workManager: WorkManager,
    private val syncSettingsDao: HorizonSyncSettingsDao,
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val apiPrefs: ApiPrefs,
    private val featureFlagProvider: FeatureFlagProvider,
) {

    private var lastAppliedFingerprint: String? = null

    suspend fun requestOneTimeSync() {
        val courseIds = courseSyncPlanDao.findAll().map { it.courseId }
        syncCoursesNow(courseIds)
    }

    suspend fun enqueueActionDrain() {
        if (!featureFlagProvider.offlineEnabled()) return
        val userId = apiPrefs.user?.id ?: return
        workManager.enqueueUniqueWork(
            uniqueActionDrainName(userId),
            ExistingWorkPolicy.KEEP,
            buildActionDrainRequest(userId),
        )
    }

    suspend fun syncCoursesNow(courseIds: List<Long>) {
        if (!featureFlagProvider.offlineEnabled()) return
        val userId = apiPrefs.user?.id ?: return
        val settings = settingsOrDefault()
        workManager.enqueueUniqueWork(
            uniqueOneTimeName(userId),
            ExistingWorkPolicy.REPLACE,
            buildOneTimeRequest(courseIds, settings, userId),
        )
    }

    suspend fun applyPeriodicSchedule() {
        val userId = apiPrefs.user?.id ?: return
        val uniqueName = uniquePeriodicName(userId)

        if (!featureFlagProvider.offlineEnabled()) {
            cancelPeriodic(uniqueName)
            return
        }
        val settings = settingsOrDefault()
        if (!settings.autoSyncEnabled) {
            cancelPeriodic(uniqueName)
            return
        }
        if (courseSyncPlanDao.findAll().isEmpty()) {
            cancelPeriodic(uniqueName)
            return
        }

        val fingerprint = fingerprint(userId, settings)
        if (fingerprint == lastAppliedFingerprint) return

        workManager.enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildPeriodicRequest(settings, userId),
        )
        lastAppliedFingerprint = fingerprint
    }

    private fun cancelPeriodic(uniqueName: String) {
        workManager.cancelUniqueWork(uniqueName)
        lastAppliedFingerprint = null
    }

    private fun fingerprint(userId: Long, settings: HorizonSyncSettingsEntity): String =
        "$userId|${settings.syncFrequency}|${settings.wifiOnly}"

    suspend fun cancelCurrentSync() {
        val userId = apiPrefs.user?.id ?: return
        workManager.cancelAllWorkByTag(oneTimeTag(userId))
        val periodicInfos = workManager.getWorkInfosForUniqueWorkFlow(uniquePeriodicName(userId)).first()
        periodicInfos
            .filter { it.state == WorkInfo.State.RUNNING }
            .forEach { workManager.cancelWorkById(it.id) }
    }

    suspend fun fullStop() {
        val userId = apiPrefs.user?.id ?: return
        workManager.cancelAllWorkByTag(oneTimeTag(userId))
        cancelPeriodic(uniquePeriodicName(userId))
        awaitWorkersDrained(userId)
    }

    private suspend fun awaitWorkersDrained(userId: Long) {
        for (tag in listOf(oneTimeTag(userId), periodicTag(userId))) {
            workManager.getWorkInfosByTagFlow(tag)
                .map { infos -> infos.all { it.state.isFinished } }
                .first { it }
        }
    }

    fun isRunning(): Flow<Boolean> {
        val userId = apiPrefs.user?.id ?: return flowOf(false)
        return combine(
            workManager.getWorkInfosByTagFlow(oneTimeTag(userId)),
            workManager.getWorkInfosByTagFlow(periodicTag(userId)),
        ) { oneTime, periodic ->
            (oneTime + periodic).any { it.state == WorkInfo.State.RUNNING }
        }
    }

    private fun uniqueOneTimeName(userId: Long) = "HorizonOfflineSyncOneTime-$userId"
    private fun uniquePeriodicName(userId: Long) = "HorizonOfflinePeriodicSync-$userId"
    private fun uniqueActionDrainName(userId: Long) = "HorizonOfflineActionDrain-$userId"
    private fun oneTimeTag(userId: Long) = "${HorizonOfflineSyncWorker.ONE_TIME_TAG}-$userId"
    private fun periodicTag(userId: Long) = "${HorizonOfflineSyncWorker.PERIODIC_TAG}-$userId"

    private suspend fun settingsOrDefault(): HorizonSyncSettingsEntity {
        return syncSettingsDao.getSettingsOnce() ?: HorizonSyncSettingsEntity()
    }

    private fun buildOneTimeRequest(courseIds: List<Long>, settings: HorizonSyncSettingsEntity, userId: Long) =
        OneTimeWorkRequestBuilder<HorizonOfflineSyncWorker>()
            .setInputData(
                Data.Builder()
                    .putLongArray(HorizonOfflineSyncWorker.COURSE_IDS_KEY, courseIds.toLongArray())
                    .putLong(HorizonOfflineSyncWorker.USER_ID_KEY, userId)
                    .build()
            )
            .setConstraints(networkConstraints(settings))
            .addTag(HorizonOfflineSyncWorker.ONE_TIME_TAG)
            .addTag(oneTimeTag(userId))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

    private fun buildActionDrainRequest(userId: Long) =
        OneTimeWorkRequestBuilder<OfflineActionDrainWorker>()
            .setInputData(
                Data.Builder()
                    .putLong(OfflineActionDrainWorker.USER_ID_KEY, userId)
                    .build()
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(OfflineActionDrainWorker.TAG)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

    private fun buildPeriodicRequest(settings: HorizonSyncSettingsEntity, userId: Long) =
        PeriodicWorkRequestBuilder<HorizonOfflineSyncWorker>(periodDays(settings), TimeUnit.DAYS)
            .setInputData(
                Data.Builder()
                    .putLong(HorizonOfflineSyncWorker.USER_ID_KEY, userId)
                    .build()
            )
            .setConstraints(networkConstraintsForPeriodic(settings))
            .addTag(HorizonOfflineSyncWorker.PERIODIC_TAG)
            .addTag(periodicTag(userId))
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

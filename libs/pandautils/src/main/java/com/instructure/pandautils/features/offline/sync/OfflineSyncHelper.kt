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

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class OfflineSyncHelper(
    private val workManager: WorkManager,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val apiPrefs: ApiPrefs
) {

    suspend fun syncCourses(courseIds: List<Long>) {
        when {
            isPeriodicWorkRunning() -> {
                scheduleWork(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
            }

            isWorkScheduled() || !syncSettingsFacade.getSyncSettings().autoSyncEnabled -> {
                val runningInfo = getRunningOneTimeWorkInfo()
                if (runningInfo != null) {
                    workManager.cancelWorkById(runningInfo.id)
                }
                syncOnce(courseIds)
            }

            else -> {
                scheduleWork()
            }
        }
    }

    fun cancelWork() {
        workManager.cancelUniqueWork(apiPrefs.user?.id.toString())
    }

    suspend fun updateWork() {
        val workRequest = createPeriodicWorkRequest()
        workManager.updateWork(workRequest)
    }

    suspend fun scheduleWork(policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE, addDelay: Boolean = false) {
        val workRequest = createPeriodicWorkRequest(addDelay)
        workManager.enqueueUniquePeriodicWork(
            apiPrefs.user?.id.toString(),
            policy,
            workRequest
        )
    }

    suspend fun syncOnce(courseIds: List<Long>) {
        val syncSettings = syncSettingsFacade.getSyncSettings()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (syncSettings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putLongArray(COURSE_IDS, courseIds.toLongArray())
            .build()
        val workRequest = OneTimeWorkRequest.Builder(OfflineSyncWorker::class.java)
            .addTag(OfflineSyncWorker.ONE_TIME_TAG)
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()
        workManager.enqueue(workRequest)
    }

    suspend fun cancelRunningWorkers() {
        if (isPeriodicWorkRunning()) {
            scheduleWork(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, true)
        }

        val runningWorkInfo = getRunningOneTimeWorkInfo()
        if (runningWorkInfo != null) {
            workManager.cancelWorkById(runningWorkInfo.id)
        }
    }

    private suspend fun isWorkScheduled(): Boolean {
        return workManager.getWorkInfosForUniqueWorkFlow(apiPrefs.user?.id.toString()).first().any { it.state != WorkInfo.State.CANCELLED }
    }

    private suspend fun isPeriodicWorkRunning(): Boolean {
        return workManager.getWorkInfosForUniqueWorkFlow(apiPrefs.user?.id.toString()).first()
            .any { it.state == WorkInfo.State.RUNNING }
    }

    private suspend fun getRunningOneTimeWorkInfo(): WorkInfo? {
        return workManager.getWorkInfosByTagFlow(OfflineSyncWorker.ONE_TIME_TAG).first()
            .firstOrNull { it.state == WorkInfo.State.RUNNING }
    }

    private suspend fun createPeriodicWorkRequest(setDelay: Boolean = false): PeriodicWorkRequest {
        val syncSettings = syncSettingsFacade.getSyncSettings()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (syncSettings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val frequency: Long = if (syncSettings.syncFrequency == SyncFrequency.DAILY) 1 else 7

        val workRequestBuilder = PeriodicWorkRequest.Builder(
            OfflineSyncWorker::class.java,
            frequency, TimeUnit.DAYS
        )
            .addTag(OfflineSyncWorker.PERIODIC_TAG)
            .setConstraints(constraints)

        if (setDelay) workRequestBuilder.setInitialDelay(frequency, TimeUnit.DAYS)

        return workRequestBuilder.build()
    }

    suspend fun scheduleWorkAfterLogin() {
        if (syncSettingsFacade.getSyncSettings().autoSyncEnabled && !isWorkScheduled()) {
            scheduleWork()
        }
    }
}
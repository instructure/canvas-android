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

import androidx.work.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import java.util.*
import java.util.concurrent.TimeUnit

class OfflineSyncHelper(
    private val workManager: WorkManager,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val apiPrefs: ApiPrefs
) {

    suspend fun syncCourses(courseIds: List<Long>) {
        cancelRunningWorkers()
        if (isWorkScheduled() || !syncSettingsFacade.getSyncSettings().autoSyncEnabled) {
            syncOnce(courseIds)
        } else {
            scheduleWork()
        }
    }

    fun cancelWork() {
        workManager.cancelUniqueWork(apiPrefs.user?.id.toString())
    }

    suspend fun updateWork() {
        val id = workManager.getWorkInfosForUniqueWork(apiPrefs.user?.id.toString()).await().firstOrNull()?.id
        val workRequest = createWorkRequest(id)
        workManager.updateWork(workRequest)
    }

    suspend fun scheduleWork() {
        val workRequest = createWorkRequest()
        workManager.enqueueUniquePeriodicWork(
            apiPrefs.user?.id.toString(),
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun syncOnce(courseIds: List<Long>) {
        val inputData = Data.Builder()
            .putLongArray(COURSE_IDS, courseIds.toLongArray())
            .build()
        val workRequest = OneTimeWorkRequest.Builder(OfflineSyncWorker::class.java)
            .addTag(OfflineSyncWorker.TAG)
            .setInputData(inputData)
            .build()
        workManager.enqueue(workRequest)
    }

    fun cancelRunningWorkers() {
        workManager.cancelAllWorkByTag(CourseSyncWorker.TAG)
        workManager.cancelAllWorkByTag(FileSyncWorker.TAG)
    }

    private suspend fun isWorkScheduled(): Boolean {
        return workManager.getWorkInfosForUniqueWork(apiPrefs.user?.id.toString()).await().isNotEmpty()
    }

    private suspend fun createWorkRequest(id: UUID? = null): PeriodicWorkRequest {
        val syncSettings = syncSettingsFacade.getSyncSettings()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (syncSettings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequestBuilder = PeriodicWorkRequest.Builder(
            OfflineSyncWorker::class.java,
            if (syncSettings.syncFrequency == SyncFrequency.DAILY) 1 else 7, TimeUnit.DAYS
        )
            .addTag(OfflineSyncWorker.TAG)
            .setConstraints(constraints)

        id?.let {
            workRequestBuilder.setId(it)
        }

        return workRequestBuilder.build()
    }
}
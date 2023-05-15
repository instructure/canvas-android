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
import com.instructure.pandautils.features.offline.syncsettings.SyncFrequency
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import java.util.concurrent.TimeUnit

class OfflineSyncHelper(
    private val workManager: WorkManager,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val apiPrefs: ApiPrefs
) {

    suspend fun syncCourses(courseIds: List<Long>) {
        if (isWorkScheduled()) {
            syncOnce(courseIds)
        } else {
            scheduleSync()
        }
    }

    private fun syncOnce(courseIds: List<Long>) {
        val inputData = Data.Builder()
            .putLongArray(COURSE_IDS, courseIds.toLongArray())
            .build()
        val workRequest = OneTimeWorkRequest.Builder(OfflineSyncWorker::class.java)
            .setInputData(inputData)
            .build()
        workManager.enqueue(workRequest)
    }

    private suspend fun isWorkScheduled(): Boolean {
        return workManager.getWorkInfosByTag(apiPrefs.user?.id.toString()).await().isNotEmpty()
    }

    private suspend fun scheduleSync() {
        val syncSettings = syncSettingsFacade.getSyncSettings()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (syncSettings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequest.Builder(
            OfflineSyncWorker::class.java,
            if (syncSettings.syncFrequency == SyncFrequency.DAILY) 1 else 7, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(apiPrefs.user?.id.toString())
            .build()

        workManager.enqueueUniquePeriodicWork(apiPrefs.user?.id.toString(), ExistingPeriodicWorkPolicy.UPDATE, workRequest)
    }

}
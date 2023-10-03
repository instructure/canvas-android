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

package com.instructure.pandautils.room.offline.facade

import androidx.lifecycle.LiveData
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.room.offline.daos.SyncSettingsDao
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity

class SyncSettingsFacade(private val syncSettingsDao: SyncSettingsDao) {

    suspend fun getSyncSettingsListenable(): LiveData<SyncSettingsEntity?> {
        val settings = syncSettingsDao.findSyncSettings()
        if (settings == null) createDefault()
        return syncSettingsDao.findSyncSettingsLiveData()
    }

    suspend fun getSyncSettings(): SyncSettingsEntity {
        return syncSettingsDao.findSyncSettings() ?: createDefault()
    }

    suspend fun update(syncSettingsEntity: SyncSettingsEntity) {
        syncSettingsDao.update(syncSettingsEntity)
    }

    private suspend fun createDefault(): SyncSettingsEntity {
        val default = SyncSettingsEntity(
            autoSyncEnabled = true,
            syncFrequency = SyncFrequency.DAILY,
            wifiOnly = true
        )

        syncSettingsDao.insert(default)

        return default
    }
}
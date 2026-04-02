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
package com.instructure.horizon.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType

@Dao
interface HorizonSyncMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HorizonSyncMetadataEntity)

    @Query("SELECT lastSyncedAtMs FROM horizon_sync_metadata WHERE dataType = :dataType")
    suspend fun getLastSyncedAt(dataType: SyncDataType): Long?
}

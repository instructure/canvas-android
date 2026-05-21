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
import com.instructure.horizon.database.entity.HorizonFileSyncPlanEntity
import com.instructure.horizon.offline.sync.HorizonProgressState
import kotlinx.coroutines.flow.Flow

@Dao
interface HorizonFileSyncPlanDao {

    @Query("SELECT * FROM horizon_file_sync_plan")
    fun findAllFlow(): Flow<List<HorizonFileSyncPlanEntity>>

    @Query("SELECT * FROM horizon_file_sync_plan")
    suspend fun findAllOnce(): List<HorizonFileSyncPlanEntity>

    @Query("SELECT * FROM horizon_file_sync_plan WHERE courseId = :courseId")
    suspend fun findByCourseId(courseId: Long): List<HorizonFileSyncPlanEntity>

    @Query("SELECT * FROM horizon_file_sync_plan WHERE courseId = :courseId")
    fun findByCourseIdFlow(courseId: Long): Flow<List<HorizonFileSyncPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HorizonFileSyncPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<HorizonFileSyncPlanEntity>)

    @Query("UPDATE horizon_file_sync_plan SET progress = :progress, state = :state WHERE fileId = :fileId")
    suspend fun updateProgress(fileId: Long, progress: Int, state: HorizonProgressState)

    @Query("DELETE FROM horizon_file_sync_plan")
    suspend fun deleteAll()
}

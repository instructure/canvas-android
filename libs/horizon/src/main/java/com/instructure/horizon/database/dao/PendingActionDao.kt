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
import com.instructure.horizon.database.entity.PendingActionEntity

@Dao
interface PendingActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingActionEntity): Long

    @Query("SELECT * FROM pending_actions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PendingActionEntity?

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingActionEntity>

    @Query("SELECT * FROM pending_actions WHERE retryCount < :maxRetries ORDER BY createdAt ASC")
    suspend fun getReadyToDrain(maxRetries: Int): List<PendingActionEntity>

    @Query("SELECT id FROM pending_actions")
    suspend fun getAllIds(): List<Long>

    @Query("UPDATE pending_actions SET retryCount = :retryCount, lastError = :lastError WHERE id = :id")
    suspend fun updateRetry(id: Long, retryCount: Int, lastError: String?)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM pending_actions")
    suspend fun deleteAll()
}

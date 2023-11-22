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

package com.instructure.pandautils.room.offline.daos

import androidx.room.*
import com.instructure.pandautils.room.offline.entities.SubmissionEntity

@Dao
interface SubmissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SubmissionEntity): Long

    @Upsert(entity = SubmissionEntity::class)
    suspend fun insertOrUpdate(entity: SubmissionEntity)

    @Delete
    suspend fun delete(entity: SubmissionEntity)

    @Update
    suspend fun update(entity: SubmissionEntity)

    @Query("SELECT * FROM SubmissionEntity WHERE id = :id ORDER BY attempt ASC")
    suspend fun findById(id: Long): List<SubmissionEntity>

    @Query("SELECT * FROM SubmissionEntity WHERE assignmentId IN (:assignmentIds)")
    suspend fun findByAssignmentIds(assignmentIds: List<Long>): List<SubmissionEntity>

    @Query("SELECT * FROM SubmissionEntity WHERE assignmentId = :assignmentId")
    suspend fun findByAssignmentId(assignmentId: Long): SubmissionEntity?
}
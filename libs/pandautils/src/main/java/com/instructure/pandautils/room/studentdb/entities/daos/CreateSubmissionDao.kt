/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.room.studentdb.entities.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity

@Dao
interface CreateSubmissionDao {

    @Insert
    suspend fun insert(submission: CreateSubmissionEntity): Long

    @Query("SELECT * FROM CreateSubmissionEntity WHERE id = :id")
    suspend fun findSubmissionById(id: Long): CreateSubmissionEntity?

    @Query("SELECT * FROM CreateSubmissionEntity WHERE id = :id")
    fun findSubmissionByIdLiveData(id: Long): LiveData<CreateSubmissionEntity?>

    @Query("DELETE FROM CreateSubmissionEntity WHERE id = :id")
    suspend fun deleteSubmissionById(id: Long)

    @Query("UPDATE CreateSubmissionEntity SET errorFlag = :error WHERE id = :id")
    suspend fun setSubmissionError(error: Boolean, id: Long)

    @Query("SELECT * FROM CreateSubmissionEntity WHERE assignmentId = :assignmentId AND userId = :userId")
    suspend fun findSubmissionsByAssignmentId(
        assignmentId: Long,
        userId: Long
    ): List<CreateSubmissionEntity>

    @Query("SELECT * FROM CreateSubmissionEntity WHERE assignmentId = :assignmentId AND userId = :userId LIMIT 1")
    fun findSubmissionByAssignmentIdLiveData(
        assignmentId: Long,
        userId: Long
    ): LiveData<CreateSubmissionEntity?>

    @Query("SELECT * FROM CreateSubmissionEntity WHERE assignmentId = :assignmentId AND userId = :userId LIMIT 1")
    fun findSubmissionsByAssignmentIdLiveData(
        assignmentId: Long,
        userId: Long
    ): LiveData<List<CreateSubmissionEntity>>

    @Query("DELETE FROM CreateSubmissionEntity WHERE assignmentId = :assignmentId AND userId = :userId")
    suspend fun deleteSubmissionsForAssignmentId(assignmentId: Long, userId: Long)

    @Query("UPDATE CreateSubmissionEntity SET currentFile = :currentFile, fileCount = :fileCount, progress = :progress WHERE id = :id")
    suspend fun updateProgress(currentFile: Long, fileCount: Long, progress: Double, id: Long)

    @Query("SELECT id FROM CreateSubmissionEntity WHERE ROWID = last_insert_rowid()")
    suspend fun getLastInsert(): Long

    @Query("SELECT * FROM CreateSubmissionEntity WHERE ROWID = :rowId")
    suspend fun findSubmissionByRowId(rowId: Long): CreateSubmissionEntity?

    @Query("DELETE FROM CreateSubmissionEntity WHERE id = :id AND userId = :userId AND isDraft = 1")
    suspend fun deleteDraftById(id: Long, userId: Long)
}
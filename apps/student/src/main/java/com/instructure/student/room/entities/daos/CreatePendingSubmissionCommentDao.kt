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
package com.instructure.student.room.entities.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.room.entities.CreatePendingSubmissionCommentEntity
import java.util.Date

@Dao
interface CreatePendingSubmissionCommentDao {

    @Query("SELECT * FROM CreatePendingSubmissionCommentEntity")
    suspend fun findAll(): List<CreatePendingSubmissionCommentEntity>

    @Query("SELECT * FROM CreatePendingSubmissionCommentEntity WHERE id = :id")
    suspend fun findCommentById(id: Long): CreatePendingSubmissionCommentEntity?

    @Query("UPDATE CreatePendingSubmissionCommentEntity SET errorFlag = :error WHERE id = :id")
    suspend fun setCommentError(error: Boolean, id: Long)

    @Query("UPDATE CreatePendingSubmissionCommentEntity SET currentFile = :currentFile, fileCount = :fileCount, progress = :progress WHERE id = :id")
    suspend fun updateCommentProgress(id: Long, currentFile: Long, fileCount: Long, progress: Double)

    @Query("DELETE FROM CreatePendingSubmissionCommentEntity WHERE id = :id")
    suspend fun deleteCommentById(id: Long)

    @Query("INSERT INTO CreatePendingSubmissionCommentEntity (accountDomain, canvasContext, assignmentName, assignmentId, lastActivityDate, isGroupMessage, message, mediaPath, attemptId) VALUES (:accountDomain, :canvasContext, :assignmentName, :assignmentId, :lastActivityDate, :isGroupMessage, :message, :mediaPath, :attemptId)")
    suspend fun insertComment(
        accountDomain: String,
        canvasContext: CanvasContext,
        assignmentName: String,
        assignmentId: Long,
        lastActivityDate: Date,
        isGroupMessage: Boolean,
        message: String?,
        mediaPath: String?,
        attemptId: Long?
    )

    @Query("SELECT id FROM CreatePendingSubmissionCommentEntity WHERE ROWID = last_insert_rowid()")
    suspend fun getLastInsert(): Long

    @Query("SELECT * FROM CreatePendingSubmissionCommentEntity WHERE accountDomain = :accountDomain AND assignmentId = :assignmentId")
    suspend fun findCommentsByAccountAndAssignmentId(accountDomain: String, assignmentId: Long): List<CreatePendingSubmissionCommentEntity>

    @Query("SELECT * FROM CreatePendingSubmissionCommentEntity WHERE accountDomain = :accountDomain AND assignmentId = :assignmentId")
    fun findCommentsByAccountAndAssignmentIdLiveData(accountDomain: String, assignmentId: Long): LiveData<List<CreatePendingSubmissionCommentEntity>?>
}
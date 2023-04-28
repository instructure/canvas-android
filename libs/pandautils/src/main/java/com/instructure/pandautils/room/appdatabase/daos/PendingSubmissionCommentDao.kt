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

package com.instructure.pandautils.room.appdatabase.daos

import androidx.room.*
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.PendingSubmissionCommentWithFileUploadInput

@Dao
interface PendingSubmissionCommentDao {

    @Insert
    suspend fun insert(pendingSubmissionCommentEntity: PendingSubmissionCommentEntity): Long

    @Delete
    suspend fun delete(pendingSubmissionCommentEntity: PendingSubmissionCommentEntity)

    @Delete
    suspend fun deleteAll(pendingSubmissionCommentEntities: List<PendingSubmissionCommentEntity>)

    @Update
    suspend fun update(pendingSubmissionCommentEntity: PendingSubmissionCommentEntity)

    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE workerId=:workerId")
    suspend fun findByWorkerId(workerId: String): PendingSubmissionCommentEntity?

    @Transaction
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE workerId=:workerId")
    suspend fun findByWorkerIdWithInputData(workerId: String): PendingSubmissionCommentWithFileUploadInput?

    @Transaction
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE pageId=:pageId")
    suspend fun findByPageId(pageId: String): List<PendingSubmissionCommentWithFileUploadInput>?

    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE id=:id")
    suspend fun findById(id: Long): PendingSubmissionCommentEntity?

    @Transaction
    @Query("SELECT * FROM PendingSubmissionCommentEntity WHERE status=:status AND workerId IS NOT NULL")
    suspend fun findByStatus(status: String): List<PendingSubmissionCommentWithFileUploadInput>?

}
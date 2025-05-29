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
 */package com.instructure.pandautils.room.studentdb.entities.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity

@Dao
interface CreateFileSubmissionDao {

    @Insert
    suspend fun insert(createFileSubmissionEntity: CreateFileSubmissionEntity)

    @Query("SELECT * FROM CreateFileSubmissionEntity WHERE dbSubmissionId = :id LIMIT 1")
    suspend fun findFileForSubmissionId(id: Long): CreateFileSubmissionEntity?

    @Query("SELECT * FROM CreateFileSubmissionEntity WHERE dbSubmissionId = :id")
    suspend fun findFilesForSubmissionId(id: Long): List<CreateFileSubmissionEntity>

    @Query("SELECT * FROM CreateFileSubmissionEntity WHERE id != :id AND fullPath = :fullPath")
    suspend fun findFilesForPath(id: Long, fullPath: String?): List<CreateFileSubmissionEntity>

    @Query("DELETE FROM CreateFileSubmissionEntity WHERE dbSubmissionId = :submissionId")
    suspend fun deleteFilesForSubmissionId(submissionId: Long)

    @Query("UPDATE CreateFileSubmissionEntity SET attachmentId = :attachmentId, errorFlag = :errorFlag, error = :error WHERE id = :id")
    suspend fun setFileAttachmentIdAndError(
        attachmentId: Long?,
        errorFlag: Boolean,
        error: String?,
        id: Long
    )

    @Query("UPDATE CreateFileSubmissionEntity SET errorFlag = :errorFlag, error = :errorMessage WHERE id = :id")
    suspend fun setFileError(errorFlag: Boolean, errorMessage: String?, id: Long)

    @Query("DELETE FROM CreateFileSubmissionEntity WHERE id = :fileId")
    suspend fun deleteFileById(fileId: Long)
}
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
import com.instructure.pandautils.room.appdatabase.entities.SubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.SubmissionCommentWithAttachments

@Dao
interface SubmissionCommentDao {

    @Insert
    suspend fun insert(submissionComment: SubmissionCommentEntity): Long

    @Delete
    suspend fun delete(submissionComment: SubmissionCommentEntity)

    @Update
    suspend fun update(submissionComment: SubmissionCommentEntity)

    @Transaction
    @Query("SELECT * FROM SubmissionCommentEntity WHERE id=:id")
    suspend fun findById(id: Long): SubmissionCommentWithAttachments?
}
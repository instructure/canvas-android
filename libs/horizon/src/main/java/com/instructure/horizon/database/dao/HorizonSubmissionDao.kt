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
import androidx.room.Transaction
import com.instructure.horizon.database.entity.HorizonSubmissionAttachmentEntity
import com.instructure.horizon.database.entity.HorizonSubmissionEntity

@Dao
interface HorizonSubmissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<HorizonSubmissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<HorizonSubmissionAttachmentEntity>)

    @Query("SELECT * FROM horizon_submissions WHERE assignmentId = :assignmentId ORDER BY attempt ASC")
    suspend fun getSubmissions(assignmentId: Long): List<HorizonSubmissionEntity>

    @Query("SELECT * FROM horizon_submission_attachments WHERE assignmentId = :assignmentId")
    suspend fun getAttachments(assignmentId: Long): List<HorizonSubmissionAttachmentEntity>

    @Query("DELETE FROM horizon_submissions WHERE assignmentId = :assignmentId")
    suspend fun deleteSubmissionsForAssignment(assignmentId: Long)

    @Query("DELETE FROM horizon_submission_attachments WHERE assignmentId = :assignmentId")
    suspend fun deleteAttachmentsForAssignment(assignmentId: Long)

    @Query("DELETE FROM horizon_submissions WHERE assignmentId IN (SELECT assignmentId FROM horizon_assignment_details WHERE courseId = :courseId)")
    suspend fun deleteByCourseId(courseId: Long)

    @Query("DELETE FROM horizon_submission_attachments WHERE assignmentId IN (SELECT assignmentId FROM horizon_assignment_details WHERE courseId = :courseId)")
    suspend fun deleteAttachmentsByCourseId(courseId: Long)

    @Transaction
    suspend fun replaceForAssignment(
        assignmentId: Long,
        submissions: List<HorizonSubmissionEntity>,
        attachments: List<HorizonSubmissionAttachmentEntity>,
    ) {
        deleteAttachmentsForAssignment(assignmentId)
        deleteSubmissionsForAssignment(assignmentId)
        insertSubmissions(submissions)
        if (attachments.isNotEmpty()) {
            insertAttachments(attachments)
        }
    }
}

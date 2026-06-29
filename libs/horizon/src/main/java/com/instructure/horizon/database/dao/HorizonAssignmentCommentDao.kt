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
import com.instructure.horizon.database.entity.HorizonAssignmentCommentAttachmentEntity
import com.instructure.horizon.database.entity.HorizonAssignmentCommentEntity

@Dao
interface HorizonAssignmentCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: HorizonAssignmentCommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<HorizonAssignmentCommentAttachmentEntity>)

    @Query("SELECT * FROM horizon_assignment_comments WHERE assignmentId = :assignmentId AND attempt = :attempt ORDER BY createdAtMs ASC")
    suspend fun getComments(assignmentId: Long, attempt: Int): List<HorizonAssignmentCommentEntity>

    @Query("SELECT * FROM horizon_assignment_comment_attachments WHERE commentId IN (:commentIds)")
    suspend fun getAttachments(commentIds: List<Long>): List<HorizonAssignmentCommentAttachmentEntity>

    @Query("SELECT COUNT(*) FROM horizon_assignment_comments WHERE assignmentId = :assignmentId AND read = 0")
    suspend fun getUnreadCommentCount(assignmentId: Long): Int

    @Query("DELETE FROM horizon_assignment_comments WHERE assignmentId = :assignmentId AND attempt = :attempt")
    suspend fun deleteCommentsForAttempt(assignmentId: Long, attempt: Int)

    @Query("DELETE FROM horizon_assignment_comments WHERE assignmentId IN (SELECT assignmentId FROM horizon_assignment_details WHERE courseId = :courseId)")
    suspend fun deleteByCourseId(courseId: Long)

    @Query("DELETE FROM horizon_assignment_comment_attachments WHERE commentId IN (SELECT id FROM horizon_assignment_comments WHERE assignmentId IN (SELECT assignmentId FROM horizon_assignment_details WHERE courseId = :courseId))")
    suspend fun deleteAttachmentsByCourseId(courseId: Long)

    @Transaction
    suspend fun replaceCommentsForAttempt(
        assignmentId: Long,
        attempt: Int,
        commentWithAttachments: List<Pair<HorizonAssignmentCommentEntity, List<HorizonAssignmentCommentAttachmentEntity>>>,
    ) {
        deleteCommentsForAttempt(assignmentId, attempt)
        for ((comment, attachments) in commentWithAttachments) {
            val commentId = insertComment(comment)
            if (attachments.isNotEmpty()) {
                insertAttachments(attachments.map { it.copy(commentId = commentId) })
            }
        }
    }
}
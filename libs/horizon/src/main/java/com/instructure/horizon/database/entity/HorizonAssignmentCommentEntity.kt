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
package com.instructure.horizon.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores a single submission comment for offline access.
 * Comments are grouped by [assignmentId] and [attempt].
 * [createdAtMs] stores [java.util.Date.getTime] for sorting.
 */
@Entity(
    tableName = "horizon_assignment_comments",
    indices = [Index("assignmentId", "attempt")]
)
data class HorizonAssignmentCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assignmentId: Long,
    val attempt: Int,
    val authorId: Long,
    val authorName: String,
    val commentText: String,
    val createdAtMs: Long,
    val read: Boolean,
)

/**
 * Stores attachments for a [HorizonAssignmentCommentEntity].
 */
@Entity(
    tableName = "horizon_assignment_comment_attachments",
    indices = [Index("commentId")],
    foreignKeys = [
        ForeignKey(
            entity = HorizonAssignmentCommentEntity::class,
            parentColumns = ["id"],
            childColumns = ["commentId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class HorizonAssignmentCommentAttachmentEntity(
    @PrimaryKey val attachmentId: Long,
    val commentId: Long,
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
)
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
package com.instructure.pandautils.room.studentdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.CanvasContext
import java.util.Date

@Entity
data class CreatePendingSubmissionCommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val accountDomain: String,
    val canvasContext: CanvasContext,
    val assignmentName: String,
    val assignmentId: Long,
    val lastActivityDate: Date,
    val isGroupMessage: Boolean,
    val message: String? = null,
    val mediaPath: String? = null,
    val currentFile: Int = 0,
    val fileCount: Int = 0,
    val progress: Float? = null,
    val errorFlag: Boolean = false,
    val attemptId: Long? = null
)
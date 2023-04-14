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

package com.instructure.pandautils.room.appdatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class FileUploadInputEntity(
    @PrimaryKey
    val workerId: String,
    val courseId: Long? = null,
    val assignmentId: Long? = null,
    val quizId: Long? = null,
    val quizQuestionId: Long? = null,
    val position: Int? = null,
    val parentFolderId: Long? = null,
    val action: String,
    val userId: Long? = null,
    val attachments: List<Long> = emptyList(),
    val submissionId: Long? = null,
    var filePaths: List<String>,
    val attemptId: Long? = null,
    val notificationId: Int? = Random().nextInt()
)
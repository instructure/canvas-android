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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CreateSubmissionEntity::class,
            parentColumns = ["id"],
            childColumns = ["dbSubmissionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CreateFileSubmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dbSubmissionId: Long,
    val attachmentId: Long? = null,
    val name: String? = null,
    val size: Long? = null,
    val contentType: String? = null,
    val fullPath: String? = null,
    val error: String? = null,
    val errorFlag: Boolean = false,
)
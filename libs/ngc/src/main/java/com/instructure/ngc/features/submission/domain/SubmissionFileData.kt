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
package com.instructure.ngc.features.submission.domain

import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity

data class SubmissionFileData(
    val id: Long,
    val name: String,
    val size: Long,
    val contentType: String?,
    val fullPath: String?,
    val errorFlag: Boolean,
    val error: String?,
)

fun CreateFileSubmissionEntity.toSubmissionFileData(): SubmissionFileData = SubmissionFileData(
    id = id,
    name = name.orEmpty(),
    size = size ?: 0L,
    contentType = contentType,
    fullPath = fullPath,
    errorFlag = errorFlag,
    error = error,
)

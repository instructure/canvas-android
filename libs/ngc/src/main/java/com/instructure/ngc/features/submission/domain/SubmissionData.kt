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

import com.instructure.canvasapi2.models.Assignment
import java.util.Date

data class SubmissionData(
    val assignmentName: String,
    val courseName: String,
    val dueDate: Date?,
    val allowsTextEntry: Boolean,
    val lastAttempt: Long?,
    val draftHtml: String?,
    val draftSavedAt: Date?,
    val availableTypes: List<Assignment.SubmissionType> = emptyList(),
    val draftFiles: List<SubmissionFileData> = emptyList(),
    val fileDraftSavedAt: Date? = null,
    val allowedExtensions: List<String> = emptyList(),
    val draftUrl: String? = null,
    val urlDraftSavedAt: Date? = null,
)

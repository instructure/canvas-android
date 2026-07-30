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
package com.instructure.ngc.features.submission

import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.ngc.features.submission.domain.SubmissionFileData

data class SubmissionFileUiState(
    val id: Long,
    val name: String,
    val size: Long,
    val contentType: String?,
    val fullPath: String?,
    val state: SubmissionFileState = SubmissionFileState.Ready,
)

sealed interface SubmissionFileState {
    data object Ready : SubmissionFileState
    data class InProgress(val progress: Float? = null) : SubmissionFileState
    data class Error(val message: String) : SubmissionFileState
}

fun SubmissionFileUiState.toFileSubmitObject(): FileSubmitObject = FileSubmitObject(
    name = name,
    size = size,
    contentType = contentType.orEmpty(),
    fullPath = fullPath.orEmpty(),
)

fun SubmissionFileData.toSubmissionFileUiState(genericErrorMessage: String = ""): SubmissionFileUiState =
    SubmissionFileUiState(
        id = id,
        name = name,
        size = size,
        contentType = contentType,
        fullPath = fullPath,
        state = when {
            errorFlag -> SubmissionFileState.Error(error ?: genericErrorMessage)
            else -> SubmissionFileState.Ready
        },
    )

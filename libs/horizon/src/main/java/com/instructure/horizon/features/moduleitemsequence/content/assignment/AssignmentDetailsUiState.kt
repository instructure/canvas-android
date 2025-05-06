/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment

import com.instructure.horizon.horizonui.platform.LoadingState

data class AssignmentDetailsUiState(
    val loadingState: LoadingState = LoadingState(isPullToRefreshEnabled = false),
    val instructions: String = "",
    val ltiUrl: String = "",
    val submissions: List<SubmissionUiState> = emptyList(),
    val currentSubmissionAttempt: Long = -1L,
    val onNewAttemptClick: () -> Unit = {},
)

data class SubmissionUiState(
    val submissionAttempt: Long,
    val submissionContent: SubmissionContent,
    val date: String,
    val onClick: () -> Unit = {},
)

sealed class SubmissionContent {
    data class TextSubmission(val text: String) : SubmissionContent()
    data class FileSubmission(val fileItems: List<FileItemUiState>) : SubmissionContent()
}

data class FileItemUiState(
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
    val onClick: () -> Unit = {},
    val onDownloadClock: () -> Unit = {},
)
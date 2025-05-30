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

import android.net.Uri
import androidx.annotation.StringRes
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.cards.AttemptCardState
import com.instructure.horizon.horizonui.platform.LoadingState

data class AssignmentDetailsUiState(
    val loadingState: LoadingState = LoadingState(isPullToRefreshEnabled = false),
    val instructions: String = "",
    val ltiUrl: String = "",
    val submissionDetailsUiState: SubmissionDetailsUiState = SubmissionDetailsUiState(),
    val addSubmissionUiState: AddSubmissionUiState = AddSubmissionUiState(),
    val showSubmissionDetails: Boolean = false,
    val showAddSubmission: Boolean = false,
    val toolsBottomSheetUiState: ToolsBottomSheetUiState = ToolsBottomSheetUiState(),
    val ltiButtonPressed: ((String) -> Unit)? = null,
    val urlToOpen: String? = null,
    val onUrlOpened: () -> Unit = {},
    val submissionConfirmationUiState: SubmissionConfirmationUiState = SubmissionConfirmationUiState(),
)

data class SubmissionDetailsUiState(
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
    data class FileSubmission(val fileItems: List<FileItem>) : SubmissionContent()
}

data class FileItem(
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
    val thumbnailUrl: String,
    val fileId: Long
)

data class AddSubmissionUiState(
    val submissionTypes: List<AddSubmissionTypeUiState> = emptyList(),
    val selectedSubmissionTypeIndex: Int = 0,
    val onSubmissionTypeSelected: (Int) -> Unit = {},
    val onSubmissionButtonClicked: () -> Unit = {},
    val draftDateString: String = "",
    val onDeleteDraftClicked: () -> Unit = {},
    val showDeleteDraftConfirmation: Boolean = false,
    val onDismissDeleteDraftConfirmation: () -> Unit = {},
    val onDraftDeleted: () -> Unit = {},
    val showSubmissionConfirmation: Boolean = false,
    val onDismissSubmissionConfirmation: () -> Unit = {},
    val onSubmitAssignment: () -> Unit = {},
    val submissionInProgress: Boolean = false,
    val submitEnabled: Boolean = false,
    val errorMessage: String? = null,
)

sealed class AddSubmissionTypeUiState(@StringRes val labelRes: Int) {
    data class Text(
        val text: String = "",
        val onTextChanged: (String) -> Unit = {},
    ) : AddSubmissionTypeUiState(R.string.assignmentDetilas_submissionTypeText)

    data class File(
        val allowedTypes: List<String> = emptyList(),
        val cameraAllowed: Boolean = false,
        val galleryPickerAllowed: Boolean = false,
        val files: List<AddSubmissionFileUiState> = emptyList(),
        val onFileAdded: (Uri) -> Unit = {},
    ) : AddSubmissionTypeUiState(R.string.assignmentDetilas_submissionTypeFileUpload)
}

data class AddSubmissionFileUiState(
    val name: String = "",
    val path: String? = null,
    val onDeleteClicked: () -> Unit = {},
)

data class ToolsBottomSheetUiState(
    val show: Boolean = false,
    val onDismiss: () -> Unit = {},
    val onAttemptsClick: () -> Unit = {},
    val onCommentsClick: () -> Unit = {},
)

data class SubmissionConfirmationUiState(
    val show: Boolean = false,
    val onDismiss: () -> Unit = {},
    val attemptCardState: AttemptCardState? = null
)
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission

import android.net.Uri
import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.Assignment
import com.instructure.horizon.R

data class AddSubmissionUiState(
    val submissionTypes: List<AddSubmissionTypeUiState> = emptyList(),
    val selectedSubmissionTypeIndex: Int = 0,
    val onSubmissionTypeSelected: (Int) -> Unit = {},
    val onSubmissionButtonClicked: () -> Unit = {},
    val showSubmissionConfirmation: Boolean = false,
    val onDismissSubmissionConfirmation: () -> Unit = {},
    val onSubmitAssignment: () -> Unit = {},
    val submissionInProgress: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val onSnackbarDismiss: () -> Unit = {},
    val onAssignmentUpdated: (Assignment) -> Unit = {},
)

sealed class AddSubmissionTypeUiState(
    @StringRes val labelRes: Int,
    val submissionType: Assignment.SubmissionType,
    open val draftUiState: DraftUiState = DraftUiState(),
    open val submitEnabled: Boolean = false,
) {

    abstract fun copyWith(
        draftUiState: DraftUiState = this.draftUiState,
        submitEnabled: Boolean = this.submitEnabled
    ): AddSubmissionTypeUiState

    data class Text(
        val text: String = "",
        val onTextChanged: (String) -> Unit = {},
        override val draftUiState: DraftUiState = DraftUiState(),
        override val submitEnabled: Boolean = false
    ) : AddSubmissionTypeUiState(R.string.assignmentDetilas_submissionTypeText, Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
        override fun copyWith(
            draftUiState: DraftUiState,
            submitEnabled: Boolean
        ) = copy(draftUiState = draftUiState, submitEnabled = submitEnabled)
    }

    data class File(
        val allowedTypes: List<String> = emptyList(),
        val cameraAllowed: Boolean = false,
        val galleryPickerAllowed: Boolean = false,
        val files: List<AddSubmissionFileUiState> = emptyList(),
        val onFileAdded: (Uri) -> Unit = {},
        val uploadFileEnabled: Boolean = true,
        override val draftUiState: DraftUiState = DraftUiState(),
        override val submitEnabled: Boolean = false
    ) : AddSubmissionTypeUiState(R.string.assignmentDetilas_submissionTypeFileUpload, Assignment.SubmissionType.ONLINE_UPLOAD) {

        override fun copyWith(
            draftUiState: DraftUiState,
            submitEnabled: Boolean
        ) = copy(draftUiState = draftUiState, submitEnabled = submitEnabled)
    }
}

data class AddSubmissionFileUiState(
    val name: String = "",
    val path: String? = null,
    val onDeleteClicked: () -> Unit = {}
)

data class DraftUiState(
    val draftDateString: String = "",
    val onDeleteDraftClicked: () -> Unit = {},
    val showDeleteDraftConfirmation: Boolean = false,
    val onDismissDeleteDraftConfirmation: () -> Unit = {},
    val onDraftDeleted: () -> Unit = {}
)
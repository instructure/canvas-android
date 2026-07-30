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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneUiState

data class TextSubmissionUiState(
    val draftHtml: String = "",
    // Incremented only on external resets (load, delete-draft, submit success) — not on every
    // keystroke — so SubmissionTextEntryField can re-seed the RCE without resetting the caret.
    val contentResetKey: Int = 0,
)

data class FileSubmissionUiState(
    val files: List<SubmissionFileUiState> = emptyList(),
    val allowedExtensions: List<String> = emptyList(),
    val onFilesPicked: (List<FileSubmitObject>) -> Unit = {},
    val onFileRemoved: (id: Long) -> Unit = {},
    val onRetryFile: (id: Long) -> Unit = {},
)

data class UrlSubmissionUiState(
    val draftUrl: String = "",
    // https-normalized URL rendered in the preview WebView; blank while typing settles, for invalid
    // input, and for cleartext http:// URLs (which a WebView cannot load). Updated by the debounce.
    val previewUrl: String = "",
    // Inline error message shown under the field (e.g. "not a valid URL"); null when valid/blank.
    val urlError: String? = null,
    // Incremented only on external resets (load, delete-draft, submit success, type switch) — not on
    // every keystroke — so UrlSubmissionContent can re-seed the field without resetting the caret.
    val contentResetKey: Int = 0,
)

data class NGCSubmissionUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val onRetry: () -> Unit = {},
    val courseName: String = "",
    val assignmentName: String = "",
    val dueDateText: String = "",

    // Assignment-allowed ∩ NGC-implemented types. Single-entry = non-interactive type row.
    val availableTypes: List<Assignment.SubmissionType> = emptyList(),
    val selectedType: Assignment.SubmissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
    val onSubmissionTypeSelected: (Assignment.SubmissionType) -> Unit = {},

    val textState: TextSubmissionUiState = TextSubmissionUiState(),
    val fileState: FileSubmissionUiState = FileSubmissionUiState(),
    val urlState: UrlSubmissionUiState = UrlSubmissionUiState(),

    val savedAtText: String = "",
    val submitting: Boolean = false,
    val submitEnabled: Boolean = false,
    val errorMessage: String? = null,
    val submissionSuccess: Boolean = false,
    val onSubmissionSuccessHandled: () -> Unit = {},
    val supportingPane: SubmissionSupportingPaneUiState = SubmissionSupportingPaneUiState(),
)

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

data class NGCSubmissionUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val onRetry: () -> Unit = {},
    val courseName: String = "",
    val assignmentName: String = "",
    val dueDateText: String = "",
    val submissionTypeLabel: String = "",
    // Authoritative HTML for the RCE editor. Updated on every keystroke so it survives
    // rotation (the ViewModel is retained). Bumping contentResetKey re-seeds the RCE
    // on external resets (initial load, delete, submit success) without re-applying html
    // on every keystroke, which would reset the caret.
    val draftHtml: String = "",
    // Incremented ONLY on external resets: initial successful load, onDeleteDraft, and
    // submit success. Pass-C's SubmissionTextEntryField keys its RCE seed off this value.
    val contentResetKey: Int = 0,
    val savedAtText: String = "",
    val submitting: Boolean = false,
    val submitEnabled: Boolean = false,
    val errorMessage: String? = null,
    val submissionSuccess: Boolean = false,
    val onSubmissionSuccessHandled: () -> Unit = {},
)

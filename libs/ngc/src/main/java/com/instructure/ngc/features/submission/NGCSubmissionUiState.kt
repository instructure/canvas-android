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

import androidx.compose.runtime.Immutable
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.ngc.features.rubric.NgcRubricUiState
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneUiState

data class TextSubmissionUiState(
    val draftHtml: String = "",
    // Incremented only on external resets (load, delete-draft, submit success) — not on every
    // keystroke — so SubmissionTextEntryField can re-seed the RCE without resetting the caret.
    val contentResetKey: Int = 0,
)

@Immutable
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

/**
 * State for the inline PSPDFKit `STUDENT_ANNOTATION` document, backed by the canvadocs "draft" session.
 * [sessionLoading]/[sessionLoadError] cover the session fetch itself, separate from the top-level
 * [NGCSubmissionUiState.loading]/[NGCSubmissionUiState.submitting].
 *
 * [registerAnnotationFlush] is how the Compose layer hands the ViewModel a reference to the currently
 * hosted [com.instructure.ngc.features.submission.annotation.PdfAnnotationHostFragment]'s flush
 * function (or `null` when no fragment is currently hosted), so `onSubmit` can await any in-flight
 * annotation writes before finalizing the attempt (see MBL-19937 Phase 4).
 */
data class StudentAnnotationUiState(
    val sessionUrl: String = "",
    val sessionLoading: Boolean = false,
    val sessionLoadError: String? = null,
    val onRetrySessionLoad: () -> Unit = {},
    val registerAnnotationFlush: ((suspend () -> Boolean)?) -> Unit = {},
)

data class MediaSubmissionUiState(
    // The single attached clip (audio or video). All three fields are null when no clip is attached.
    // Unlike Text/Url there is no text field to re-seed, so no contentResetKey is needed — the clip
    // card renders directly off these fields.
    val clipName: String? = null,
    val clipPath: String? = null,
    val clipContentType: String? = null,
    val onClipReady: (FileSubmitObject) -> Unit = {},
)

/**
 * `@Immutable` because the Compose compiler infers this class as UNSTABLE from its `List` properties
 * (`availableTypes`, and transitively `fileState`/`rubricUiState`) — `List` is a read-only interface, so
 * the compiler cannot prove the backing collection never mutates.
 *
 * An unstable parameter makes a composable non-skippable, and this state is passed whole to
 * `SubmissionFormContent`, `SubmissionFormHeader`, `SubmissionFormFooter` and `SubmissionTypeRow`. The
 * result was that the entire form subtree recomposed on *every* state emission — every keystroke, every
 * upload-progress tick, every type switch — which is what made switching submission type read as the
 * whole screen flickering (MBL-19937 QA).
 *
 * The promise holds: every property is a `val`, and the lists are only ever replaced via `copy(...)`,
 * never mutated in place. Verified with the Compose compiler stability report.
 */
@Immutable
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

    // Read-only rubric shown on the supporting pane's Rubric tab; null when the assignment has no rubric.
    val rubricUiState: NgcRubricUiState? = null,

    val textState: TextSubmissionUiState = TextSubmissionUiState(),
    val fileState: FileSubmissionUiState = FileSubmissionUiState(),
    val urlState: UrlSubmissionUiState = UrlSubmissionUiState(),
    val studentAnnotationState: StudentAnnotationUiState = StudentAnnotationUiState(),
    val mediaState: MediaSubmissionUiState = MediaSubmissionUiState(),

    val savedAtText: String = "",
    val submitting: Boolean = false,
    val submitEnabled: Boolean = false,
    val errorMessage: String? = null,
    val submissionSuccess: Boolean = false,
    val onSubmissionSuccessHandled: () -> Unit = {},
    val supportingPane: SubmissionSupportingPaneUiState = SubmissionSupportingPaneUiState(),
)

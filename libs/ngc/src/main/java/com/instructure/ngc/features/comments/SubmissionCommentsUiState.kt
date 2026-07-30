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
package com.instructure.ngc.features.comments

import com.instructure.ngc.features.comments.domain.SubmissionCommentItem
import com.instructure.pandautils.views.RecordingMediaType
import java.io.File

data class SubmissionCommentsUiState(
    val comments: List<SubmissionCommentItem> = emptyList(),
    val commentText: String = "",
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
    val showAttachmentTypeDialog: Boolean = false,
    val fileSelectorDialogData: FileSelectorDialogData? = null,
    val showRecordFloatingView: RecordingMediaType? = null,
    val actions: SubmissionCommentsActions = SubmissionCommentsActions(),
)

data class FileSelectorDialogData(
    val assignmentId: Long,
    val courseId: Long,
    val userId: Long,
    val attempt: Long?,
)

/**
 * UI → ViewModel callbacks, wired to private ViewModel functions in the [SubmissionCommentsUiState]
 * the ViewModel emits. Grouped into their own data class since there are many of them.
 */
data class SubmissionCommentsActions(
    val onCommentChanged: (String) -> Unit = {},
    val onSendComment: () -> Unit = {},
    val onRetryComment: (SubmissionCommentItem) -> Unit = {},
    val onAddAttachment: () -> Unit = {},
    val onAttachmentTypeDialogDismiss: () -> Unit = {},
    val onChooseFiles: () -> Unit = {},
    val onRecordAudio: () -> Unit = {},
    val onRecordVideo: () -> Unit = {},
    val onRecordDialogDismiss: () -> Unit = {},
    val onMediaRecorded: (File) -> Unit = {},
)

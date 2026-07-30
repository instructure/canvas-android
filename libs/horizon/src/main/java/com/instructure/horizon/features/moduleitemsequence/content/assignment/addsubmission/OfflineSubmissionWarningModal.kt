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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.Modal
import com.instructure.horizon.horizonui.organisms.ModalDialogState

/**
 * Shared warning modal shown before any submission is queued offline (rich-text or file).
 * Consumed by both the text (CLXR-950) and file (CLXR-949) submission flows through
 * [AddSubmissionContent].
 */
@Composable
fun OfflineSubmissionWarningModal(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Modal(
        dialogState = ModalDialogState(
            title = stringResource(R.string.assignmentDetails_offlineSubmissionWarningTitle),
            message = stringResource(R.string.assignmentDetails_offlineSubmissionWarningMessage),
            primaryButtonTitle = stringResource(R.string.assignmentDetails_offlineSubmissionWarningConfirm),
            secondaryButtonTitle = stringResource(R.string.assignmentDetails_offlineSubmissionWarningCancel),
            primaryButtonClick = onConfirm,
            secondaryButtonClick = onDismiss
        ),
        onDismiss = onDismiss
    )
}

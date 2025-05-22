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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.content.assignment.AddSubmissionTypeUiState
import com.instructure.pandautils.compose.composables.rce.ComposeRCE
import com.instructure.pandautils.compose.composables.rce.RceControlsPosition

@Composable
fun AddTextSubmissionContent(
    uiState: AddSubmissionTypeUiState.Text,
    modifier: Modifier = Modifier,
    onRceFocused: () -> Unit = {},
) {
    ComposeRCE(
        html = uiState.text,
        hint = stringResource(R.string.assignmentDetails_textEntryHint),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onRceFocused = onRceFocused,
        onTextChangeListener = uiState.onTextChanged,
        rceControlsPosition = RceControlsPosition.BOTTOM
    )
}
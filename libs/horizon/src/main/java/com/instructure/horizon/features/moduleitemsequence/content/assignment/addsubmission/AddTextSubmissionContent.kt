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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.pandautils.compose.composables.rce.ComposeRCE
import com.instructure.pandautils.compose.composables.rce.RceControlsPosition
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun AddTextSubmissionContent(
    uiState: AddSubmissionTypeUiState.Text,
    modifier: Modifier = Modifier,
    onCursorYCoordinateChanged: (Float) -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    Box {
        ComposeRCE(
            html = uiState.text,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onRceFocused = { isFocused = true },
            onCursorYCoordinateChanged = onCursorYCoordinateChanged,
            onTextChangeListener = uiState.onTextChanged,
            rceControlsPosition = RceControlsPosition.BOTTOM,
            rceDialogThemeColor = ThemePrefs.brandColor,
            rceDialogButtonColor = ThemePrefs.brandColor,
            fileUploadRestParams = RestParams(
                shouldIgnoreToken = true,
                disableFileVerifiers = false
            )
        )

        // Display hint text when the RCE is empty
        // This is a workaround for the RCE, because it's not handling placeholders correctly. (Keyboard won't open automatically on the first click)
        if (uiState.text.isEmpty() && !isFocused) {
            Text(
                text = stringResource(R.string.assignmentDetails_textEntryHint),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.placeholder(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
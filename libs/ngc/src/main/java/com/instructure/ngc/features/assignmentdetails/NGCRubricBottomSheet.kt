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
package com.instructure.ngc.features.assignmentdetails

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.ngc.R
import com.instructure.ngc.features.rubric.NgcRubricUiState
import com.instructure.ngc.features.rubric.RubricContent

/**
 * Read-only rubric bottom sheet for the NGC Assignment Details screen. Wraps the shared [RubricContent]
 * in a [Tray] with a centered title; the criteria list and highlighting live in [RubricContent].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RubricBottomSheet(rubricUiState: NgcRubricUiState, onDismiss: () -> Unit) {
    Tray(onDismiss = onDismiss, modifier = Modifier.testTag("ngcAssignmentDetails_rubricSheet")) {
        RubricContent(
            rubric = rubricUiState,
            header = {
                Heading(
                    text = stringResource(R.string.ngc_rubric),
                    level = HeadingLevel.H3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
                )
            },
        )
    }
}

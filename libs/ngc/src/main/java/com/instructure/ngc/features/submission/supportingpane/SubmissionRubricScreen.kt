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

package com.instructure.ngc.features.submission.supportingpane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.features.rubric.NgcRubricUiState
import com.instructure.ngc.features.rubric.RubricContent

@Composable
fun SubmissionRubricScreen(
    rubric: NgcRubricUiState?,
    modifier: Modifier = Modifier,
) {
    if (rubric == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            Text(
                text = stringResource(R.string.ngc_rubricEmptyMessage),
                style = InstUIText.content,
                color = InstUISemanticColors.Text.muted(),
                textAlign = TextAlign.Center,
            )
        }
    } else {
        RubricContent(rubric = rubric, modifier = modifier.fillMaxSize())
    }
}

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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper

@Composable
fun SubmissionInstructionsScreen(
    loading: Boolean,
    html: String,
    modifier: Modifier = Modifier,
) {
    when {
        loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Spinner()
            }
        }
        html.isBlank() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.ngc_submissionNoInstructions),
                    color = InstUISemanticColors.Text.muted(),
                )
            }
        }
        else -> {
            // The WebView itself does not scroll; an outer Compose scroll owns scrolling so the
            // gesture cooperates with the bottom sheet's nested scroll (mirrors Horizon's
            // AssignmentDetailsScreen). ComposeCanvasWebViewWrapper's internal fillMaxSize is a
            // no-op under the scroll container's infinite height, so the WebView lays out at its
            // full content height.
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                        vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.ngc_submissionInstructionsLabel),
                    style = InstUIText.contentSmall,
                    color = InstUISemanticColors.Text.muted(),
                )
                ComposeCanvasWebViewWrapper(
                    content = html,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// region Previews

@Preview(name = "Instructions — Loading", showBackground = true)
@Composable
private fun InstructionsLoadingPreview() {
    InstUITheme {
        SubmissionInstructionsScreen(
            loading = true,
            html = "",
        )
    }
}

@Preview(name = "Instructions — Content", showBackground = true)
@Composable
private fun InstructionsContentPreview() {
    InstUITheme {
        SubmissionInstructionsScreen(
            loading = false,
            html = "<p>Write a 500-word essay about the International Space Station, focusing on its scientific contributions and international collaboration.</p>",
        )
    }
}

@Preview(name = "Instructions — Empty", showBackground = true)
@Composable
private fun InstructionsEmptyPreview() {
    InstUITheme {
        SubmissionInstructionsScreen(
            loading = false,
            html = "",
        )
    }
}

@Preview(
    name = "Instructions — Content (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun InstructionsContentDarkPreview() {
    InstUITheme {
        SubmissionInstructionsScreen(
            loading = false,
            html = "<p>Write a 500-word essay about the International Space Station, focusing on its scientific contributions and international collaboration.</p>",
        )
    }
}

// endregion

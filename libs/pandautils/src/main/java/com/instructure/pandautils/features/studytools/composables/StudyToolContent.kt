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
 *
 */
package com.instructure.pandautils.features.studytools.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.ShimmerBox
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.pandautils.features.studytools.StudyToolState
import com.instructure.pandautils.features.studytools.StudyToolsActions
import com.instructure.instui.compose.text.Text as InstUIText

/**
 * Everything below the panel header once a tool is open: the generated result with the shared footer,
 * or the skeleton / error that stands in for it.
 *
 * A null [state] means there is nothing to show for the selected tool yet — the case for Quiz and
 * Flashcards until MBL-20263 lands.
 */
@Composable
fun StudyToolContent(
    state: StudyToolState?,
    actions: StudyToolsActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (state) {
                null -> Unit

                StudyToolState.Generating -> StudyToolsSkeleton()

                is StudyToolState.Ready -> when (val result = state.result) {
                    is StudyToolResult.Summary -> SummarizeContent(summary = result)
                    // Renderers for these land with MBL-20263.
                    is StudyToolResult.Quiz, is StudyToolResult.Flashcards -> Unit
                }

                StudyToolState.Offline -> StudyToolsEmptyState(
                    message = stringResource(id = R.string.studyTools_offline),
                    buttonText = stringResource(id = R.string.retry),
                    onButtonClick = actions.onToolRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studyToolOffline")
                )

                is StudyToolState.Error -> StudyToolsEmptyState(
                    title = state.header,
                    // A retryable failure is worth trying again; anything else is not, so its copy must
                    // not end in "try again" either.
                    message = state.message ?: stringResource(
                        id = if (state.retryable) R.string.studyTools_genericError else R.string.studyTools_generationFailed
                    ),
                    // Rate limits and unusable page content do not improve on a second attempt, so
                    // those errors offer no retry at all.
                    buttonText = stringResource(id = R.string.retry).takeIf { state.retryable },
                    onButtonClick = actions.onToolRetry.takeIf { state.retryable },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studyToolError")
                )
            }
        }

        if (state is StudyToolState.Ready) {
            state.regenerateError?.let { RegenerateError(message = it) }

            StudyToolsFooter(
                feedback = state.feedback,
                onRegenerate = actions.onRegenerate,
                onFeedback = actions.onFeedback
            )
        }
    }
}

/**
 * Shown in place of replacing the content when a regenerate fails, so the result the user was reading
 * survives. The regenerate link immediately below it is the retry affordance.
 */
@Composable
private fun RegenerateError(message: String) {
    InstUIText(
        text = message,
        style = InstUITextTokens.contentSmall,
        color = InstUISemanticColors.Text.error(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .testTag("studyToolRegenerateError")
    )
}

/**
 * Paragraph-shaped placeholder shown while the model works. Generation is slow enough — up to ten
 * seconds for a quiz — that a spinner reads as a stall.
 */
@Composable
private fun StudyToolsSkeleton(modifier: Modifier = Modifier) {
    val generating = stringResource(id = R.string.studyTools_generatingContentDescription)

    Column(
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg, vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
            .semantics {
                contentDescription = generating
                liveRegion = LiveRegionMode.Polite
            }
            .testTag("studyToolsSkeleton")
    ) {
        repeat(SKELETON_LINE_COUNT) { index ->
            // The last line is short so the block reads as a paragraph rather than a block of bars.
            val widthFraction = if (index == SKELETON_LINE_COUNT - 1) 0.5f else 1f
            ShimmerBox(
                baseColor = InstUISemanticColors.Background.muted(),
                highlightColor = InstUISemanticColors.Background.container(),
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(14.dp)
            )
        }
    }
}

private const val SKELETON_LINE_COUNT = 7

@Preview(showBackground = true)
@Composable
private fun StudyToolContentGeneratingPreview() {
    StudyToolContent(state = StudyToolState.Generating, actions = StudyToolsActions())
}

@Preview(showBackground = true)
@Composable
private fun StudyToolContentSummaryPreview() {
    StudyToolContent(
        state = StudyToolState.Ready(StudyToolResult.Summary("This page explains how photosynthesis works.")),
        actions = StudyToolsActions()
    )
}

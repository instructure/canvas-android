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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.instui.compose.text.Text as InstUIText

/**
 * The summary is plain prose — the endpoint's LLM config asks for no markdown and the serializer only
 * trims whitespace — so it renders as plain text with no formatting pipeline.
 */
@Composable
fun SummarizeContent(
    summary: StudyToolResult.Summary,
    modifier: Modifier = Modifier
) {
    InstUIText(
        text = summary.text,
        style = InstUITextTokens.content,
        color = InstUISemanticColors.Text.base(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg, vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
            .testTag("studyToolsSummary")
    )
}

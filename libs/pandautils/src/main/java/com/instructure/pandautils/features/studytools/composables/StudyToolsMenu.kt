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
package com.instructure.pandautils.features.studytools.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.instructure.instui.compose.container.Card
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.component.InstUISharedTokens
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.R
import com.instructure.instui.compose.text.Text as InstUIText

/**
 * The fixed, button-driven entry list. Canvas deliberately has no free-text chat here — the tools
 * the server offers are rendered as three buttons.
 */
@Composable
fun StudyToolsMenu(
    tools: List<StudyToolOption>,
    onToolSelected: (StudyToolOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg, vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
    ) {
        tools.forEach { option ->
            StudyToolButton(option = option, onClick = { onToolSelected(option) })
        }
    }
}

@Composable
private fun StudyToolButton(
    option: StudyToolOption,
    onClick: () -> Unit
) {
    Card(
        backgroundColor = InstUISemanticColors.Background.container(),
        borderColor = InstUISemanticColors.Stroke.base(),
        modifier = Modifier
            .fillMaxWidth()
            // Clipped to the same radius as the Card so the ripple respects its rounded corners
            // instead of spilling out as a rectangle.
            .clip(RoundedCornerShape(InstUISharedTokens.BorderRadius.Card.md))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .testTag("studyTool_${option.tool.kind}")
    ) {
        // The card's own contentPadding (12dp) plus this line's ~24dp height already clears the
        // 48dp touch target, so no extra minHeight is needed here.
        InstUIText(
            text = stringResource(id = option.tool.labelRes()),
            style = InstUITextTokens.contentImportant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Our own localized label for a tool. The server also sends a display label, but it is English-only
 * from the client's point of view, so we key off the stable [StudyTool] instead.
 */
@StringRes
fun StudyTool.labelRes(): Int = when (this) {
    StudyTool.SUMMARIZE -> R.string.studyTools_summarize
    StudyTool.QUIZ -> R.string.studyTools_quizMe
    StudyTool.FLASHCARDS -> R.string.studyTools_flashcards
}

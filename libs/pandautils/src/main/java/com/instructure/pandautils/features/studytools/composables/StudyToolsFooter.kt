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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.token.semantic.InstUILayoutSizes.Spacing.SpaceMd
import com.instructure.instui.token.semantic.InstUILayoutSizes.Spacing.SpaceXs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.studytools.StudyToolsFeedback

/**
 * The row every tool shows under its generated content: a regenerate link plus inline feedback.
 */
@Composable
fun StudyToolsFooter(
    feedback: StudyToolsFeedback?,
    onRegenerate: () -> Unit,
    onFeedback: (StudyToolsFeedback) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceMd.spaceMd, vertical = SpaceXs.spaceXs)
    ) {
        TextButton(
            text = stringResource(id = R.string.studyTools_regenerate),
            onClick = onRegenerate,
            modifier = Modifier.testTag("studyToolsRegenerate")
        )

        Spacer(modifier = Modifier.weight(1f))

        StudyToolsFeedbackRow(selected = feedback, onFeedback = onFeedback)
    }
}

@Preview(showBackground = true)
@Composable
private fun StudyToolsFooterPreview() {
    StudyToolsFooter(feedback = null, onRegenerate = {}, onFeedback = {})
}

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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.instui.compose.text.Text as InstUIText

/**
 * A compact empty/error state for the Study Tools panel — a bottom sheet has far less room than the
 * screens [com.instructure.pandautils.compose.composables.EmptyContent] was designed for, so its
 * 250dp illustration buries the message.
 */
@Composable
fun StudyToolsEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceXl.spaceXl,
                vertical = InstUILayoutSizes.Spacing.SpaceLg.spaceLg
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_panda_nothing_to_see),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceLg.spaceLg))
        title?.let {
            Heading(
                text = it,
                level = HeadingLevel.H4,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg)
            )
            Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceSm.spaceSm))
        }
        InstUIText(
            text = message,
            color = InstUISemanticColors.Text.muted(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg)
        )
        if (buttonText != null && onButtonClick != null) {
            Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceLg.spaceLg))
            TextButton(text = buttonText, onClick = onButtonClick)
        }
    }
}

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

package com.instructure.ngc.features.submission.file

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.plus
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Reusable add-file affordance: a Row with a plus icon and a label,
 * both tinted with [InstUISemanticColors.Text.Interactive.Action.Tertiary.base()].
 *
 * When [interactive] is `true` (the default) the Row is itself a tap target.
 * Set [interactive] to `false` when the Row is embedded inside an already-clickable
 * parent (e.g. [SubmissionFileEmptyState]) so there are no nested clickables.
 *
 * When [enabled] is `false` the affordance is rendered in a muted color and is not
 * clickable, providing a visual disabled state during upload.
 */
@Composable
fun AddFileButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    enabled: Boolean = true,
) {
    val actionColor = if (enabled) {
        InstUISemanticColors.Text.Interactive.Action.Tertiary.base()
    } else {
        InstUISemanticColors.Text.muted()
    }
    val clickModifier = if (interactive && enabled) {
        Modifier
            .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Interactive.base))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
    } else {
        Modifier.padding(vertical = InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
        modifier = modifier.then(clickModifier),
    ) {
        Icon(
            imageVector = InstUIIcons.plus,
            contentDescription = null,
            tint = actionColor,
            size = IconSize.Small,
        )
        Text(
            text = label,
            style = InstUIText.content,
            color = actionColor,
        )
    }
}

// region Previews

@Preview(name = "AddFileButton", showBackground = true)
@Composable
private fun AddFileButtonPreview() {
    InstUITheme {
        AddFileButton(label = "Add file", onClick = {})
    }
}

@Preview(name = "AddFileButton — add another", showBackground = true)
@Composable
private fun AddFileButtonAnotherPreview() {
    InstUITheme {
        AddFileButton(label = "Add another file", onClick = {})
    }
}

// endregion

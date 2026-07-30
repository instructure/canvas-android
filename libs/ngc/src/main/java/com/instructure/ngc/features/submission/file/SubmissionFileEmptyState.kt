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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.cloudUpload
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R

/**
 * Empty-state drop zone shown when no files have been attached yet.
 *
 * The entire card is a single tap target that opens the file picker.  The inner
 * [AddFileButton] row is rendered visually only (non-interactive); the outer Box
 * carries the clickable role so there is one unified touch area with no nested
 * clickables competing for events.
 *
 * When [enabled] is `false` the card is not clickable and renders in a visually
 * muted/disabled state, preventing interaction during an active upload.
 */
@Composable
fun SubmissionFileEmptyState(
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier,
    allowedExtensions: List<String> = emptyList(),
    enabled: Boolean = true,
) {
    val clickableModifier = if (enabled) {
        Modifier.clickable(role = Role.Button, onClickLabel = stringResource(R.string.ngc_submission_addFile), onClick = onAddFile)
    } else {
        Modifier
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.md))
            .border(
                width = InstUILayoutSizes.BorderWidth.Sm.sm,
                color = InstUISemanticColors.Stroke.muted(),
                shape = RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.md),
            )
            .background(InstUISemanticColors.Background.muted())
            .then(clickableModifier)
            .padding(vertical = InstUILayoutSizes.Spacing.SpaceXl.spaceXl),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        ) {
            Icon(
                imageVector = InstUIIcons.cloudUpload,
                contentDescription = null,
                color = IconColor.Muted,
                size = IconSize.XXLarge,
            )
            // Visual-only; the enclosing Box is the single interactive affordance.
            AddFileButton(
                label = stringResource(R.string.ngc_submission_addFile),
                onClick = {},
                interactive = false,
                enabled = enabled,
            )
            AllowedFileTypesText(allowedExtensions)
        }
    }
}

// region Previews

@Preview(name = "FileEmptyState — no restrictions", showBackground = true)
@Composable
private fun FileEmptyStateNoRestrictionsPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            SubmissionFileEmptyState(onAddFile = {})
        }
    }
}

@Preview(name = "FileEmptyState — with allowed types", showBackground = true)
@Composable
private fun FileEmptyStateWithTypesPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            SubmissionFileEmptyState(
                onAddFile = {},
                allowedExtensions = listOf("pdf", "docx", "png"),
            )
        }
    }
}

@Preview(
    name = "FileEmptyState — with allowed types (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FileEmptyStateDarkPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            SubmissionFileEmptyState(
                onAddFile = {},
                allowedExtensions = listOf("pdf", "docx"),
            )
        }
    }
}

// endregion

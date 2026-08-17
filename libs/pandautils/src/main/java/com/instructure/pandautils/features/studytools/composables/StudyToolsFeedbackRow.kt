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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.indicator.Icon as InstUIIcon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.thumbsDown
import com.instructure.instui.token.icon.thumbsDownSolid
import com.instructure.instui.token.icon.thumbsUp
import com.instructure.instui.token.icon.thumbsUpSolid
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.pandautils.R
import com.instructure.pandautils.features.studytools.StudyToolsFeedback

/**
 * Thumbs up / down for a generated result.
 *
 * A vote is a Pendo event only — there is no feedback endpoint — so it never fails and never needs a
 * retry affordance. It also cannot be taken back: the filled icon simply records what was sent.
 */
@Composable
fun StudyToolsFeedbackRow(
    selected: StudyToolsFeedback?,
    onFeedback: (StudyToolsFeedback) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        FeedbackIcon(
            selected = selected == StudyToolsFeedback.LIKED,
            imageVector = InstUIIcons.thumbsUp,
            selectedImageVector = InstUIIcons.thumbsUpSolid,
            contentDescriptionRes = R.string.studyTools_positiveFeedbackContentDescription,
            testTag = "studyToolsFeedbackLiked",
            onClick = { onFeedback(StudyToolsFeedback.LIKED) }
        )

        FeedbackIcon(
            selected = selected == StudyToolsFeedback.DISLIKED,
            imageVector = InstUIIcons.thumbsDown,
            selectedImageVector = InstUIIcons.thumbsDownSolid,
            contentDescriptionRes = R.string.studyTools_negativeFeedbackContentDescription,
            testTag = "studyToolsFeedbackDisliked",
            onClick = { onFeedback(StudyToolsFeedback.DISLIKED) }
        )
    }
}

/**
 * InstUI's `IconButton` only accepts a `@DrawableRes` icon, so it cannot host a vector from
 * [InstUIIcons]. Built from [InstUIIcon] plus a clickable modifier at the 48dp minimum touch target
 * instead, matching the pattern
 * [com.instructure.pandautils.features.studytools.StudyToolsPanel]'s `HeaderIconButton` already uses.
 */
@Composable
private fun FeedbackIcon(
    selected: Boolean,
    imageVector: ImageVector,
    selectedImageVector: ImageVector,
    @StringRes contentDescriptionRes: Int,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(
                minWidth = InstUILayoutSizes.Size.Interactive.height_lg,
                minHeight = InstUILayoutSizes.Size.Interactive.height_lg
            )
            // Clipped to a circle so the ripple matches the round icon instead of the square box.
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                // The vote is announced as a selected state rather than by the icon swap alone, which
                // TalkBack cannot see.
                this.selected = selected
                role = Role.Button
            }
            .testTag(testTag)
    ) {
        InstUIIcon(
            imageVector = if (selected) selectedImageVector else imageVector,
            contentDescription = stringResource(id = contentDescriptionRes),
            size = IconSize.Medium,
            color = IconColor.Muted
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudyToolsFeedbackRowPreview() {
    StudyToolsFeedbackRow(selected = StudyToolsFeedback.LIKED, onFeedback = {})
}

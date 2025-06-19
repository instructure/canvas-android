/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.horizonui.organisms.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState
import com.instructure.pandautils.compose.modifiers.conditional

data class CommentCardState(
    val title: String,
    val date: String,
    val subtitle: String,
    val commentText: String,
    val read: Boolean = false,
    val files: List<FileDropItemState> = emptyList(),
    val fromCurrentUser: Boolean = false
)

@Composable
fun CommentCard(
    state: CommentCardState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .conditional(state.fromCurrentUser) { padding(start = 24.dp) }
            .conditional(!state.fromCurrentUser) { padding(end = 24.dp) }
            .background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level3)
            .border(HorizonBorder.level1(), shape = HorizonCornerRadius.level3)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
        ) {
            Text(text = state.title, style = HorizonTypography.labelLargeBold, modifier = Modifier.padding(end = 16.dp))
            HorizonSpace(SpaceSize.SPACE_2)
            Text(text = state.date, style = HorizonTypography.p2, color = HorizonColors.Text.timestamp())
            HorizonSpace(SpaceSize.SPACE_2)
            Text(text = state.subtitle, style = HorizonTypography.p2, color = HorizonColors.Text.timestamp())
            HorizonSpace(SpaceSize.SPACE_12)
            Text(text = state.commentText, style = HorizonTypography.p1)
            HorizonSpace(SpaceSize.SPACE_12)
            state.files.forEach {
                FileDropItem(it)
                HorizonSpace(SpaceSize.SPACE_8)
            }
        }
        if (!state.read) Badge(
            content = BadgeContent.ColorSmall,
            type = BadgeType.Primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 4.dp, vertical = 6.dp)
        )
    }
}

@Preview
@Composable
fun CommentCardPreview() {
    ContextKeeper.appContext = LocalContext.current
    CommentCard(
        state = CommentCardState(
            title = "John Doe",
            date = "2025-01-01",
            subtitle = "Attempt 1",
            commentText = "This is a sample comment text.",
            read = true,
            files = listOf(
                FileDropItemState.NoLongerEditable(
                    fileName = "example.txt"
                )
            )
        )
    )
}

@Preview
@Composable
fun CommentCardUnreadPreview() {
    ContextKeeper.appContext = LocalContext.current
    CommentCard(
        state = CommentCardState(
            title = "John Doe",
            date = "2025-01-01",
            subtitle = "Attempt 1",
            commentText = "This is a sample comment text.",
            read = false
        )
    )
}
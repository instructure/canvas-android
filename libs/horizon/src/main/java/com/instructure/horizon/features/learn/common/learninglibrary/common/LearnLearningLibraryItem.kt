/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.learn.common.learninglibrary.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.LoadingImage
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState

data class LearnLearningLibraryCollectionItemState(
    val imageUrl: String?,
    val name: String,
    val isBookmarked: Boolean,
    val isCompleted: Boolean,
    val chips: List<LearnLearningLibraryCollectionItemChipState>
)

data class LearnLearningLibraryCollectionItemChipState(
    val label: String,
    val color: StatusChipColor = StatusChipColor.Grey,
    val iconRes: Int? = null
)

@Composable
fun LearnLearningLibraryItem(
    state: LearnLearningLibraryCollectionItemState,
    onClick: (() -> Unit),
    onBookmarkClick: (() -> Unit),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier
        .horizonShadow(HorizonElevation.level4, shape = HorizonCornerRadius.level4)
        .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level4)
        .clickable(onClick = { onClick() })
    ) {
        Column(Modifier.padding(24.dp)) {
            LoadingImage(state.imageUrl)
            HorizonSpace(SpaceSize.SPACE_16)
            Text(
                text = state.name,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.body(),
            )
            HorizonSpace(SpaceSize.SPACE_12)
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    state.chips.forEach { chipState ->
                        StatusChip(
                            StatusChipState(
                                chipState.label,
                                chipState.color,
                                true,
                                chipState.iconRes
                            )
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (state.isCompleted) {
                        Icon(
                            painter = painterResource(R.drawable.check_circle),
                            contentDescription = stringResource(R.string.a11y_learnLearningLibraryItemCompletedContentDescription),
                            tint = HorizonColors.Icon.default()
                        )
                    }

                    AnimatedContent(state.isBookmarked) { isBookmarked ->
                        IconButton(
                            iconRes = if (isBookmarked) R.drawable.bookmark_fill else R.drawable.bookmark,
                            contentDescription = if (isBookmarked)
                                stringResource(R.string.a11y_learnLearningLibraryItemRemoveBookmarkContentDescription)
                            else
                                stringResource(R.string.a11y_learnLearningLibraryItemBookmarkContentDescription),
                            color = IconButtonColor.WhiteGreyOutline,
                            size = IconButtonSize.NORMAL,
                            onClick = { onClick() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun LearningLibraryItemPreview() {
    val state = LearnLearningLibraryCollectionItemState(
        imageUrl = "https://example.com/image.png",
        name = "Example Course",
        isBookmarked = true,
        isCompleted = true,
        chips = listOf(
            LearnLearningLibraryCollectionItemChipState(
                label = "Required",
                color = StatusChipColor.Green,
            ),
            LearnLearningLibraryCollectionItemChipState(
                label = "Completed",
                color = StatusChipColor.Green,
            ),
            LearnLearningLibraryCollectionItemChipState(
                label = "Locked",
                color = StatusChipColor.Grey,
            ),
            LearnLearningLibraryCollectionItemChipState(
                label = "Locked",
                color = StatusChipColor.Grey,
            )
        )
    )

    LearnLearningLibraryItem(state, {}, {})
}
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
package com.instructure.horizon.features.learn.mycontent.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.LoadingImage
import com.instructure.horizon.horizonui.molecules.ProgressBarSmallInline
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState

@Composable
fun LearnMyContentCard(
    cardState: LearnContentCardState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .horizonShadow(HorizonElevation.level4, shape = HorizonCornerRadius.level4)
            .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level4)
            .clickable { navController.navigate(cardState.route) }
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (cardState.imageUrl != null) {
                LoadingImage(
                    cardState.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(HorizonCornerRadius.level4Top),
                )
            }

            HorizonSpace(SpaceSize.SPACE_16)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Text(
                    text = cardState.name,
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Text.title(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (cardState.progress != null) {
                    HorizonSpace(SpaceSize.SPACE_12)
                    ProgressBarSmallInline(cardState.progress)
                }

                HorizonSpace(SpaceSize.SPACE_16)

                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    cardState.cardChips.forEach { chip ->
                        StatusChip(
                            StatusChipState(
                                label = chip.label,
                                color = chip.color,
                                iconRes = chip.iconRes,
                                fill = true,
                            )
                        )
                    }
                }

                if (cardState.buttonLabel != null) {
                    HorizonSpace(SpaceSize.SPACE_16)
                    Button(
                        label = cardState.buttonLabel,
                        height = ButtonHeight.NORMAL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        onClick = { navController.navigate(cardState.route) },
                    )
                }

                HorizonSpace(SpaceSize.SPACE_24)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LearnMyContentProgramCardPreview() {
    LearnMyContentCard(
        cardState = LearnContentCardState(
            name = "Introduction to Programming",
            progress = 45.0,
            route = "",
            cardChips = listOf(
                LearnContentCardChipState(label = "Program", color = StatusChipColor.Violet, iconRes = R.drawable.book_5),
                LearnContentCardChipState(label = "3 courses"),
                LearnContentCardChipState(label = "2 hrs 30 mins"),
                LearnContentCardChipState(label = "01/01/25 – 06/30/25", iconRes = R.drawable.calendar_today),
            ),
            courseNames = listOf("Course A", "Course B", "Course C"),
        ),
        navController = rememberNavController(),
    )
}

@Preview(showBackground = true)
@Composable
private fun LearnMyContentCourseCardPreview() {
    LearnMyContentCard(
        cardState = LearnContentCardState(
            name = "Lorem Ipsum Course Name Here",
            progress = 65.0,
            route = "",
            buttonLabel = "Resume learning",
            cardChips = listOf(
                LearnContentCardChipState(label = "Course", color = StatusChipColor.Institution, iconRes = R.drawable.book_2),
                LearnContentCardChipState(label = "01/01/25 – 06/30/25", iconRes = R.drawable.calendar_today),
            ),
        ),
        navController = rememberNavController(),
    )
}

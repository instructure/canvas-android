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
package com.instructure.horizon.features.learn.mycontent.inprogress

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardChipState
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardState
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentCard
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentUiState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnMyContentInProgressScreen(
    uiState: LearnMyContentUiState<LearnContentCardState>,
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LoadingStateWrapper(uiState.loadingState) {
        LazyColumn(contentPadding = contentPadding) {
            item {
                Text(
                    text = pluralStringResource(R.plurals.learnMyContentItemsCount, uiState.totalItemCount, uiState.totalItemCount),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.dataPoint(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .wrapContentWidth(Alignment.End),
                )
            }
            items(uiState.contentCards) { card ->
                LearnMyContentCard(
                    cardState = card,
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            if (uiState.showMoreButton) {
                item {
                    Button(
                        label = stringResource(R.string.learnMyContentShowMoreLabel),
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.BlackOutline,
                        onClick = uiState.increaseTotalItemCount,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LearnMyContentInProgressScreenPreview() {
    LearnMyContentInProgressScreen(
        uiState = LearnMyContentUiState(
            totalItemCount = 4,
            contentCards = listOf(
                LearnContentCardState(
                    name = "Introduction to Programming",
                    progress = 45.0,
                    route = "",
                    isProgram = true,
                    buttonLabel = null,
                    cardChips = listOf(
                        LearnContentCardChipState(label = "Program", color = StatusChipColor.Violet, iconRes = R.drawable.book_5),
                        LearnContentCardChipState(label = "3 courses"),
                    ),
                ),
                LearnContentCardState(
                    name = "Advanced Data Structures",
                    progress = 10.0,
                    route = "",
                    buttonLabel = "Start learning",
                    cardChips = listOf(
                        LearnContentCardChipState(label = "Course", color = StatusChipColor.Institution, iconRes = R.drawable.book_2),
                    ),
                ),
            )
        ),
        navController = rememberNavController(),
    )
}

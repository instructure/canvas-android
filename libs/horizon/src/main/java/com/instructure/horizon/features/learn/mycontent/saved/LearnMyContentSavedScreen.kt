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
package com.instructure.horizon.features.learn.mycontent.saved

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
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryItem
import com.instructure.horizon.features.learn.learninglibrary.common.LearningLibraryRoute
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
fun LearnMyContentSavedScreen(
    uiState: LearnMyContentUiState<LearnLearningLibraryCollectionItemState>,
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
                LearnLearningLibraryItem(
                    state = card,
                    onClick = {
                        when (val route = card.route) {
                            is LearningLibraryRoute.ObjectRoute -> navController.navigate(route.route)
                            is LearningLibraryRoute.StringRoute -> navController.navigate(route.route)
                            null -> {}
                        }
                    },
                    onBookmarkClick = {},
                    onEnrollClick = {},
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
private fun LearnMyContentSavedScreenPreview() {
    LearnMyContentSavedScreen(
        uiState = LearnMyContentUiState(
            totalItemCount = 2,
            contentCards = listOf(
                LearnLearningLibraryCollectionItemState(
                    id = "1",
                    imageUrl = null,
                    name = "Introduction to Data Science",
                    description = "Learn the fundamentals of data science",
                    isBookmarked = true,
                    bookmarkLoading = false,
                    canEnroll = false,
                    type = CollectionItemType.COURSE,
                    route = null,
                    chips = listOf(LearnLearningLibraryCollectionItemChipState(label = "Course", color = StatusChipColor.Institution)),
                ),
                LearnLearningLibraryCollectionItemState(
                    id = "2",
                    imageUrl = null,
                    name = "Full Stack Development Program",
                    description = "Become a full stack developer",
                    isBookmarked = true,
                    bookmarkLoading = false,
                    canEnroll = true,
                    type = CollectionItemType.PROGRAM,
                    route = null,
                    chips = listOf(LearnLearningLibraryCollectionItemChipState(label = "Program", color = StatusChipColor.Violet)),
                ),
            )
        ),
        navController = rememberNavController(),
    )
}

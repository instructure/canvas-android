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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryItem
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.common.LearningLibraryRoute
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentEmptyMessage
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentUiState
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.LoadingButton
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnMyContentSavedScreen(
    searchQuery: String,
    sortByOption: LearnLearningLibrarySortOption,
    typeFilter: LearnLearningLibraryTypeFilter,
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: LearnMyContentSavedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(searchQuery, sortByOption, typeFilter) {
        viewModel.onFiltersChanged(searchQuery, sortByOption, typeFilter)
    }
    LearnMyContentSavedContent(uiState, navController, contentPadding, viewModel::onBookmarkItem)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnMyContentSavedContent(
    uiState: LearnMyContentUiState<LearnLearningLibraryCollectionItemState>,
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues(),
    onBookmarkClick: (String) -> Unit = {},
) {
    LoadingStateWrapper(uiState.loadingState) {
        LazyColumn(contentPadding = contentPadding) {
            stickyHeader {
                Text(
                    text = pluralStringResource(R.plurals.learnMyContentItemsCount, uiState.totalItemCount, uiState.totalItemCount),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.dataPoint(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HorizonColors.Surface.pagePrimary())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                        .wrapContentWidth(Alignment.End),
                )
            }
            if (uiState.contentCards.isEmpty()) {
                item { LearnMyContentEmptyMessage() }
            }
            items(uiState.contentCards, key = { it.id }) { card ->
                LearnLearningLibraryItem(
                    state = card,
                    onClick = {
                        when (val route = card.route) {
                            is LearningLibraryRoute.ObjectRoute -> navController.navigate(route.route)
                            is LearningLibraryRoute.StringRoute -> navController.navigate(route.route)
                            null -> {}
                        }
                    },
                    onBookmarkClick = { onBookmarkClick(card.id) },
                    onEnrollClick = { navController.navigate(LearnRoute.LearnLearningLibraryEnrollScreen.route(card.id)) },
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .animateItem(),
                )
            }
            if (uiState.showMoreButton) {
                item {
                    LoadingButton(
                        label = stringResource(R.string.learnMyContentShowMoreLabel),
                        loading = uiState.isMoreLoading,
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.BlackOutline,
                        onClick = uiState.increaseTotalItemCount,
                        contentAlignment = Alignment.Center,
                        fixedLoadingSize = true,
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
    LearnMyContentSavedContent(
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

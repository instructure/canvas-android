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
package com.instructure.horizon.features.learn.learninglibrary.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollection
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonBorderShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.modifiers.conditional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnLearningLibraryListScreen(
    state: LearnLearningLibraryListUiState,
    navController: NavHostController
) {
    val scrollState = rememberLazyListState()
    LoadingStateWrapper(state.loadingState) {
        LazyColumn(state = scrollState) {
            stickyHeader {
                LearnLearningLibraryListFilterRow(state, scrollState, navController)
            }

            LearnLearningLibraryCollection(
                state.collections.take(state.itemsToDisplays),
                state.onBookmarkClicked,
                state.onEnrollClicked,
                { route ->
                    route?.let { navController.navigate(route) }
                },
                {
                    navController.navigate(LearnRoute.LearnLearningLibraryDetailsScreen.route(it))
                },
                Modifier.padding(horizontal = 24.dp)
            )

            if (state.collections.size > state.itemsToDisplays) {
                item {
                    Button(
                        label = stringResource(R.string.learningLibraryListShowMoreLabel),
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.BlackOutline,
                        onClick = state.increaseItemsToDisplay,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnLearningLibraryListFilterRow(
    state: LearnLearningLibraryListUiState,
    scrollState: LazyListState,
    navController: NavHostController
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .conditional(scrollState.canScrollBackward) {
                horizonBorderShadow(HorizonColors.LineAndBorder.lineStroke(), bottom = 1.dp)
            }
            .background(HorizonColors.Surface.pagePrimary())
            .padding(24.dp)
    ) {
        LearnSearchBar(
            value = state.searchQuery,
            onValueChange = state.updateSearchQuery,
            placeholder = stringResource(R.string.learnLearningLibraryListSearchPlaceholder),
            modifier = Modifier.weight(1f)
        )
        HorizonSpace(SpaceSize.SPACE_16)
        IconButton(
            iconRes = R.drawable.bookmarks,
            size = IconButtonSize.NORMAL,
            color = IconButtonColor.White,
            elevation = HorizonElevation.level4,
            contentDescription = stringResource(R.string.a11y_learnLearningLibraryBookmarkContentDescription),
            onClick = {
                navController.navigate(LearnRoute.LearnLearningLibraryBookmarkScreen.route())
            }
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.history,
            size = IconButtonSize.NORMAL,
            color = IconButtonColor.White,
            elevation = HorizonElevation.level4,
            contentDescription = stringResource(R.string.a11y_learnLearningLibraryCompletedContentDescription),
            onClick = {
                navController.navigate(LearnRoute.LearnLearningLibraryCompletedScreen.route())
            }
        )
    }
}
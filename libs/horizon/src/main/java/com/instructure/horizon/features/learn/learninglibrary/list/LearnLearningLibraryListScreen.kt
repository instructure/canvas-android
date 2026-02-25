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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollection
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryItem
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryStatusFilter
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonBorderShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.DropdownChip
import com.instructure.horizon.horizonui.molecules.DropdownItem
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
    val collectionScrollState = rememberLazyListState()
    val itemScrollState = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        LearnLearningLibraryListFilterRow(
            state,
            if (state.isEmptyFilter()) collectionScrollState else itemScrollState,
            navController
        )

        if (state.isEmptyFilter()) {
            LearnLearningLibraryCollections(state, collectionScrollState, navController)
        } else {
            LearnLearningLibraryItems(state, itemScrollState, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnLearningLibraryCollections(
    state: LearnLearningLibraryListUiState,
    scrollState: LazyListState,
    navController: NavHostController,
) {
    LoadingStateWrapper(state.collectionState.loadingState) {
        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(top = 16.dp),
            modifier = Modifier.testTag("CollapsableBody")
        ) {
            LearnLearningLibraryCollection(
                state.collectionState.collections.take(state.collectionState.itemsToDisplays),
                state.collectionState.onBookmarkClicked,
                state.collectionState.onEnrollClicked,
                { route ->
                    route?.let { navController.navigate(route) }
                },
                {
                    navController.navigate(LearnRoute.LearnLearningLibraryDetailsScreen.route(it))
                },
                Modifier.padding(horizontal = 24.dp)
            )

            if (state.collectionState.collections.size > state.collectionState.itemsToDisplays) {
                item {
                    Button(
                        label = stringResource(R.string.learningLibraryListShowMoreLabel),
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.BlackOutline,
                        onClick = state.collectionState.increaseItemsToDisplay,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnLearningLibraryItems(
    state: LearnLearningLibraryListUiState,
    scrollState: LazyListState,
    navController: NavHostController,
) {
    LoadingStateWrapper(state.itemState.loadingState) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.testTag("CollapsableBody")
        ) {
            item {
                if (state.itemState.items.isEmpty()) {
                    EmptyMessage()
                }
            }

            items(state.itemState.items) { collectionItemState ->
                LearnLearningLibraryItem(
                    state = collectionItemState,
                    onClick = {
                        collectionItemState.toRoute()?.let { navController.navigate(it) }
                    },
                    onBookmarkClick = {
                        state.itemState.onBookmarkClicked(collectionItemState.id)
                    },
                    onEnrollClick = {
                        state.itemState.onEnrollClicked(collectionItemState.id)
                    },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                )
            }

            if (state.itemState.isMoreButtonLoading) {
                item {
                    Button(
                        label = stringResource(R.string.learningLibraryListShowMoreLabel),
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.BlackOutline,
                        onClick = state.itemState.onShowMoreClicked,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 24.dp)
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
    Column(Modifier
        .fillMaxWidth()
        .conditional(scrollState.canScrollBackward) {
            horizonBorderShadow(HorizonColors.LineAndBorder.lineStroke(), bottom = 1.dp)
        }
        .background(HorizonColors.Surface.pagePrimary())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
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
                    navController.navigate(LearnRoute.LearnLearningLibraryBookmarkScreen.route)
                }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(HorizonColors.Surface.pagePrimary())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            DropdownChip(
                items = LearnLearningLibraryStatusFilter.entries.map {
                    DropdownItem(
                        value = it,
                        label = stringResource(it.labelRes)
                    )
                },
                selectedItem = DropdownItem(
                    value = state.statusFilter,
                    label = stringResource(state.statusFilter.labelRes)
                ),
                onItemSelected = { it?.let { state.updateStatusFilter(it.value) } },
                placeholder = "",
                dropdownWidth = 200.dp,
                verticalPadding = 6.dp
            )

            HorizonSpace(SpaceSize.SPACE_8)

            DropdownChip(
                items = LearnLearningLibraryTypeFilter.entries.map {
                    DropdownItem(
                        value = it,
                        label = stringResource(it.labelRes)
                    )
                },
                selectedItem = DropdownItem(
                    value = state.typeFilter,
                    label = stringResource(state.typeFilter.labelRes)
                ),
                onItemSelected = { it?.let { state.updateTypeFilter(it.value) } },
                placeholder = "",
                dropdownWidth = 200.dp,
                verticalPadding = 6.dp
            )

            if (state.typeFilter != LearnLearningLibraryTypeFilter.All
                || state.statusFilter != LearnLearningLibraryStatusFilter.All
            ) {
                HorizonSpace(SpaceSize.SPACE_8)

                IconButton(
                    iconRes = R.drawable.close,
                    contentDescription = stringResource(R.string.a11y_learningLibraryClearFiltersContentDescription),
                    size = IconButtonSize.SMALL,
                    color = IconButtonColor.Ghost,
                    onClick = {
                        state.updateTypeFilter(LearnLearningLibraryTypeFilter.All)
                        state.updateStatusFilter(LearnLearningLibraryStatusFilter.All)
                    }
                )

                HorizonSpace(SpaceSize.SPACE_16)
            }

            Spacer(Modifier.weight(1f))

            val itemCount = if (state.isEmptyFilter()) {
                state.collectionState.collections.take(state.collectionState.itemsToDisplays).size
            } else {
                state.itemState.items.size
            }
            if (itemCount > 0) {
                Text(
                    text = itemCount.toString(),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.dataPoint()
                )
            }
        }
    }
}

@Composable
private fun EmptyMessage() {
    Text(
        text = stringResource(R.string.learnLearningLibraryItemEmptyMessage),
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}
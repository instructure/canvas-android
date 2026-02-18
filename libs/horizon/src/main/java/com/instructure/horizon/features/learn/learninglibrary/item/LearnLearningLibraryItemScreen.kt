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
package com.instructure.horizon.features.learn.learninglibrary.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryItem
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
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
import com.instructure.horizon.horizonui.organisms.CollapsableHeaderScreen
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.modifiers.conditional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnLearningLibraryItemScreen(
    state: LearnLearningLibraryItemUiState,
    navController: NavHostController
) {
    CollapsableHeaderScreen(
        headerContent = {
            LearnLearningLibraryItemHeader(state, navController)
        },
        bodyContent = {
            LoadingStateWrapper(state.loadingState) {
                LearnLearningLibraryItemContent(state, navController)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnLearningLibraryItemContent(
    state: LearnLearningLibraryItemUiState,
    navController: NavHostController
) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.testTag("CollapsableBody")
    ) {
        stickyHeader {
            LearnLearningLibraryItemContentFilter(state, scrollState)
        }

        item {
            if (state.items.isEmpty()) {
                EmptyMessage()
            }
        }

        items(state.items) { collectionItemState ->
            LearnLearningLibraryItem(
                state = collectionItemState,
                onClick = {
                    collectionItemState.toRoute()?.let { navController.navigate(it) }
                },
                onBookmarkClick = {
                    state.onBookmarkClicked(collectionItemState.id)
                },
                onEnrollClick = {
                    state.onEnrollClicked(collectionItemState.id)
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            )
        }

        if (state.showMoreButton) {
            item {
                Button(
                    label = stringResource(R.string.learningLibraryListShowMoreLabel),
                    height = ButtonHeight.SMALL,
                    width = ButtonWidth.FILL,
                    color = ButtonColor.BlackOutline,
                    onClick = state.onShowMoreClicked,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun LearnLearningLibraryItemContentFilter(
    state: LearnLearningLibraryItemUiState,
    scrollState: LazyListState
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pagePrimary())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .conditional(scrollState.canScrollBackward) {
                horizonBorderShadow(HorizonColors.LineAndBorder.lineStroke(), bottom = 1.dp)
            }
    ) {
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

        Spacer(Modifier.weight(1f))

        Text(
            text = state.items.size.toString(),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.dataPoint()
        )
    }
}

@Composable
private fun LearnLearningLibraryItemHeader(
    state: LearnLearningLibraryItemUiState,
    navController: NavHostController
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            IconButton(
                iconRes = R.drawable.arrow_back,
                contentDescription = stringResource(R.string.a11yNavigateBack),
                color = IconButtonColor.Ghost,
                size = IconButtonSize.SMALL,
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = state.title,
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        }
        LearnSearchBar(
            value = state.searchQuery,
            onValueChange = state.updateSearchQuery,
            placeholder = stringResource(R.string.learnLearningLibraryItemSearchPlaceholder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
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
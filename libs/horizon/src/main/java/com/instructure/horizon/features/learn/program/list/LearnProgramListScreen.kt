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
package com.instructure.horizon.features.learn.program.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonBorderShadow
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.DropdownChip
import com.instructure.horizon.horizonui.molecules.DropdownItem
import com.instructure.horizon.horizonui.molecules.ProgressBarSmallInline
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.horizon.horizonui.organisms.scaffolds.CollapsableHeaderScreen
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.modifiers.conditional

@Composable
fun LearnProgramListScreen(
    state: LearnProgramListUiState,
    navController: NavHostController
) {
    CollapsableHeaderScreen(
        statusBarColor = null,
        headerContent = { paddingValues ->
            Searchbar(state, Modifier.padding(paddingValues))
        },
        bodyContent = { paddingValues ->
            LearnProgramListContent(state, navController, Modifier.padding(paddingValues))
        }
    )
}

@Composable
private fun Searchbar(state: LearnProgramListUiState, modifier: Modifier = Modifier) {
    LearnSearchBar(
        value = state.searchQuery,
        onValueChange = { state.updateSearchQuery(it) },
        placeholder = stringResource(R.string.learnProgramListSearchBarPlaceholder),
        modifier = modifier.padding(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnProgramListContent(
    state: LearnProgramListUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    LoadingStateWrapper(state.loadingState, modifier) {
        val scrollState = rememberLazyListState()
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.testTag("CollapsableBody")
        ) {
            stickyHeader {
                LearnProgramListFilter(state, scrollState)
            }

            if (state.filteredPrograms.isEmpty()) {
                item {
                    EmptyMessage()
                }
            }

            items(state.filteredPrograms.take(state.visibleItemCount)) {
                LearnProgramCard(it, Modifier.padding(horizontal = 24.dp)) {
                    navController.navigate(LearnRoute.LearnProgramDetailsScreen.route(it.programId))
                }
            }

            if (state.filteredPrograms.size > state.visibleItemCount) {
                item {
                    Button(
                        label = stringResource(R.string.learnProgramListShowMoreLabel),
                        width = ButtonWidth.FILL,
                        height = ButtonHeight.SMALL,
                        color = ButtonColor.BlackOutline,
                        onClick = { state.increaseVisibleItemCount() },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnProgramCard(
    program: LearnProgramState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) = {}
) {
    Box(modifier = modifier
        .horizonShadow(HorizonElevation.level4, shape = HorizonCornerRadius.level4)
        .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level4)
        .clickable(onClick = { onClick() })
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = program.programName,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.title(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            HorizonSpace(SpaceSize.SPACE_12)

            ProgressBarSmallInline(
                program.programProgress,
            )

            HorizonSpace(SpaceSize.SPACE_12)

            FlowRow(
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                program.programChips.forEach {
                    StatusChip(
                        StatusChipState(
                            label = it.label,
                            color = StatusChipColor.Grey,
                            fill = true,
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnProgramListFilter(state: LearnProgramListUiState, scrollState: LazyListState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .conditional(scrollState.canScrollBackward) {
                horizonBorderShadow(HorizonColors.LineAndBorder.lineStroke(), bottom = 1.dp)
            }
            .background(HorizonColors.Surface.pagePrimary())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        DropdownChip(
            items = LearnProgramFilterOption.entries.map {
                DropdownItem(it, stringResource(it.labelRes))
            },
            selectedItem = DropdownItem(
                state.selectedFilterValue,
                stringResource(state.selectedFilterValue.labelRes))
            ,
            onItemSelected = { state.updateFilterValue(it?.value ?: LearnProgramFilterOption.All) },
            dropdownWidth = 180.dp,
            verticalPadding = 6.dp,
            placeholder = ""
        )

        Text(
            text = state.filteredPrograms.take(state.visibleItemCount).size.toString()
        )
    }
}

@Composable
private fun EmptyMessage() {
    Text(
        text = stringResource(R.string.learnProgramListEmptyMessage),
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}
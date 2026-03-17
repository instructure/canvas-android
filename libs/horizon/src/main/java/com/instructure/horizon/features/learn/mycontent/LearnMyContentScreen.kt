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
package com.instructure.horizon.features.learn.mycontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.mycontent.completed.LearnMyContentCompletedScreen
import com.instructure.horizon.features.learn.mycontent.completed.LearnMyContentCompletedViewModel
import com.instructure.horizon.features.learn.mycontent.inprogress.LearnMyContentInProgressScreen
import com.instructure.horizon.features.learn.mycontent.inprogress.LearnMyContentInProgressViewModel
import com.instructure.horizon.features.learn.mycontent.saved.LearnMyContentSavedScreen
import com.instructure.horizon.features.learn.mycontent.saved.LearnMyContentSavedViewModel
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.FilterChip
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.organisms.scaffolds.CollapsableHeaderScreen
import kotlinx.coroutines.delay

@Composable
fun LearnMyContentScreen(
    state: LearnMyContentUiState,
    navController: NavHostController,
    inProgressViewModel: LearnMyContentInProgressViewModel = hiltViewModel(),
    completedViewModel: LearnMyContentCompletedViewModel = hiltViewModel(),
    savedViewModel: LearnMyContentSavedViewModel = hiltViewModel(),
) {
    val inProgressUiState by inProgressViewModel.uiState.collectAsState()
    val completedUiState by completedViewModel.uiState.collectAsState()
    val savedUiState by savedViewModel.uiState.collectAsState()

    LaunchedEffect(state.searchQuery.text, state.sortByOption, state.typeFilter) {
        delay(300)
        inProgressViewModel.onFiltersChanged(state.searchQuery.text, state.sortByOption, state.typeFilter)
        completedViewModel.onFiltersChanged(state.searchQuery.text, state.sortByOption, state.typeFilter)
        savedViewModel.onFiltersChanged(state.searchQuery.text, state.sortByOption, state.typeFilter)
    }

    CollapsableHeaderScreen(
        headerContent = { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                LearnMyContentTabSelector(
                    selectedTab = state.selectedTab,
                    onTabSelected = state.onTabSelected,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    LearnSearchBar(
                        state.searchQuery,
                        state.updateSearchQuery,
                        placeholder = stringResource(R.string.learnMyContentSearchLabel),
                        modifier = Modifier.weight(1f),
                    )
                    val filterScreenType = when (state.selectedTab) {
                        LearnMyContentTab.Saved -> LearnLearningLibraryFilterScreenType.MyContentSaved
                        else -> LearnLearningLibraryFilterScreenType.MyContent
                    }
                    IconButton(
                        iconRes = R.drawable.tune,
                        size = IconButtonSize.NORMAL,
                        color = IconButtonColor.White,
                        elevation = HorizonElevation.level4,
                        contentDescription = stringResource(R.string.a11y_learnLearningLibraryFilterContentDescription),
                        onClick = {
                            navController.navigate(
                                LearnRoute.LearnLearningLibraryFilterScreen.route(
                                    screenType = filterScreenType,
                                    typeFilter = state.typeFilter,
                                    sortOption = state.sortByOption,
                                )
                            )
                        },
                        badge = if (state.activeFilterCount > 0) {
                            {
                                Badge(
                                    content = BadgeContent.Text(state.activeFilterCount.toString()),
                                    type = BadgeType.Primary
                                )
                            }
                        } else null
                    )
                }
            }
        },
        bodyContent = { paddingValues ->
            when (state.selectedTab) {
                LearnMyContentTab.InProgress -> LearnMyContentInProgressScreen(inProgressUiState, navController, paddingValues)
                LearnMyContentTab.Completed -> LearnMyContentCompletedScreen(completedUiState, navController, paddingValues)
                LearnMyContentTab.Saved -> LearnMyContentSavedScreen(savedUiState, navController, paddingValues)
            }
        }
    )
}

@Composable
private fun LearnMyContentTabSelector(
    selectedTab: LearnMyContentTab,
    onTabSelected: (LearnMyContentTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LearnMyContentTab.entries.forEach { tab ->
            val tabLabel = when (tab) {
                LearnMyContentTab.InProgress -> stringResource(R.string.LearnMyContentInProgressLabel)
                LearnMyContentTab.Completed -> stringResource(R.string.LearnMyContentCompletedLabel)
                LearnMyContentTab.Saved -> stringResource(R.string.LearnMyContentSavedLabel)
            }
            FilterChip(
                label = tabLabel,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LearnMyContentScreenPreview() {
    LearnMyContentScreen(
        state = LearnMyContentUiState(),
        navController = rememberNavController(),
    )
}

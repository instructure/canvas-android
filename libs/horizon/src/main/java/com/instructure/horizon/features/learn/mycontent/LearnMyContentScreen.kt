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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.instructure.horizon.features.learn.mycontent.completed.LearnMyContentCompletedScreen
import com.instructure.horizon.features.learn.mycontent.completed.LearnMyContentCompletedViewModel
import com.instructure.horizon.features.learn.mycontent.inprogress.LearnMyContentInProgressScreen
import com.instructure.horizon.features.learn.mycontent.inprogress.LearnMyContentInProgressViewModel
import com.instructure.horizon.features.learn.mycontent.saved.LearnMyContentSavedScreen
import com.instructure.horizon.features.learn.mycontent.saved.LearnMyContentSavedViewModel
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.FilterChip
import com.instructure.horizon.horizonui.organisms.scaffolds.CollapsableHeaderScreen

@Composable
fun LearnMyContentScreen(
    state: LearnMyContentUiState,
    navController: NavHostController,
    inProgressViewModel: LearnMyContentInProgressViewModel = hiltViewModel(),
    completedViewModel: LearnMyContentCompletedViewModel = hiltViewModel(),
    savedViewModel: LearnMyContentSavedViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(LearnMyContentTab.InProgress) }

    val inProgressUiState by inProgressViewModel.uiState.collectAsState()
    val completedUiState by completedViewModel.uiState.collectAsState()
    val savedUiState by savedViewModel.uiState.collectAsState()

    LaunchedEffect(state.searchQuery.text, state.sortByOption) {
        inProgressViewModel.onFiltersChanged(state.searchQuery.text, state.sortByOption)
        completedViewModel.onFiltersChanged(state.searchQuery.text, state.sortByOption)
        savedViewModel.onFiltersChanged(state.searchQuery.text, state.sortByOption)
    }

    CollapsableHeaderScreen(
        headerContent = { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                LearnMyContentTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
                HorizonSpace(SpaceSize.SPACE_8)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LearnSearchBar(
                        state.searchQuery,
                        state.updateSearchQuery,
                        placeholder = stringResource(R.string.learnMyContentSearchLabel),
                    )
                }
            }
        },
        bodyContent = { paddingValues ->
            when (selectedTab) {
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

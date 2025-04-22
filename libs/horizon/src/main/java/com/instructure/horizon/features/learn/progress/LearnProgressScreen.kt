/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.cards.ModuleContainer
import com.instructure.horizon.horizonui.organisms.cards.ModuleHeaderState
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCard
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState
import com.instructure.horizon.horizonui.organisms.cards.ModuleStatus
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnProgressScreen(courseId: Long, modifier: Modifier = Modifier, viewModel: LearnProgressViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.loadState(courseId)
    }

    LoadingStateWrapper(state.screenState) {
        LearnProgressContent(
            state,
            modifier
        )
    }
}

@Composable
private fun LearnProgressContent(
    state: LearnProgressUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ){
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.moduleItemStates.values.toList()) { moduleHeaderState ->
                ModuleContainer(
                    state = moduleHeaderState.first,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    moduleHeaderState.second.forEach { moduleItemState ->
                        if (moduleItemState is ModuleItemState.ModuleItemCard) {
                            ModuleItemCard(
                                state = moduleItemState.cardState,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                        if (moduleItemState is ModuleItemState.SubHeader) {
                            ModuleSubHeader(
                                subHeader = moduleItemState.subHeader,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleSubHeader(subHeader: String, modifier: Modifier = Modifier) {
    Text(
        text = subHeader,
        style = HorizonTypography.labelMediumBold,
        color = HorizonColors.Text.body(),
        modifier = modifier
    )
}

@Composable
@Preview
private fun LearnProgressScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnProgressUiState(
        moduleItemStates = mapOf(
            1L to Pair(
                ModuleHeaderState(
                    title = "Module 1",
                    status = ModuleStatus.IN_PROGRESS,
                    expanded = true,
                    subtitle = "Subtitle",
                    itemCount = 5,
                    pastDueCount = 2,
                    remainingMinutes = "30 minutes"
                ),
                listOf(
                    ModuleItemState.ModuleItemCard(
                        cardState = ModuleItemCardState(
                            title = "Assignment 1",
                            learningObjectStatus = LearningObjectStatus.REQUIRED,
                            learningObjectType = LearningObjectType.ASSIGNMENT,
                        )
                    ),
                    ModuleItemState.SubHeader("Subheader")
                )
            )
        )
    )
    LearnProgressContent(state)
}
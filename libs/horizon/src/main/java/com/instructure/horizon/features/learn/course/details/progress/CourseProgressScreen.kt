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
package com.instructure.horizon.features.learn.course.details.progress

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import com.instructure.horizon.navigation.MainNavigationRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseProgressScreen(
    courseId: Long,
    mainNavController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CourseProgressViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var previousCourseId: Long? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(courseId) {
        if (courseId != previousCourseId) {
            previousCourseId = courseId
            viewModel.loadState(courseId)
        }
    }

    LoadingStateWrapper(state.screenState) {
        LearnProgressContent(
            state,
            modifier,
            courseId,
            mainNavController
        )
    }
}

@Composable
private fun LearnProgressContent(
    state: CourseProgressUiState,
    modifier: Modifier = Modifier,
    courseId: Long,
    mainNavController: NavController
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ){
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp, start = 24.dp, end = 24.dp),
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
                                state = moduleItemState.cardState.copy(onClick = {
                                    mainNavController.navigate(MainNavigationRoute.ModuleItemSequence(courseId, moduleItemState.moduleItemId))
                                }),
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
            .padding(top = 16.dp)
    )
}

@Composable
@Preview
private fun LearnProgressScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = CourseProgressUiState(
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
                        moduleItemId = 1,
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
    LearnProgressContent(state, courseId = 1, mainNavController = rememberNavController())
}
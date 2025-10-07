/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.learn.program

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DASHBOARD_REFRESH
import com.instructure.horizon.features.learn.program.components.CourseCardChipState
import com.instructure.horizon.features.learn.program.components.CourseCardStatus
import com.instructure.horizon.features.learn.program.components.ProgramCourseCardState
import com.instructure.horizon.features.learn.program.components.ProgramProgress
import com.instructure.horizon.features.learn.program.components.ProgramProgressItemState
import com.instructure.horizon.features.learn.program.components.ProgramProgressItemStatus
import com.instructure.horizon.features.learn.program.components.ProgramProgressState
import com.instructure.horizon.features.learn.program.components.ProgramsProgressBar
import com.instructure.horizon.features.learn.program.components.SequentialProgramProgressProperties
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.navigation.MainNavigationRoute

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailsScreen(uiState: ProgramDetailsUiState, mainNavController: NavHostController, onCourseSelected: (Long) -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(uiState.navigateToCourseId) {
        uiState.navigateToCourseId?.let { courseId ->
            onCourseSelected(courseId)
            uiState.onNavigateToCourse()
        }
    }

    val homeEntry =
        remember(mainNavController.currentBackStackEntry) { mainNavController.getBackStackEntry(MainNavigationRoute.Home.route) }
    LaunchedEffect(uiState.shouldRefreshDashboard) {
        if (uiState.shouldRefreshDashboard) {
            homeEntry.savedStateHandle[DASHBOARD_REFRESH] = true
            uiState.onDashboardRefreshed()
        }
    }

    LoadingStateWrapper(loadingState = uiState.loadingState) {
        Column(
            modifier = modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HorizonSpace(SpaceSize.SPACE_24)
            if (uiState.showProgressBar) {
                ProgramsProgressBar(
                    uiState.progressBarUiState,
                    progressBarStyle = ProgressBarStyle.WhiteBackground(overrideProgressColor = HorizonColors.Surface.institution())
                )
            }
            HorizonSpace(SpaceSize.SPACE_8)
            Text(text = uiState.description, style = HorizonTypography.p1)
            HorizonSpace(SpaceSize.SPACE_16)
            FlowRow(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.tags.forEach { tag ->
                    StatusChip(
                        StatusChipState(
                            label = tag.name,
                            color = StatusChipColor.White,
                            fill = true,
                            iconRes = tag.iconRes
                        )
                    )
                }
            }
            HorizonSpace(SpaceSize.SPACE_24)
            ProgramProgress(state = uiState.programProgressState)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramDetailsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgramDetailsScreen(
        uiState = ProgramDetailsUiState(
            programName = "Program Name Here",
            progressBarUiState = ProgressBarUiState(progress = 15.0, progressBarStatus = ProgressBarStatus.IN_PROGRESS),
            description = "Learner provider-generated program description At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Guidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi.",
            tags = listOf(
                ProgramDetailTag("Body Text"),
                ProgramDetailTag("Body Text"),
                ProgramDetailTag("Body Text", iconRes = R.drawable.add)
            ),
            programProgressState = ProgramProgressState(
                courses = listOf(
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            courseName = "Sample Course",
                            status = CourseCardStatus.Completed,
                            courseProgress = 0.0,
                            chips = listOf(
                                CourseCardChipState("Body Text"),
                            )
                        ),
                        sequentialProperties = SequentialProgramProgressProperties(
                            status = ProgramProgressItemStatus.Completed,
                            index = 1,
                            first = true,
                            last = false
                        )
                    ),
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            courseName = "Sample Course",
                            status = CourseCardStatus.InProgress,
                            courseProgress = 50.0,
                            chips = listOf(
                                CourseCardChipState("Required"),
                                CourseCardChipState("5 hours 2 mins"),
                                CourseCardChipState("XX/XX/XX – XX/XX/XX", iconRes = R.drawable.calendar_today)
                            )
                        ),
                        sequentialProperties = SequentialProgramProgressProperties(
                            status = ProgramProgressItemStatus.Active,
                            index = 2,
                            first = false,
                            last = false,
                            previousCompleted = true
                        )
                    ),
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            courseName = "Sample Course",
                            status = CourseCardStatus.Inactive,
                            courseProgress = 0.0,
                            chips = listOf(
                                CourseCardChipState("Locked", iconRes = R.drawable.lock),
                                CourseCardChipState("Required"),
                                CourseCardChipState("5 hours 2 mins"),
                            )
                        ),
                        sequentialProperties = SequentialProgramProgressProperties(
                            status = ProgramProgressItemStatus.Upcoming,
                            index = 3,
                            first = false,
                            last = true
                        )
                    )
                )
            )
        ), rememberNavController(), onCourseSelected = {}
    )
}
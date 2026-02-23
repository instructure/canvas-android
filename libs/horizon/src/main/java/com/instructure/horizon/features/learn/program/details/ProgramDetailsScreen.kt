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
package com.instructure.horizon.features.learn.program.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.program.details.components.CourseCardChipState
import com.instructure.horizon.features.learn.program.details.components.CourseCardStatus
import com.instructure.horizon.features.learn.program.details.components.ProgramCourseCardState
import com.instructure.horizon.features.learn.program.details.components.ProgramProgress
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressItemState
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressItemStatus
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressState
import com.instructure.horizon.features.learn.program.details.components.SequentialProgramProgressProperties
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.ProgressBarSmallInline
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.horizon.horizonui.organisms.CollapsableHeaderScreen
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailsScreen(uiState: ProgramDetailsUiState, navController: NavHostController, modifier: Modifier = Modifier) {
    LoadingStateWrapper(loadingState = uiState.loadingState, modifier) {
        CollapsableHeaderScreen(
            headerContent = {
                ProgramDetailsHeader(uiState, navController)
            },
            bodyContent = {
                ProgramDetailsContent(uiState, navController)
            }
        )
    }
}

@Composable
private fun ProgramDetailsHeader(uiState: ProgramDetailsUiState, navController: NavHostController) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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
                text = uiState.programName,
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        }
        HorizonSpace(SpaceSize.SPACE_16)
        if (uiState.showProgressBar) {
            ProgressBarSmallInline(uiState.progressBarUiState.progress)
        }
    }
}

@Composable
private fun ProgramDetailsContent(uiState: ProgramDetailsUiState, navController: NavHostController) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.description.isNotEmpty()) {
            item {
                Column {
                    Text(text = uiState.description, style = HorizonTypography.p1)
                }
            }
        }
        if (uiState.tags.isNotEmpty()) {
            item {
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
            }
        }
        item {
            Column {
                HorizonSpace(SpaceSize.SPACE_8)
                ProgramProgress(uiState.programProgressState, navController)
            }
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
                                CourseCardChipState(
                                    "XX/XX/XX – XX/XX/XX",
                                    iconRes = R.drawable.calendar_today
                                )
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
        ), rememberNavController(),
    )
}
/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.dashboard.widget.forecast

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ShimmerBox
import kotlinx.coroutines.flow.SharedFlow
import java.time.LocalDate
import java.util.Date

@Composable
fun ForecastWidget(
    refreshSignal: SharedFlow<Unit>,
    modifier: Modifier = Modifier
) {
    val viewModel: ForecastWidgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            viewModel.refresh()
        }
    }

    ForecastWidgetContent(
        modifier = modifier,
        uiState = uiState
    )
}

@Composable
fun ForecastWidgetContent(
    modifier: Modifier = Modifier,
    uiState: ForecastWidgetUiState
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.forecastWidgetTitle),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = colorResource(R.color.textDarkest),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.backgroundInfo)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isLoading) {
                    ForecastWidgetLoadingState(
                        weekPeriod = uiState.weekPeriod,
                        selectedSection = uiState.selectedSection,
                        onNavigatePrevious = uiState.onNavigatePrevious,
                        onNavigateNext = uiState.onNavigateNext
                    )
                } else if (uiState.isError) {
                    ForecastWidgetErrorState(
                        onRetry = uiState.onRetry
                    )
                } else {
                    uiState.weekPeriod?.let { weekPeriod ->
                        WeekNavigationHeader(
                            weekPeriod = weekPeriod,
                            onNavigatePrevious = uiState.onNavigatePrevious,
                            onNavigateNext = uiState.onNavigateNext
                        )

                        ForecastSegmentedControl(
                            missingCount = uiState.missingAssignments.size,
                            dueCount = uiState.dueAssignments.size,
                            recentGradesCount = uiState.recentGrades.size,
                            selectedSection = uiState.selectedSection,
                            onSectionSelected = uiState.onSectionSelected
                        )

                        AnimatedVisibility(
                            visible = uiState.selectedSection != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            AnimatedContent(
                                targetState = uiState.selectedSection,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "sectionContent"
                            ) { section ->
                                if (section != null) {
                                    val assignments = when (section) {
                                        ForecastSection.MISSING -> uiState.missingAssignments
                                        ForecastSection.DUE -> uiState.dueAssignments
                                        ForecastSection.RECENT_GRADES -> uiState.recentGrades
                                    }

                                    if (assignments.isEmpty()) {
                                        ForecastWidgetEmptyState(section = section)
                                    } else {
                                        AssignmentList(
                                            assignments = assignments,
                                            onAssignmentClick = uiState.onAssignmentClick
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentList(
    assignments: List<AssignmentItem>,
    onAssignmentClick: (androidx.fragment.app.FragmentActivity, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = colorResource(R.color.backgroundLightest),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        assignments.forEachIndexed { index, assignment ->
            AssignmentListItem(
                assignment = assignment,
                onAssignmentClick = onAssignmentClick
            )

            if (index < assignments.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = colorResource(R.color.borderMedium)
                )
            }
        }
    }
}

@Composable
private fun ForecastWidgetLoadingState(
    weekPeriod: WeekPeriod?,
    selectedSection: ForecastSection?,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Show real week navigation header if weekPeriod is available
        if (weekPeriod != null) {
            WeekNavigationHeader(
                weekPeriod = weekPeriod,
                onNavigatePrevious = onNavigatePrevious,
                onNavigateNext = onNavigateNext
            )
        } else {
            // Week navigation header shimmer
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Segmented control shimmer
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp)
        )

        // If a section is selected, show content shimmer
        if (selectedSection != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = colorResource(R.color.backgroundLightest),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastWidgetErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyContent(
        emptyMessage = stringResource(R.string.forecastWidgetErrorMessage),
        imageRes = R.drawable.ic_panda_space,
        buttonText = stringResource(R.string.retry),
        buttonClick = onRetry,
        modifier = modifier
            .padding(top = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
            .background(
                colorResource(R.color.backgroundLightest),
                shape = RoundedCornerShape(14.dp)
            )
    )
}

@Composable
private fun ForecastWidgetEmptyState(
    section: ForecastSection?,
    modifier: Modifier = Modifier
) {
    val emptyMessage = when (section) {
        ForecastSection.MISSING -> stringResource(R.string.forecastWidgetEmptyMissing)
        ForecastSection.DUE -> stringResource(R.string.forecastWidgetEmptyDue)
        ForecastSection.RECENT_GRADES -> stringResource(R.string.forecastWidgetEmptyRecentGrades)
        null -> ""
    }

    if (emptyMessage.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = colorResource(R.color.backgroundLightest),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = emptyMessage,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = colorResource(R.color.textDark),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun ForecastWidgetContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    ForecastWidgetContent(
        uiState = ForecastWidgetUiState(
            isLoading = false,
            weekPeriod = WeekPeriod(
                startDate = LocalDate.of(2025, 9, 1),
                endDate = LocalDate.of(2025, 9, 7),
                displayText = "1 Sep - 7 Sep",
                weekNumber = 36
            ),
            missingAssignments = listOf(
                AssignmentItem(
                    id = 1,
                    courseId = 101,
                    courseName = "COGS101",
                    assignmentName = "The Mind's Maze: Mapping Cognition",
                    dueDate = Date(),
                    gradedDate = null,
                    pointsPossible = 100.0,
                    weight = 10.0,
                    iconRes = R.drawable.ic_quiz,
                    url = ""
                ),
                AssignmentItem(
                    id = 2,
                    courseId = 204,
                    courseName = "POLI204",
                    assignmentName = "Fix a hyperdrive motivator",
                    dueDate = Date(System.currentTimeMillis() + 86400000),
                    gradedDate = null,
                    pointsPossible = 50.0,
                    weight = null,
                    iconRes = R.drawable.ic_assignment,
                    url = ""
                )
            ),
            dueAssignments = listOf(
                AssignmentItem(
                    id = 3,
                    courseId = 150,
                    courseName = "ENVS150",
                    assignmentName = "Web of Life: Mapping Ecological Interdependence",
                    dueDate = Date(System.currentTimeMillis() + 172800000),
                    gradedDate = null,
                    pointsPossible = 75.0,
                    weight = 15.0,
                    iconRes = R.drawable.ic_assignment,
                    url = ""
                )
            ),
            recentGrades = emptyList(),
            selectedSection = ForecastSection.DUE
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ForecastWidgetLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    ForecastWidgetContent(
        uiState = ForecastWidgetUiState(
            isLoading = true,
            weekPeriod = WeekPeriod(
                startDate = LocalDate.of(2025, 9, 1),
                endDate = LocalDate.of(2025, 9, 7),
                displayText = "1 Sep - 7 Sep",
                weekNumber = 36
            ),
            selectedSection = ForecastSection.RECENT_GRADES
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ForecastWidgetErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ForecastWidgetContent(
        uiState = ForecastWidgetUiState(
            isLoading = false,
            isError = true
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ForecastWidgetEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    ForecastWidgetContent(
        uiState = ForecastWidgetUiState(
            isLoading = false,
            weekPeriod = WeekPeriod(
                startDate = LocalDate.of(2025, 9, 1),
                endDate = LocalDate.of(2025, 9, 7),
                displayText = "1 Sep - 7 Sep",
                weekNumber = 36
            ),
            selectedSection = ForecastSection.MISSING
        )
    )
}
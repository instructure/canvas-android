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

package com.instructure.pandautils.features.dashboard.widget.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.models.courses.CourseCardItem
import com.instructure.pandautils.domain.models.courses.GradeDisplay
import com.instructure.pandautils.domain.models.courses.GroupCardItem
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun CoursesWidget(
    refreshSignal: SharedFlow<Unit>,
    modifier: Modifier = Modifier
) {
    val viewModel: CoursesWidgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            viewModel.refresh()
        }
    }

    CoursesWidgetContent(
        modifier = modifier,
        uiState = uiState
    )
}

@Composable
private fun CoursesWidgetContent(
    modifier: Modifier = Modifier,
    uiState: CoursesWidgetUiState
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (uiState.isLoading) {
            CoursesWidgetLoadingState()
        } else {
            if (uiState.courses.isNotEmpty()) {
                CollapsibleSection(
                    title = stringResource(R.string.courses),
                    count = uiState.courses.size,
                    isExpanded = uiState.isCoursesExpanded,
                    onToggleExpanded = uiState.onToggleCoursesExpanded
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.courses.forEach { course ->
                            CourseCard(
                                courseCard = course,
                                showGrade = uiState.showGrades,
                                onCourseClick = uiState.onCourseClick,
                                onManageOfflineContent = uiState.onManageOfflineContent,
                                onCustomizeCourse = uiState.onCustomizeCourse
                            )
                        }
                    }
                }
            }

            if (uiState.groups.isNotEmpty()) {
                if (uiState.courses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                CollapsibleSection(
                    title = stringResource(R.string.groups),
                    count = uiState.groups.size,
                    isExpanded = uiState.isGroupsExpanded,
                    onToggleExpanded = uiState.onToggleGroupsExpanded
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.groups.forEach { group ->
                            GroupCard(
                                groupCard = group,
                                onGroupClick = uiState.onGroupClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursesWidgetLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            CourseCardShimmer()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoursesWidgetContentPreview() {
    CoursesWidgetContent(
        uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = listOf(
                CourseCardItem(
                    id = 1,
                    name = "Introduction to Computer Science",
                    courseCode = "CS 101",
                    color = 0xFF2196F3.toInt(),
                    imageUrl = null,
                    grade = GradeDisplay.Percentage("85%"),
                    announcementCount = 2,
                    isSynced = true,
                    isClickable = true
                ),
                CourseCardItem(
                    id = 2,
                    name = "Advanced Mathematics",
                    courseCode = "MATH 201",
                    color = 0xFF4CAF50.toInt(),
                    imageUrl = null,
                    grade = GradeDisplay.Letter("A-"),
                    announcementCount = 0,
                    isSynced = false,
                    isClickable = true
                )
            ),
            groups = listOf(
                GroupCardItem(
                    id = 1,
                    name = "Project Team Alpha",
                    parentCourseName = "Introduction to Computer Science",
                    parentCourseId = 1,
                    color = 0xFF4CAF50.toInt(),
                    memberCount = 5
                )
            ),
            isCoursesExpanded = true,
            isGroupsExpanded = true,
            showGrades = true
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun CoursesWidgetLoadingPreview() {
    CoursesWidgetContent(
        uiState = CoursesWidgetUiState(isLoading = true)
    )
}
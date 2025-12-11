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

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.courses.model.CourseCardItem
import com.instructure.pandautils.features.dashboard.widget.courses.model.GradeDisplay
import com.instructure.pandautils.features.dashboard.widget.courses.model.GroupCardItem
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getFragmentActivityOrNull
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun CoursesWidget(
    refreshSignal: SharedFlow<Unit>,
    columns: Int,
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
        uiState = uiState,
        columns = columns
    )
}

@Composable
fun CoursesWidgetContent(
    modifier: Modifier = Modifier,
    uiState: CoursesWidgetUiState,
    columns: Int = 1
) {
    val activity = LocalActivity.current?.getFragmentActivityOrNull()
    Column(modifier = modifier.fillMaxWidth()) {
        if (uiState.isLoading) {
            CoursesWidgetLoadingState(columns = columns)
        } else {
            if (uiState.courses.isNotEmpty()) {
                CollapsibleSection(
                    title = stringResource(R.string.courses),
                    count = uiState.courses.size,
                    isExpanded = uiState.isCoursesExpanded,
                    onToggleExpanded = uiState.onToggleCoursesExpanded
                ) {
                    NonLazyGrid(
                        columns = columns,
                        itemCount = uiState.courses.size,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalSpacing = 12.dp,
                        verticalSpacing = 12.dp
                    ) { index ->
                        val course = uiState.courses[index]
                        CourseCard(
                            courseCard = course,
                            showGrade = uiState.showGrades,
                            showColorOverlay = uiState.showColorOverlay,
                            onCourseClick = uiState.onCourseClick,
                            onManageOfflineContent = uiState.onManageOfflineContent,
                            onCustomizeCourse = uiState.onCustomizeCourse
                        )
                    }
                }
            }

            if (uiState.groups.isNotEmpty()) {

                CollapsibleSection(
                    title = stringResource(R.string.groups),
                    count = uiState.groups.size,
                    isExpanded = uiState.isGroupsExpanded,
                    onToggleExpanded = uiState.onToggleGroupsExpanded
                ) {
                    NonLazyGrid(
                        columns = columns,
                        itemCount = uiState.groups.size,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalSpacing = 12.dp,
                        verticalSpacing = 12.dp
                    ) { index ->
                        val group = uiState.groups[index]
                        GroupCard(
                            groupCard = group,
                            onGroupClick = uiState.onGroupClick
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = { activity?.let { uiState.onAllCourses(it) } },
                    modifier = Modifier
                        .height(24.dp)
                        .padding(start = 16.dp),
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        bottom = 0.dp,
                        start = 10.dp,
                        end = 6.dp
                    ),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(ThemePrefs.buttonColor),
                        contentColor = Color(ThemePrefs.buttonTextColor)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.allCourses),
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_down_small),
                            contentDescription = null,
                            modifier = Modifier.rotate(270f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NonLazyGrid(
    columns: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    content: @Composable (Int) -> Unit
) {
    val rows = (itemCount + columns - 1) / columns
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                repeat(columns) { columnIndex ->
                    val itemIndex = rowIndex * columns + columnIndex
                    Box(modifier = Modifier.weight(1f)) {
                        if (itemIndex < itemCount) {
                            content(itemIndex)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursesWidgetLoadingState(columns: Int = 1) {
    NonLazyGrid(
        columns = columns,
        itemCount = 3,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalSpacing = 12.dp,
        verticalSpacing = 12.dp
    ) {
        CourseCardShimmer()
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun CoursesWidgetContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    CoursesWidgetContent(
        uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = listOf(
                CourseCardItem(
                    id = 1,
                    name = "Introduction to Computer Science",
                    courseCode = "CS 101",
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
                    memberCount = 5
                )
            ),
            isCoursesExpanded = true,
            isGroupsExpanded = true,
            showGrades = true
        )
    )
}

@Preview(widthDp = 1260)
@Preview(
    widthDp = 1260,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124,
    showBackground = true
)
@Composable
private fun CoursesWidgetTabletContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    CoursesWidgetContent(
        columns = 3,
        uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = listOf(
                CourseCardItem(
                    id = 1,
                    name = "Introduction to Computer Science",
                    courseCode = "CS 101",
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
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun CoursesWidgetLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    CoursesWidgetContent(
        uiState = CoursesWidgetUiState(isLoading = true)
    )
}
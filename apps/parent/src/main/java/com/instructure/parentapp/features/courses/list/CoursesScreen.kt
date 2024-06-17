/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.courses.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent


@Composable
internal fun CourseListScreen(
    uiState: CourseListUiState,
    actionHandler: (CourseListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            content = { padding ->
                if (uiState.loadError) {
                    ErrorContent(
                        errorMessage = stringResource(id = R.string.errorLoadingCourses),
                        retryClick = {
                            actionHandler(CourseListAction.Refresh)
                        }, modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.courseListItems.isEmpty() && !uiState.loading) {
                    EmptyContent(
                        emptyTitle = stringResource(id = R.string.parentNoCourses),
                        emptyMessage = stringResource(id = R.string.parentNoCoursesMessage),
                        image = R.drawable.ic_panda_book,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CourseListContent(
                        uiState = uiState,
                        actionHandler = actionHandler,
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    )
                }
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CourseListContent(
    uiState: CourseListUiState,
    actionHandler: (CourseListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.loading,
        onRefresh = {
            actionHandler(CourseListAction.Refresh)
        }
    )

    Box(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (!uiState.loading) {
                items(uiState.courseListItems) {
                    CourseListItem(it, uiState.studentColor, Modifier.clickable {
                        actionHandler(CourseListAction.CourseTapped(it.courseId))
                    })
                }
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.loading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = Color(uiState.studentColor)
        )
    }
}

@Composable
private fun CourseListItem(
    uiState: CourseListItemUiState,
    studentColor: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = uiState.courseName,
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )
        if (uiState.courseCode.isNotEmpty()) {
            Text(
                text = uiState.courseCode,
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp
            )
        }
        if (uiState.grade.isNotEmpty()) {
            Text(
                text = uiState.grade,
                color = Color(studentColor),
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseListPreview() {
    CourseListScreen(
        uiState = CourseListUiState(
            studentColor = android.graphics.Color.RED,
            courseListItems = listOf(
                CourseListItemUiState(1L, "Course 1", "course-1", "A"),
                CourseListItemUiState(2L, "Course 2", "course-2", ""),
                CourseListItemUiState(3L, "Course 3", "", "C")
            )
        ),
        actionHandler = {}
    )
}

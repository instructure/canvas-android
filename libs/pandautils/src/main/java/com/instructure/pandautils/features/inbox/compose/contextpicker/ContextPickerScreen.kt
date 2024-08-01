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
package com.instructure.pandautils.features.inbox.compose.contextpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.inbox.compose.ContextPickerActionHandler
import com.instructure.pandautils.features.inbox.compose.ContextPickerUiState
import com.instructure.pandautils.features.inbox.compose.ScreenState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.backgroundColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContextPickerScreen(
    uiState: ContextPickerUiState,
    actionHandler: (ContextPickerActionHandler) -> Unit,
) {
    val pullToRefreshState = rememberPullRefreshState(refreshing = false, onRefresh = {
        actionHandler(ContextPickerActionHandler.RefreshCalled)
    })

    Scaffold(
        topBar = {
            CanvasAppBar(
                title = if (uiState.groups.isEmpty()) stringResource(R.string.selectCourse) else stringResource(R.string.selectCourseOrGroup),
                navigationActionClick = { actionHandler(ContextPickerActionHandler.DoneClicked) },
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pullRefresh(pullToRefreshState)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize()
                ) {
                    when (uiState.screenState) {
                        is ScreenState.Loading -> {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding)
                                ) {
                                    Loading()
                                }
                            }
                        }
                        is ScreenState.Error -> {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding)
                                ) {
                                    ErrorContent(errorMessage = stringResource(R.string.failedToLoadCoursesAndGroups))
                                }
                            }
                        }
                        is ScreenState.Empty -> {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding)
                                ) {
                                    EmptyContent(
                                        emptyMessage = stringResource(id = R.string.noCourses),
                                        imageRes = R.drawable.ic_panda_nocourses
                                    )
                                }
                            }
                        }
                        is ScreenState.Data -> {
                            if (uiState.courses.isNotEmpty()) {
                                item {
                                    SectionHeaderView(stringResource(R.string.courses))
                                }

                                items(uiState.courses) {
                                    DataRow(it, actionHandler, uiState.selectedContext == it)
                                }
                            }

                            if (uiState.groups.isNotEmpty()) {
                                item {
                                    SectionHeaderView(stringResource(R.string.groups))
                                }

                                items(uiState.groups) {
                                    DataRow(it, actionHandler, uiState.selectedContext == it)
                                }
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = uiState.screenState == ScreenState.Loading,
                    state = pullToRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("pullRefreshIndicator"),
                )
            }
        }
    )
}


@Composable
private fun SectionHeaderView(subTitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colorResource(id = R.color.backgroundLight))
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            subTitle,
            color = colorResource(id = R.color.textDark),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}

@Composable
private fun DataRow(context: CanvasContext, actionHandler: (ContextPickerActionHandler) -> Unit, isSelected: Boolean) {
    val color = if (context.type == CanvasContext.Type.USER) ThemePrefs.brandColor else context.backgroundColor
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colorResource(id = R.color.backgroundLightest))
            .fillMaxWidth()
            .clickable { actionHandler(ContextPickerActionHandler.ContextClicked(context)) }
            .height(50.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(36.dp)
                .background(Color(color), CircleShape)
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_checkmark_lined),
                    contentDescription = null,
                    tint = colorResource(id = R.color.textLightest),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            context.name ?: context.contextId,
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(end = 16.dp)
                .padding(vertical = 8.dp)
        )
    }
}

@Preview
@Composable
fun ContextPickerScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val courses = listOf(
        Course(id = 1, name = "Course 1", courseColor = "#FF0000"),
        Course(id = 1, name = "Course 2", courseColor = "#00FF00"),
        Course(id = 1, name = "Course 3", courseColor = "#0000FF"),
    )
    val groups = listOf(
        Group(id = 1, name = "Group 1"),
        Group(id = 1, name = "Group 2"),
        Group(id = 1, name = "Group 3"),
    )
    ContextPickerScreen(
        uiState = ContextPickerUiState(
            courses = courses,
            groups = groups,
            selectedContext = null,
            screenState = ScreenState.Data
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun ContextPickerScreenCoursesPreview() {
    ContextKeeper.appContext = LocalContext.current
    val courses = listOf(
        Course(id = 1, name = "Course 1", courseColor = "#FF0000"),
        Course(id = 1, name = "Course 2", courseColor = "#00FF00"),
        Course(id = 1, name = "Course 3", courseColor = "#0000FF"),
    )
    ContextPickerScreen(
        uiState = ContextPickerUiState(
            courses = courses,
            groups = emptyList(),
            selectedContext = null,
            screenState = ScreenState.Data
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun ContextPickerScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    ContextPickerScreen(
        uiState = ContextPickerUiState(
            courses = emptyList(),
            groups = emptyList(),
            selectedContext = null,
            screenState = ScreenState.Loading
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun ContextPickerScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ContextPickerScreen(
        uiState = ContextPickerUiState(
            courses = emptyList(),
            groups = emptyList(),
            selectedContext = null,
            screenState = ScreenState.Error
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun ContextPickerScreenEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    ContextPickerScreen(
        uiState = ContextPickerUiState(
            courses = emptyList(),
            groups = emptyList(),
            selectedContext = null,
            screenState = ScreenState.Empty
        ),
        actionHandler = {}
    )
}
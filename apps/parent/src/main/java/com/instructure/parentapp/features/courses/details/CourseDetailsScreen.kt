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

package com.instructure.parentapp.features.courses.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.features.courses.details.frontpage.FrontPageScreen
import com.instructure.parentapp.features.courses.details.grades.ParentGradesScreen
import com.instructure.parentapp.features.courses.details.summary.SummaryScreen
import kotlinx.coroutines.launch


@Composable
internal fun CourseDetailsScreen(
    uiState: CourseDetailsUiState,
    actionHandler: (CourseDetailsAction) -> Unit,
    applyOnWebView: (CanvasWebView.() -> Unit),
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.backgroundLightest)
        ) {
            when {
                uiState.isLoading -> {
                    Loading(
                        color = Color(uiState.studentColor),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("loading"),
                    )
                }

                uiState.isError -> {
                    ErrorContent(
                        errorMessage = stringResource(id = R.string.errorLoadingCourse),
                        retryClick = {
                            actionHandler(CourseDetailsAction.Refresh)
                        }, modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    CourseDetailsScreenContent(
                        uiState = uiState,
                        actionHandler = actionHandler,
                        navigationActionClick = navigationActionClick,
                        applyOnWebView = applyOnWebView,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseDetailsScreenContent(
    uiState: CourseDetailsUiState,
    actionHandler: (CourseDetailsAction) -> Unit,
    navigationActionClick: () -> Unit,
    applyOnWebView: (CanvasWebView.() -> Unit),
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val localCoroutineScope = rememberCoroutineScope()
    uiState.snackbarMessage?.let {
        LaunchedEffect(Unit) {
            localCoroutineScope.launch {
                val result = snackbarHostState.showSnackbar(it)
                if (result == SnackbarResult.Dismissed) {
                    actionHandler(CourseDetailsAction.SnackbarDismissed)
                }
            }
        }
    }
    val pagerState = rememberPagerState { uiState.tabs.size }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            uiState.tabs.getOrNull(page)?.let {
                actionHandler(CourseDetailsAction.CurrentTabChanged(it))
            }
        }
    }

    val tabContents: List<@Composable () -> Unit> = uiState.tabs.map { tabType ->
        when (tabType) {
            TabType.GRADES -> {
                {
                    ParentGradesScreen(
                        navigateToAssignmentDetails = { courseId, assignmentId ->
                            actionHandler(CourseDetailsAction.NavigateToAssignmentDetails(courseId, assignmentId))
                        }
                    )
                }
            }

            TabType.FRONT_PAGE -> {
                {
                    FrontPageScreen(
                        applyOnWebView = applyOnWebView,
                        onLtiButtonPressed = {
                            actionHandler(CourseDetailsAction.OnLtiClicked(it))
                        },
                        showSnackbar = {
                            actionHandler(CourseDetailsAction.ShowSnackbar(it))
                        }
                    )
                }
            }

            TabType.SYLLABUS -> {
                {
                    CourseDetailsWebViewScreen(
                        html = uiState.syllabus,
                        isRefreshing = uiState.isRefreshing,
                        studentColor = uiState.studentColor,
                        onRefresh = {
                            actionHandler(CourseDetailsAction.RefreshCourse)
                        },
                        applyOnWebView = applyOnWebView,
                        onLtiButtonPressed = {
                            actionHandler(CourseDetailsAction.OnLtiClicked(it))
                        }
                    )
                }
            }

            TabType.SUMMARY -> {
                {
                    SummaryScreen(
                        navigateToCalendarEvent = { contextType, contextId, eventId ->
                            actionHandler(CourseDetailsAction.NavigateToCalendarEvent(contextType, contextId, eventId))
                        },
                        navigateToAssignmentDetails = { courseId, assignmentId ->
                            actionHandler(CourseDetailsAction.NavigateToAssignmentDetails(courseId, assignmentId))
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("snackbarHost")
            )
        },
        topBar = {
            CanvasThemedAppBar(
                title = uiState.courseName,
                navigationActionClick = {
                    navigationActionClick()
                },
                backgroundColor = Color(uiState.studentColor),
                contentColor = colorResource(id = R.color.textLightest)
            )
        },
        content = { padding ->
            Column(
                modifier = modifier.padding(padding)
            ) {
                if (tabContents.size > 1) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        contentColor = colorResource(id = R.color.textLightest),
                        backgroundColor = Color(uiState.studentColor),
                        modifier = Modifier
                            .shadow(10.dp)
                            .testTag("courseDetailsTabRow")
                    ) {
                        uiState.tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                selectedContentColor = colorResource(id = R.color.textLightest),
                                unselectedContentColor = colorResource(id = R.color.textLightest),
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(text = stringResource(id = tab.labelRes).uppercase())
                                }
                            )
                        }
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = uiState.tabs.size,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("courseDetailsPager")
                ) { page ->
                    tabContents[page]()
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                backgroundColor = Color(uiState.studentColor),
                onClick = {
                    actionHandler(CourseDetailsAction.SendAMessage)
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chat),
                    tint = Color(ThemePrefs.buttonTextColor),
                    contentDescription = stringResource(id = R.string.courseDetailsMessageContentDescription)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    CourseDetailsScreen(
        uiState = CourseDetailsUiState(
            courseName = "Course Name",
            studentColor = Color.Black.toArgb(),
            isLoading = false,
            isError = false,
            tabs = listOf(
                TabType.SYLLABUS,
                TabType.SUMMARY
            )
        ),
        actionHandler = {},
        applyOnWebView = {},
        navigationActionClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailsScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    CourseDetailsScreen(
        uiState = CourseDetailsUiState(
            studentColor = Color.Black.toArgb(),
            isError = true,
        ),
        actionHandler = {},
        applyOnWebView = {},
        navigationActionClick = {}
    )
}

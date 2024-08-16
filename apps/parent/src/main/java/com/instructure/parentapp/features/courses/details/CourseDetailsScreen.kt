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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.grades.GradesScreen
import com.instructure.pandautils.features.grades.GradesViewModel
import com.instructure.pandautils.utils.ThemePrefs
import kotlinx.coroutines.launch


@Composable
internal fun CourseDetailsScreen(
    uiState: CourseDetailsUiState,
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
                    // Error
                }

                else -> {
                    CourseDetailsScreenContent(
                        uiState = uiState,
                        navigationActionClick = navigationActionClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CourseDetailsScreenContent(
    uiState: CourseDetailsUiState,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { uiState.tabs.size }
    val coroutineScope = rememberCoroutineScope()

    val gradesViewModel: GradesViewModel = viewModel()
    val gradeUiState by remember { gradesViewModel.uiState }.collectAsState()

    val tabContents: List<@Composable () -> Unit> = uiState.tabs.map {
        when (it) {
            TabType.GRADES -> {
                {
                    GradesScreen(gradeUiState, gradesViewModel::handleAction)
                    LaunchedEffect(key1 = Unit) {
                        gradesViewModel.loadGrades(uiState.courseId, false)
                    }
                }
            }

            TabType.FRONT_PAGE -> {
                { FrontPageScreen() }
            }

            TabType.SYLLABUS -> {
                { SyllabusScreen() }
            }

            TabType.SUMMARY -> {
                { SummaryScreen() }
            }
        }
    }

    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CanvasThemedAppBar(
                title = uiState.courseName,
                navigationActionClick = {
                    navigationActionClick()
                },
                backgroundColor = Color(uiState.studentColor)
            )
        },
        content = { padding ->
            Column(
                modifier = modifier.padding(padding)
            ) {
                if (tabContents.size > 1) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        contentColor = Color.White,
                        backgroundColor = Color(uiState.studentColor),
                        modifier = Modifier.shadow(10.dp)
                    ) {
                        uiState.tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
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
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    tabContents[page]()
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                backgroundColor = Color(ThemePrefs.buttonColor),
                onClick = {

                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    tint = Color(ThemePrefs.buttonTextColor),
                    contentDescription = "todo"
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    CourseDetailsScreen(
        uiState = CourseDetailsUiState(
            courseId = 0,
            courseName = "Course Name",
            studentColor = Color.Black.toArgb(),
            isLoading = false,
            isError = false,
            tabs = listOf(
                TabType.GRADES,
                TabType.SYLLABUS,
                TabType.SUMMARY
            )
        ),
        navigationActionClick = {}
    )
}

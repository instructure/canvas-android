/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.calendar.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarEventsPageUiState
import com.instructure.pandautils.features.calendar.CalendarEventsUiState
import com.instructure.pandautils.features.calendar.CalendarScreenUiState
import com.instructure.pandautils.features.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarUiState
import com.instructure.pandautils.features.calendar.EventUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

@ExperimentalFoundationApi
@Composable
fun CalendarScreen(
    title: String,
    calendarScreenUiState: CalendarScreenUiState,
    actionHandler: (CalendarAction) -> Unit,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val localCoroutineScope = rememberCoroutineScope()
        if (calendarScreenUiState.snackbarMessage != null) {
            LaunchedEffect(Unit) {
                localCoroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(calendarScreenUiState.snackbarMessage)
                    if (result == SnackbarResult.Dismissed) {
                        actionHandler(CalendarAction.SnackbarDismissed)
                    }
                }
            }
        }
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                TopAppBar(title = {
                    Text(text = title)
                },
                    actions = {
                        if (calendarScreenUiState.selectedDay != LocalDate.now()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .clickable {
                                    actionHandler(CalendarAction.TodayTapped)
                                }) {
                                Icon(
                                    painterResource(id = R.drawable.ic_calendar_day),
                                    contentDescription = stringResource(id = R.string.a11y_contentDescriptionCalendarJumpToToday),
                                    tint = Color(ThemePrefs.primaryTextColor)
                                )
                                Text(
                                    text = LocalDate.now().dayOfMonth.toString(),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = Color(ThemePrefs.primaryTextColor),
                                )
                            }
                        }
                    },
                    backgroundColor = Color(ThemePrefs.primaryColor),
                    contentColor = Color(ThemePrefs.primaryTextColor),
                    navigationIcon = {
                        IconButton(onClick = navigationActionClick) {
                            Icon(
                                painterResource(id = R.drawable.ic_hamburger),
                                contentDescription = stringResource(id = R.string.navigation_drawer_open)
                            )
                        }

                    })
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { padding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PaddingValues(0.dp, 8.dp, 0.dp, 0.dp)),
                    color = colorResource(id = R.color.backgroundLightest),
                ) {
                    Column {
                        Calendar(calendarScreenUiState, actionHandler, Modifier.fillMaxWidth())
                        CalendarEvents(calendarScreenUiState.calendarEventsUiState, actionHandler)
                    }
                }
            })
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    val calendarStateMapper = CalendarStateMapper()
    val calendarUiState = CalendarUiState(
        headerUiState = calendarStateMapper.createHeaderUiState(LocalDate.now(), null),
        bodyUiState = calendarStateMapper.createBodyUiState(true, false, 0, LocalDate.now(), emptyMap())
    )
    CalendarScreen(
        "Calendar", CalendarScreenUiState(
            LocalDate.now(), true, calendarUiState, CalendarEventsUiState(
                currentPage = CalendarEventsPageUiState(
                    events = listOf(
                        EventUiState(
                            1L,
                            "Course To Do",
                            CanvasContext.defaultCanvasContext(),
                            "Todo 1",
                            R.drawable.ic_assignment
                        ),
                        EventUiState(
                            2L,
                            "Course",
                            CanvasContext.defaultCanvasContext(),
                            "Assignment 1",
                            R.drawable.ic_assignment,
                            "Due Jan 9 at 8:00 AM",
                            "Missing"
                        )
                    )
                )
            )
        ), {}) {}
}
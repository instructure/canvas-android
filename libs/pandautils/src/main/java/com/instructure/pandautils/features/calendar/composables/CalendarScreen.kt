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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ExpandableFabItem
import com.instructure.pandautils.compose.composables.ExpandableFloatingActionButton
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarEventsPageUiState
import com.instructure.pandautils.features.calendar.CalendarEventsUiState
import com.instructure.pandautils.features.calendar.CalendarScreenUiState
import com.instructure.pandautils.features.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarUiState
import com.instructure.pandautils.features.calendar.EventUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate

@ExperimentalFoundationApi
@Composable
fun CalendarScreen(
    title: String,
    calendarScreenUiState: CalendarScreenUiState,
    triggerAccessibilityFocus: Boolean,
    showToolbar: Boolean,
    actionHandler: (CalendarAction) -> Unit,
    navigationActionClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val todayFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
                if (showToolbar) {
                    CanvasThemedAppBar(
                        title = title,
                        actions = {
                            if (calendarScreenUiState.calendarUiState.selectedDay != LocalDate.now()) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .semantics(mergeDescendants = true) { }
                                    .clickable {
                                        actionHandler(CalendarAction.TodayTapped)
                                    }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_calendar_day),
                                        contentDescription = stringResource(
                                            id = R.string.a11y_contentDescriptionCalendarJumpToTodayWithDates,
                                            calendarScreenUiState.calendarUiState.selectedDay,
                                            LocalDate.now()
                                        ),
                                        tint = Color(ThemePrefs.primaryTextColor)
                                    )
                                    Text(
                                        text = LocalDate.now().dayOfMonth.toString(),
                                        fontSize = 9.sp,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clearAndSetSemantics { },
                                        color = Color(ThemePrefs.primaryTextColor),
                                    )
                                }
                            }
                        },
                        navigationActionClick = navigationActionClick,
                        navIconRes = R.drawable.ic_hamburger,
                        navIconContentDescription = stringResource(id = R.string.navigation_drawer_open),
                        modifier = Modifier
                            .focusable()
                            .focusRequester(focusRequester)
                    )
                    // This is needed to trigger accessibility focus on the calendar screen when the tab is selected
                    LaunchedEffect(key1 = triggerAccessibilityFocus, block = {
                        focusRequester.requestFocus()
                    })
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.testTag("snackbarHost")) },
            content = { padding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(PaddingValues(0.dp, 8.dp, 0.dp, 0.dp)),
                    color = colorResource(id = R.color.backgroundLightest),
                ) {
                    Column {
                        Calendar(calendarScreenUiState.calendarUiState, actionHandler, Modifier.fillMaxWidth(), todayFocusRequester)
                        CalendarEvents(calendarScreenUiState.calendarEventsUiState, actionHandler, Modifier.testTag("calendarEvents"))
                    }
                    val todayTapped = calendarScreenUiState.calendarUiState.todayTapped
                    LaunchedEffect(todayTapped) {
                        if (todayTapped) {
                            focusManager.clearFocus(true)
                            delay(200)
                            todayFocusRequester.requestFocus()
                            actionHandler(CalendarAction.TodayTapHandled)
                        }
                    }
                }
            },
            floatingActionButton = {
                val fabExpandedState = remember { mutableStateOf(false) }
                ExpandableFloatingActionButton(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            tint = colorResource(id = R.color.textLightest),
                            contentDescription = stringResource(id = R.string.calendarAddNewCalendarItemContentDescription)
                        )
                    },
                    expanded = fabExpandedState,
                    expandedItems = listOf(
                        {
                            ExpandableFabItem(
                                icon = painterResource(id = R.drawable.ic_todo),
                                text = stringResource(id = R.string.calendarAddToDo),
                                modifier = Modifier
                                    .clickable {
                                        fabExpandedState.value = false
                                        actionHandler(CalendarAction.AddToDoTapped)
                                    }
                                    .semantics { traversalIndex = 1f }
                            )
                        },
                        {
                            ExpandableFabItem(
                                icon = painterResource(id = R.drawable.ic_calendar_month_24),
                                text = stringResource(id = R.string.calendarAddEvent),
                                modifier = Modifier
                                    .clickable {
                                        fabExpandedState.value = false
                                        actionHandler(CalendarAction.AddEventTapped)
                                    }
                                    .semantics { traversalIndex = 2f }
                            )
                        }
                    )
                )
            }
        )
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())
    val calendarUiState = CalendarUiState(
        LocalDate.now(),
        true,
        headerUiState = calendarStateMapper.createHeaderUiState(LocalDate.now(), null),
        bodyUiState = calendarStateMapper.createBodyUiState(true, LocalDate.now(), false, 0, emptyMap())
    )
    CalendarScreen(
        "Calendar", CalendarScreenUiState(
            calendarUiState, CalendarEventsUiState(
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
        ), triggerAccessibilityFocus = false, showToolbar = true, actionHandler = {}) {}
}
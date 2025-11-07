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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarEventsPageUiState
import com.instructure.pandautils.features.calendar.CalendarEventsUiState
import com.instructure.pandautils.features.calendar.EventUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.courseOrUserColor
import com.jakewharton.threetenabp.AndroidThreeTen

private const val PAGE_COUNT = 1000

@ExperimentalFoundationApi
@Composable
fun CalendarEvents(
    calendarEventsUiState: CalendarEventsUiState,
    actionHandler: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var centerIndex by remember { mutableIntStateOf(PAGE_COUNT / 2) }
    val pagerState = rememberPagerState(
        initialPage = PAGE_COUNT / 2,
        initialPageOffsetFraction = 0f
    ) {
        PAGE_COUNT
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val monthOffset = page - centerIndex
            centerIndex = page
            actionHandler(CalendarAction.EventPageChanged(monthOffset))
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = 2,
        reverseLayout = false,
        pageSize = PageSize.Fill,
        pageContent = { page ->
            val settledPage = pagerState.settledPage

            val monthOffset = page - centerIndex
            val calendarEventsPageUiState = when (monthOffset) {
                -1 -> calendarEventsUiState.previousPage
                1 -> calendarEventsUiState.nextPage
                else -> calendarEventsUiState.currentPage
            }

            if (page >= settledPage - 1 && page <= settledPage + 1 && !calendarEventsPageUiState.loading) {
                CalendarEventsPage(
                    calendarEventsPageUiState = calendarEventsPageUiState,
                    actionHandler,
                    modifier = Modifier.testTag("calendarEventsPage$monthOffset")
                )
            } else {
                Loading(modifier = Modifier
                    .fillMaxSize()
                    .testTag("loading$monthOffset"))
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CalendarEventsPage(
    calendarEventsPageUiState: CalendarEventsPageUiState,
    actionHandler: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = calendarEventsPageUiState.refreshing,
        onRefresh = { actionHandler(CalendarAction.PullToRefresh) },
        refreshThreshold = PullRefreshDefaults.RefreshingOffset
    )

    Box(modifier.pullRefresh(pullRefreshState).testTag("calendarEventItemsBox")) {
        if (calendarEventsPageUiState.events.isNotEmpty()) {
            LazyColumn(
                Modifier
                    .fillMaxSize().testTag("calendarEventsList"), verticalArrangement = Arrangement.Top
            ) {
                items(calendarEventsPageUiState.events) {
                    CalendarEventItem(
                        eventUiState = it,
                        onEventClick = { id ->
                            actionHandler(CalendarAction.EventSelected(id))
                        },
                        onCancelReservation = { reservationId ->
                            if (it.appointmentGroupId != null) {
                                actionHandler(CalendarAction.CancelReservation(reservationId, it.appointmentGroupId))
                            }
                        },
                        modifier = Modifier.testTag("calendarEventItem")
                    )
                }
            }
        } else if (calendarEventsPageUiState.error) {
            ErrorContent(
                stringResource(id = R.string.calendarPageError), retryClick = {
                    actionHandler(CalendarAction.Retry)
                }, modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            CalendarEventsEmpty(
                Modifier
                    .fillMaxSize().testTag("calendarEventsEmpty")
            )
        }

        PullRefreshIndicator(
            refreshing = calendarEventsPageUiState.refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = colorResource(id = R.color.white),
        )
    }
}

@Composable
fun CalendarEventItem(
    eventUiState: EventUiState,
    onEventClick: (Long) -> Unit,
    onCancelReservation: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val contextColor = Color(eventUiState.canvasContext.courseOrUserColor)
    Row(
        modifier
            .clickable { onEventClick(eventUiState.plannableId) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .semantics {
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            painter = painterResource(id = eventUiState.iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contextColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eventUiState.contextName,
                fontSize = 14.sp,
                color = contextColor,
                modifier = Modifier.padding(vertical = 1.dp).testTag("contextName")
            )
            Text(
                text = eventUiState.name,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier.padding(vertical = 1.dp).testTag("eventTitle")
            )
            if (!eventUiState.tag.isNullOrEmpty()) Text(
                text = eventUiState.tag,
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.padding(vertical = 1.dp).testTag("eventTag")
            )
            if (eventUiState.date != null) Text(
                text = eventUiState.date,
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.padding(vertical = 1.dp).testTag("eventDate")
            )
            if (eventUiState.status != null) Text(
                text = eventUiState.status,
                fontSize = 14.sp,
                color = Color(ThemePrefs.brandColor),
                modifier = Modifier.padding(vertical = 1.dp).testTag("eventStatus")
            )
        }
        if (eventUiState.canCancel && onCancelReservation != null && eventUiState.reservationId != null) {
            IconButton(
                onClick = { onCancelReservation(eventUiState.reservationId) },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .semantics {
                        role = Role.Button
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(R.string.a11y_cancelReservation),
                    tint = colorResource(id = R.color.textDanger)
                )
            }
        }
    }
}

@Composable
fun CalendarEventsEmpty(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_no_events),
            tint = Color.Unspecified,
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.calendarNoEvents),
            fontSize = 22.sp,
            color = colorResource(
                id = R.color.textDarkest
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 68.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.calendarNoEventsDescription),
            fontSize = 16.sp,
            color = colorResource(
                id = R.color.textDarkest
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 68.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun CalendarEventsPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    CalendarEvents(
        calendarEventsUiState = CalendarEventsUiState(
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
                    ),
                    EventUiState(
                        2L,
                        "Course",
                        CanvasContext.defaultCanvasContext(),
                        "Assignment 1",
                        R.drawable.ic_assignment,
                        "Due Jan 9 at 8:00 AM",
                        "Missing",
                        tag = "Additional replies (2)"
                    ),
                    EventUiState(
                        3L,
                        "Course",
                        CanvasContext.defaultCanvasContext(),
                        "Appointment Slot",
                        R.drawable.ic_assignment,
                        "Jan 15 at 2:00 PM",
                        "Reserved",
                        isReservation = true,
                        reservationId = 3L,
                        appointmentGroupId = 100L,
                        canCancel = true
                    )
                )
            )
        ), actionHandler = {})
}
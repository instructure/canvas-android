/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.dashboard.widget.todo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.instructure.pandautils.compose.composables.calendar.CalendarBody
import com.instructure.pandautils.compose.composables.calendar.CalendarBodyUiState
import org.threeten.bp.LocalDate

private const val PAGE_COUNT = 1000

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekCalendarView(
    calendarBodyUiState: CalendarBodyUiState,
    selectedDay: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    onPageChanged: (Int) -> Unit,
    scrollToPageOffset: Int,
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
            val weekOffset = page - centerIndex
            centerIndex = page
            onPageChanged(weekOffset)
        }
    }

    LaunchedEffect(scrollToPageOffset) {
        if (scrollToPageOffset != 0) {
            pagerState.animateScrollToPage(pagerState.currentPage + scrollToPageOffset)
        }
    }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = 2,
        reverseLayout = false,
        pageSize = PageSize.Fill,
        pageContent = { page ->
            val settledPage = pagerState.settledPage
            val weekOffset = page - centerIndex
            val calendarPageUiState = when (weekOffset) {
                -1 -> calendarBodyUiState.previousPage
                1 -> calendarBodyUiState.nextPage
                else -> calendarBodyUiState.currentPage
            }

            if (page >= settledPage - 1 && page <= settledPage + 1) {
                CalendarBody(
                    calendarRows = calendarPageUiState.calendarRows,
                    selectedDay = selectedDay,
                    selectedDayChanged = onDaySelected,
                    scaleRatio = 0f,
                    modifier = Modifier
                )
            }
        }
    )
}
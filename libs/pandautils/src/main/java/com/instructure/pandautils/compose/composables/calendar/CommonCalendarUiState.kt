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
package com.instructure.pandautils.compose.composables.calendar

import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Locale

data class CalendarHeaderUiState(val monthTitle: String, val yearTitle: String, val loadingMonths: Boolean = false)

data class CalendarBodyUiState(
    val previousPage: CalendarPageUiState,
    val currentPage: CalendarPageUiState,
    val nextPage: CalendarPageUiState
)

data class CalendarPageUiState(
    val calendarRows: List<CalendarRowUiState>,
    val buttonContentDescription: String
)

data class CalendarRowUiState(val days: List<CalendarDayUiState>)

data class CalendarDayUiState(
    val dayNumber: Int,
    val date: LocalDate = LocalDate.now(),
    val enabled: Boolean = true,
    val indicatorCount: Int = 0,
) {
    val today: Boolean
        get() {
            val today = LocalDate.now()
            return date.isEqual(today)
        }

    val contentDescription: String = date.let {
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val day = date.dayOfMonth
        "$dayOfWeek, $month $day"
    }
}
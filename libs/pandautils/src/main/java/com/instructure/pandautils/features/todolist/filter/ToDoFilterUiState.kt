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
package com.instructure.pandautils.features.todolist.filter

import com.instructure.pandautils.R
import com.instructure.pandautils.utils.getSystemLocaleCalendar
import java.util.Calendar
import java.util.Date

data class ToDoFilterUiState(
    val checkboxItems: List<FilterCheckboxItem> = emptyList(),
    val pastDateOptions: List<DateRangeOption> = emptyList(),
    val selectedPastOption: DateRangeSelection = DateRangeSelection.ONE_WEEK,
    val futureDateOptions: List<DateRangeOption> = emptyList(),
    val selectedFutureOption: DateRangeSelection = DateRangeSelection.ONE_WEEK,
    val shouldCloseAndApplyFilters: Boolean = false,
    val areDateFiltersChanged: Boolean = false,
    val onPastDaysChanged: (DateRangeSelection) -> Unit = {},
    val onFutureDaysChanged: (DateRangeSelection) -> Unit = {},
    val onDone: () -> Unit = {},
    val onFiltersApplied: () -> Unit = {}
)

data class FilterCheckboxItem(
    val titleRes: Int,
    val checked: Boolean,
    val onToggle: (Boolean) -> Unit
)

data class DateRangeOption(
    val selection: DateRangeSelection,
    val labelText: String,
    val dateText: String
)

enum class DateRangeSelection(val pastLabelResId: Int, val futureLabelResId: Int) {
    TODAY(R.string.todoFilterToday, R.string.todoFilterToday),
    THIS_WEEK(R.string.todoFilterThisWeek, R.string.todoFilterThisWeek),
    ONE_WEEK(R.string.todoFilterLastWeek, R.string.todoFilterNextWeek),
    TWO_WEEKS(R.string.todoFilterTwoWeeks, R.string.todoFilterInTwoWeeks),
    THREE_WEEKS(R.string.todoFilterThreeWeeks, R.string.todoFilterInThreeWeeks),
    FOUR_WEEKS(R.string.todoFilterFourWeeks, R.string.todoFilterInFourWeeks);

    fun calculatePastDateRange(): Date {
        val calendar = getSystemLocaleCalendar().apply { time = Date() }

        val weeksToAdd = when (this) {
            TODAY -> return calendar.apply { setStartOfDay() }.time
            THIS_WEEK -> 0
            ONE_WEEK -> -1
            TWO_WEEKS -> -2
            THREE_WEEKS -> -3
            FOUR_WEEKS -> -4
        }

        return calendar.apply {
            add(Calendar.WEEK_OF_YEAR, weeksToAdd)
            set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            setStartOfDay()
        }.time
    }

    fun calculateFutureDateRange(): Date {
        val calendar = getSystemLocaleCalendar().apply { time = Date() }

        val weeksToAdd = when (this) {
            TODAY -> return calendar.apply { setEndOfDay() }.time
            THIS_WEEK -> 0
            ONE_WEEK -> 1
            TWO_WEEKS -> 2
            THREE_WEEKS -> 3
            FOUR_WEEKS -> 4
        }

        return calendar.apply {
            add(Calendar.WEEK_OF_YEAR, weeksToAdd)
            set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            add(Calendar.DAY_OF_YEAR, 6)
            setEndOfDay()
        }.time
    }
}

private fun Calendar.setStartOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

private fun Calendar.setEndOfDay() {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}
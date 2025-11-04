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

data class ToDoFilterUiState(
    val checkboxItems: List<FilterCheckboxItem> = emptyList(),
    val pastDateOptions: List<DateRangeOption> = emptyList(),
    val selectedPastOption: DateRangeSelection = DateRangeSelection.ONE_WEEK,
    val futureDateOptions: List<DateRangeOption> = emptyList(),
    val selectedFutureOption: DateRangeSelection = DateRangeSelection.ONE_WEEK,
    val onPastDaysChanged: (DateRangeSelection) -> Unit = {},
    val onFutureDaysChanged: (DateRangeSelection) -> Unit = {},
    val onDone: () -> Unit = {},
    val onDismiss: () -> Unit = {}
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
    FOUR_WEEKS(R.string.todoFilterFourWeeks, R.string.todoFilterInFourWeeks)
}
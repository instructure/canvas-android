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
package com.instructure.pandautils.features.calendar.filter

import androidx.annotation.ColorInt

data class CalendarFilterScreenUiState(
    val users: List<CalendarFilterItemUiState> = emptyList(),
    val courses: List<CalendarFilterItemUiState> = emptyList(),
    val groups: List<CalendarFilterItemUiState> = emptyList(),
    val error: Boolean = false,
    val loading: Boolean = false,
    val selectAllAvailable: Boolean = false,
    val explanationMessage: String? = null,
    val snackbarMessage: String? = null
) {
    val anyFiltersSelected: Boolean
        get() = users.any { it.selected } || courses.any { it.selected } || groups.any { it.selected }
}

data class CalendarFilterItemUiState(
    val contextId: String,
    val name: String,
    val selected: Boolean,
    @ColorInt val color: Int
)

sealed class CalendarFilterAction {
    data class ToggleFilter(val contextId: String) : CalendarFilterAction()
    data object Retry : CalendarFilterAction()
    data object SnackbarDismissed : CalendarFilterAction()
    data object SelectAll : CalendarFilterAction()
    data object DeselectAll : CalendarFilterAction()
}

sealed class CalendarFilterViewModelAction {
    data class FiltersClosed(val changed: Boolean) : CalendarFilterViewModelAction()
}
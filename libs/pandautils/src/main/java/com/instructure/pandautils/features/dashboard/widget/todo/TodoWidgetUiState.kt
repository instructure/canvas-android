/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.todo

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.compose.composables.calendar.CalendarBodyUiState
import com.instructure.pandautils.compose.composables.todo.ToDoItemUiState
import org.threeten.bp.LocalDate

data class TodoWidgetUiState(
    val todosLoading: Boolean = false,
    val todosError: Boolean = false,
    val todos: List<ToDoItemUiState> = emptyList(),
    val selectedDay: LocalDate = LocalDate.now(),
    val calendarBodyUiState: CalendarBodyUiState? = null,
    val showCompleted: Boolean = false,
    val monthTitle: String = "",
    val scrollToPageOffset: Int = 0,
    val onTodoClick: (FragmentActivity, Long) -> Unit = { _, _ -> },
    val onDaySelected: (LocalDate) -> Unit = {},
    val onPageChanged: (Int) -> Unit = {},
    val onNavigateWeek: (Int) -> Unit = {},
    val onToggleShowCompleted: () -> Unit = {}
)
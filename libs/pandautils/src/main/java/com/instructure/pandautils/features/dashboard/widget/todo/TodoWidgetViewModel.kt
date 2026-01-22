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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.ToDo
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.features.dashboard.widget.todo.model.TodoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TodoWidgetViewModel @Inject constructor(
    private val todoWidgetBehavior: TodoWidgetBehavior,
    private val calendarStateMapper: CalendarStateMapper,
    clock: Clock
) : ViewModel() {

    private var todos: List<ToDo> = emptyList()
    private var selectedDay = LocalDate.now(clock)
    private var showCompleted = false

    private val _uiState = MutableStateFlow(
        TodoWidgetUiState(
            selectedDay = selectedDay,
            calendarBodyUiState = createCalendarBodyUiState(),
            monthTitle = getMonthTitle(),
            showCompleted = showCompleted,
            scrollToPageOffset = 0,
            onTodoClick = ::onTodoClick,
            onDaySelected = ::onDaySelected,
            onPageChanged = ::onPageChanged,
            onNavigateWeek = ::onNavigateWeek,
            onToggleShowCompleted = ::toggleShowCompleted
        )
    )
    val uiState: StateFlow<TodoWidgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun onTodoClick(activity: FragmentActivity, todoId: Long) {
        val todo = todos.find { it.id == todoId } ?: return
        todoWidgetBehavior.onTodoClick(activity, todo)
    }

    fun refresh() {
        loadData(forceRefresh = true)
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(todosLoading = true, todosError = false) }

            try {
                val todoItems = emptyList<TodoItem>()

                _uiState.update {
                    it.copy(
                        todosLoading = false,
                        todos = todoItems
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        todosLoading = false,
                        todosError = true
                    )
                }
            }
        }
    }

    private fun toggleShowCompleted() {
        showCompleted = !showCompleted
        _uiState.update {
            it.copy(showCompleted = showCompleted)
        }
    }

    private fun onDaySelected(day: LocalDate) {
        selectedDay = day
        _uiState.update {
            it.copy(
                selectedDay = day,
                calendarBodyUiState = createCalendarBodyUiState(),
                monthTitle = getMonthTitle()
            )
        }
    }

    private fun onNavigateWeek(offset: Int) {
        selectedDay = selectedDay.plus(offset.toLong(), ChronoUnit.WEEKS)
        _uiState.update {
            it.copy(
                selectedDay = selectedDay,
                calendarBodyUiState = createCalendarBodyUiState(),
                monthTitle = getMonthTitle(),
                scrollToPageOffset = offset
            )
        }
    }

    private fun onPageChanged(offset: Int) {
        if (offset != 0) {
            selectedDay = selectedDay.plus(offset.toLong(), ChronoUnit.WEEKS)
            _uiState.update {
                it.copy(
                    selectedDay = selectedDay,
                    calendarBodyUiState = createCalendarBodyUiState(),
                    monthTitle = getMonthTitle(),
                    scrollToPageOffset = 0
                )
            }
        }
    }

    private fun createCalendarBodyUiState() = calendarStateMapper.createBodyUiState(
        expanded = false,
        selectedDay = selectedDay,
        jumpToToday = false,
        scrollToPageOffset = 0,
        eventIndicators = emptyMap()
    )

    private fun getMonthTitle(): String {
        return selectedDay.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
}
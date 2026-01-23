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
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.toApiStringSafe
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.domain.usecase.planner.LoadPlannerItemsUseCase
import com.instructure.pandautils.features.dashboard.widget.todo.model.TodoItem
import com.instructure.pandautils.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TodoWidgetViewModel @Inject constructor(
    private val todoWidgetBehavior: TodoWidgetBehavior,
    private val calendarStateMapper: CalendarStateMapper,
    private val loadPlannerItemsUseCase: LoadPlannerItemsUseCase,
    clock: Clock
) : ViewModel() {

    private var todos: List<ToDo> = emptyList()
    private var selectedDay = LocalDate.now(clock)
    private var showCompleted = false

    private val eventsByDay = mutableMapOf<LocalDate, MutableList<PlannerItem>>()
    private val loadingDays = mutableSetOf<LocalDate>()
    private val errorDays = mutableSetOf<LocalDate>()
    private val refreshingDays = mutableSetOf<LocalDate>()
    private val loadedWeeks = mutableSetOf<LocalDate>()

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
        loadVisibleWeeks()
    }

    private fun onTodoClick(activity: FragmentActivity, todoId: Long) {
        val todo = todos.find { it.id == todoId } ?: return
        todoWidgetBehavior.onTodoClick(activity, todo)
    }

    private fun toggleShowCompleted() {
        showCompleted = !showCompleted
        _uiState.update { createNewUiState() }
    }

    private fun onDaySelected(day: LocalDate) {
        selectedDay = day
        _uiState.update { createNewUiState() }
    }

    private fun onNavigateWeek(offset: Int) {
        _uiState.update { it.copy(scrollToPageOffset = offset) }
    }

    private fun onPageChanged(offset: Int) {
        if (offset != 0) {
            selectedDay = selectedDay.plus(offset.toLong(), ChronoUnit.WEEKS)
            _uiState.update { createNewUiState().copy(scrollToPageOffset = 0) }

            viewModelScope.launch {
                val weekField = WeekFields.of(Locale.getDefault())
                val newWeekStart = selectedDay.with(weekField.dayOfWeek(), 1)
                val adjacentWeekStart = if (offset > 0) {
                    newWeekStart.plusWeeks(1)
                } else {
                    newWeekStart.minusWeeks(1)
                }
                loadEventsForWeek(adjacentWeekStart)
            }
        }
    }

    private fun createNewUiState(): TodoWidgetUiState {
        val todoItems = createTodoItemsForSelectedDay()
        val eventIndicators = createEventIndicators()

        return _uiState.value.copy(
            todos = todoItems,
            calendarBodyUiState = calendarStateMapper.createBodyUiState(
                expanded = false,
                selectedDay = selectedDay,
                jumpToToday = false,
                scrollToPageOffset = 0,
                eventIndicators = eventIndicators
            ),
            monthTitle = getMonthTitle(),
            selectedDay = selectedDay,
            showCompleted = showCompleted,
            todosLoading = loadingDays.contains(selectedDay),
            todosError = errorDays.contains(selectedDay)
        )
    }

    private fun createTodoItemsForSelectedDay(): List<TodoItem> {
        // TODO: Implement mapping from PlannerItem to TodoItem
        // Filter by selectedDay
        // Filter by showCompleted
        return emptyList()
    }

    private fun createEventIndicators(): Map<LocalDate, Int> {
        return eventsByDay.mapValues { (_, plannerItems) ->
            minOf(3, plannerItems.size)
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

    private fun loadVisibleWeeks() {
        viewModelScope.launch {
            val weekField = WeekFields.of(Locale.getDefault())
            val currentWeekStart = selectedDay.with(weekField.dayOfWeek(), 1)

            val loadedStates = listOf(
                async { loadEventsForWeek(currentWeekStart.minusWeeks(1)) },
                async { loadEventsForWeek(currentWeekStart) },
                async { loadEventsForWeek(currentWeekStart.plusWeeks(1)) }
            ).awaitAll()

            if (loadedStates.all { it }) {
                _uiState.update { createNewUiState() }
            }
        }
    }

    private suspend fun loadEventsForWeek(date: LocalDate, refresh: Boolean = false): Boolean {
        val weekField = WeekFields.of(Locale.getDefault())
        val weekStart = date.with(weekField.dayOfWeek(), 1)

        if (!refresh && loadedWeeks.contains(weekStart)) {
            return true
        }

        val weekEnd = weekStart.plusDays(6)
        val daysInWeek = daysBetweenDates(
            weekStart.atStartOfDay(),
            weekEnd.atTime(23, 59, 59)
        )

        if (refresh) {
            refreshingDays.addAll(daysInWeek)
        } else {
            loadingDays.addAll(daysInWeek)
        }
        errorDays.removeAll(daysInWeek)
        _uiState.update { createNewUiState() }

        return try {
            val result = loadPlannerItemsUseCase(
                startDate = weekStart.atStartOfDay().toApiStringSafe(),
                endDate = weekEnd.atTime(23, 59, 59).toApiStringSafe(),
                forceNetwork = refresh
            )

            if (refresh) {
                refreshingDays.removeAll(daysInWeek)
            } else {
                loadingDays.removeAll(daysInWeek)
            }

            if (refresh) {
                eventsByDay.clear()
                loadedWeeks.clear()
                loadedWeeks.add(weekStart)
            }

            loadedWeeks.add(weekStart)
            storeResults(result)
            _uiState.update { createNewUiState() }
            true
        } catch (e: Exception) {
            if (refresh) {
                refreshingDays.removeAll(daysInWeek)
            } else {
                loadedWeeks.remove(weekStart)
                loadingDays.removeAll(daysInWeek)
                errorDays.addAll(daysInWeek)
            }
            _uiState.update { createNewUiState() }
            false
        }
    }

    private fun daysBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Set<LocalDate> {
        val days = mutableSetOf<LocalDate>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            days.add(current.toLocalDate())
            current = current.plusDays(1)
        }
        return days
    }

    private fun storeResults(result: List<PlannerItem>, dateToClear: LocalDate? = null) {
        if (dateToClear != null) {
            eventsByDay[dateToClear]?.clear()
        }

        result.forEach { plannerItem ->
            val date = plannerItem.plannableDate.toLocalDate()
            eventsByDay.getOrPut(date) { mutableListOf() }.add(plannerItem)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val weekField = WeekFields.of(Locale.getDefault())
            val currentWeekStart = selectedDay.with(weekField.dayOfWeek(), 1)

            val loadedStates = listOf(
                async { loadEventsForWeek(currentWeekStart.minusWeeks(1), refresh = true) },
                async { loadEventsForWeek(currentWeekStart, refresh = true) },
                async { loadEventsForWeek(currentWeekStart.plusWeeks(1), refresh = true) }
            ).awaitAll()

            if (loadedStates.all { it }) {
                _uiState.update { createNewUiState() }
            }
        }
    }
}
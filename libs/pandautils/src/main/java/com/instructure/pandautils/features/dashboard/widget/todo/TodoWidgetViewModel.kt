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

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.toApiStringSafe
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.compose.composables.todo.ToDoItemUiState
import com.instructure.pandautils.compose.composables.todo.ToDoStateMapper
import com.instructure.pandautils.domain.usecase.courses.LoadAvailableCoursesParams
import com.instructure.pandautils.domain.usecase.courses.LoadAvailableCoursesUseCase
import com.instructure.pandautils.domain.usecase.planner.CreatePlannerOverrideParams
import com.instructure.pandautils.domain.usecase.planner.CreatePlannerOverrideUseCase
import com.instructure.pandautils.domain.usecase.planner.LoadPlannerItemsUseCase
import com.instructure.pandautils.domain.usecase.planner.UpdatePlannerOverrideParams
import com.instructure.pandautils.domain.usecase.planner.UpdatePlannerOverrideUseCase
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.isComplete
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    @ApplicationContext private val context: Context,
    private val todoWidgetBehavior: TodoWidgetBehavior,
    private val calendarStateMapper: CalendarStateMapper,
    private val toDoStateMapper: ToDoStateMapper,
    private val loadPlannerItemsUseCase: LoadPlannerItemsUseCase,
    private val loadAvailableCoursesUseCase: LoadAvailableCoursesUseCase,
    private val updatePlannerOverrideUseCase: UpdatePlannerOverrideUseCase,
    private val createPlannerOverrideUseCase: CreatePlannerOverrideUseCase,
    private val networkStateProvider: NetworkStateProvider,
    private val calendarSharedEvents: CalendarSharedEvents,
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase,
    private val crashlytics: FirebaseCrashlytics,
    clock: Clock
) : ViewModel() {

    private var selectedDay = LocalDate.now(clock)
    private val plannerItemsMap = mutableMapOf<String, PlannerItem>()
    private var showCompleted = false
    private var courseMap = mapOf<Long, Course>()

    private val eventsByDay = mutableMapOf<LocalDate, MutableList<PlannerItem>>()
    private val loadingDays = mutableSetOf<LocalDate>()
    private val errorDays = mutableSetOf<LocalDate>()
    private val loadedWeeks = mutableSetOf<LocalDate>()

    private val checkboxRemovedItems = mutableSetOf<String>()
    private var checkboxDebounceJob: Job? = null

    private val _uiState = MutableStateFlow(
        TodoWidgetUiState(
            selectedDay = selectedDay,
            calendarBodyUiState = createCalendarBodyUiState(),
            monthTitle = getMonthTitle(),
            showCompleted = showCompleted,
            scrollToPageOffset = 0,
            onTodoClick = ::onTodoClick,
            onAddTodoClick = ::onAddTodoClick,
            onDaySelected = ::onDaySelected,
            onPageChanged = ::onPageChanged,
            onNavigateWeek = ::onNavigateWeek,
            onToggleShowCompleted = ::toggleShowCompleted,
            onRefresh = ::refresh,
            onSnackbarDismissed = ::clearSnackbarMessage,
            onUndoMarkAsDoneUndone = ::handleUndoMarkAsDoneUndone,
            onMarkedAsDoneSnackbarDismissed = ::clearMarkedAsDoneItem,
            onToDoCountUpdated = ::onToDoCountUpdated
        )
    )
    val uiState: StateFlow<TodoWidgetUiState> = _uiState.asStateFlow()

    init {
        observeConfig()
        observeCalendarSharedEvents()
        loadVisibleWeeks()
    }

    private fun onTodoClick(activity: FragmentActivity, htmlUrl: String) {
        todoWidgetBehavior.onTodoClick(activity, htmlUrl)
    }

    private fun onAddTodoClick(activity: FragmentActivity) {
        val dateString = selectedDay.atTime(12, 0).toApiStringSafe()
        todoWidgetBehavior.onAddTodoClick(activity, dateString)
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
            todosError = errorDays.contains(selectedDay),
            removingItemIds = emptySet(),
            confirmationSnackbarData = null
        )
    }

    private fun createTodoItemsForSelectedDay(): List<ToDoItemUiState> {
        val plannerItems = eventsByDay[selectedDay] ?: return emptyList()

        return plannerItems
            .filter { showCompleted || !it.isComplete() }
            .map { plannerItem ->
                val itemId = plannerItem.plannable.id.toString()
                toDoStateMapper.mapToUiState(
                    plannerItem = plannerItem,
                    courseMap = courseMap,
                    onSwipeToDone = { handleSwipeToDone(itemId) },
                    onCheckboxToggle = { isChecked -> handleCheckboxToggle(itemId, isChecked) }
                )
            }
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

    private fun loadVisibleWeeks(refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                // Load courses
                val courses = loadAvailableCoursesUseCase(LoadAvailableCoursesParams(forceRefresh = refresh))
                courseMap = courses.associateBy { it.id }

                // Load planner items for visible weeks
                val weekField = WeekFields.of(Locale.getDefault())
                val currentWeekStart = selectedDay.with(weekField.dayOfWeek(), 1)

                if (refresh) {
                    eventsByDay.clear()
                    loadedWeeks.clear()
                }

                val loadedStates = listOf(
                    async { loadEventsForWeek(currentWeekStart.minusWeeks(1), refresh) },
                    async { loadEventsForWeek(currentWeekStart, refresh) },
                    async { loadEventsForWeek(currentWeekStart.plusWeeks(1), refresh) }
                ).awaitAll()

                if (loadedStates.all { it }) {
                    _uiState.update { createNewUiState() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

        loadingDays.addAll(daysInWeek)
        errorDays.removeAll(daysInWeek)
        _uiState.update { createNewUiState() }

        return try {
            val result = loadPlannerItemsUseCase(
                startDate = weekStart.atStartOfDay().toApiStringSafe(),
                endDate = weekEnd.atTime(23, 59, 59).toApiStringSafe(),
                forceNetwork = refresh
            )

            loadingDays.removeAll(daysInWeek)

            loadedWeeks.add(weekStart)
            storeResults(result)
            _uiState.update { createNewUiState() }
            true
        } catch (e: Exception) {
            if (!refresh) {
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

            // Store in plannerItemsMap for later reference
            val itemId = plannerItem.plannable.id.toString()
            plannerItemsMap[itemId] = plannerItem
        }
    }

    private fun handleSwipeToDone(itemId: String) {
        viewModelScope.launch {
            if (!networkStateProvider.isOnline()) {
                _uiState.update {
                    it.copy(snackbarMessage = context.getString(R.string.todoActionOffline))
                }
                return@launch
            }

            val plannerItem = plannerItemsMap[itemId] ?: return@launch
            val currentIsChecked = plannerItem.isComplete()
            val newIsChecked = !currentIsChecked

            // Check if we should show completed items (based on switch state, not filter)
            val shouldRemoveFromList = newIsChecked && !showCompleted

            // Immediately add to removing set for animation if item should be hidden
            if (shouldRemoveFromList) {
                _uiState.update {
                    it.copy(removingItemIds = it.removingItemIds + itemId)
                }
            }

            val success = updateItemCompleteState(itemId, newIsChecked)

            // Show marked-as-done snackbar
            if (success) {
                _uiState.update {
                    it.copy(
                        confirmationSnackbarData = ConfirmationSnackbarData(
                            itemId = itemId,
                            title = plannerItem.plannable.title,
                            markedAsDone = newIsChecked
                        )
                    )
                }
            } else {
                // Remove from removing set if update failed
                if (shouldRemoveFromList) {
                    _uiState.update {
                        it.copy(removingItemIds = it.removingItemIds - itemId)
                    }
                }
            }
        }
    }

    private fun handleCheckboxToggle(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            if (!networkStateProvider.isOnline()) {
                _uiState.update {
                    it.copy(snackbarMessage = context.getString(R.string.todoActionOffline))
                }
                return@launch
            }

            val plannerItem = plannerItemsMap[itemId] ?: return@launch

            val shouldRemoveFromList = isChecked && !showCompleted

            // Handle checkbox removal animation
            if (shouldRemoveFromList) {
                // Add to pending removal set (will be removed after debounce)
                checkboxRemovedItems.add(itemId)
            } else if (!isChecked && itemId in checkboxRemovedItems) {
                // Unchecking - remove from pending removal
                checkboxRemovedItems.remove(itemId)
                // If item was already in removingItemIds, restore it
                _uiState.update {
                    it.copy(removingItemIds = it.removingItemIds - itemId)
                }
            }

            // Reset debounce timer
            startCheckboxDebounceTimer()

            val success = updateItemCompleteState(itemId, isChecked)

            // Show marked-as-done snackbar only when checking the box
            if (success) {
                _uiState.update {
                    it.copy(
                        confirmationSnackbarData = ConfirmationSnackbarData(
                            itemId = itemId,
                            title = plannerItem.plannable.title,
                            markedAsDone = isChecked
                        )
                    )
                }
            } else {
                // Remove from pending removal if update failed
                if (shouldRemoveFromList) {
                    checkboxRemovedItems.remove(itemId)
                }
            }
        }
    }

    private fun handleUndoMarkAsDoneUndone(itemId: String, markedAsDone: Boolean) {
        viewModelScope.launch {
            // Clear the snackbar immediately and restore item to list
            _uiState.update {
                it.copy(
                    confirmationSnackbarData = null,
                    removingItemIds = it.removingItemIds - itemId
                )
            }

            updateItemCompleteState(itemId, !markedAsDone)
        }
    }

    private suspend fun updateItemCompleteState(itemId: String, newIsChecked: Boolean): Boolean {
        val plannerItem = plannerItemsMap[itemId] ?: return false
        val currentIsChecked = plannerItem.isComplete()

        // Optimistically update UI
        updateItemCheckedState(itemId, newIsChecked)

        return try {
            // Update or create planner override
            val plannerOverrideResult = if (plannerItem.plannerOverride?.id != null) {
                updatePlannerOverrideUseCase(
                    UpdatePlannerOverrideParams(
                        plannerOverrideId = plannerItem.plannerOverride?.id.orDefault(),
                        markedComplete = newIsChecked
                    )
                )
            } else {
                createPlannerOverrideUseCase(
                    CreatePlannerOverrideParams(
                        plannableId = plannerItem.plannable.id,
                        plannableType = plannerItem.plannableType,
                        markedComplete = newIsChecked
                    )
                )
            }

            // Update the stored planner item with new override state
            val updatedPlannerItem = plannerItem.copy(plannerOverride = plannerOverrideResult)
            plannerItemsMap[itemId] = updatedPlannerItem

            // Update in eventsByDay as well
            val date = plannerItem.plannableDate.toLocalDate()
            eventsByDay[date]?.let { items ->
                val index = items.indexOfFirst { it.plannable.id.toString() == itemId }
                if (index != -1) {
                    items[index] = updatedPlannerItem
                }
            }

            // Invalidate planner cache
            CanvasRestAdapter.clearCacheUrls("planner")
            todoWidgetBehavior.updateWidget(true)
            _uiState.update { _uiState.value.copy(updateToDoCount = true) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Revert the optimistic update
            updateItemCheckedState(itemId, currentIsChecked)
            // Show error snackbar
            _uiState.update {
                it.copy(snackbarMessage = context.getString(R.string.errorUpdatingToDo))
            }
            false
        }
    }

    private fun updateItemCheckedState(itemId: String, isChecked: Boolean) {
        _uiState.update { state ->
            val updatedTodos = state.todos.map { item ->
                if (item.id == itemId) {
                    item.copy(isChecked = isChecked)
                } else {
                    item
                }
            }
            state.copy(todos = updatedTodos)
        }
    }

    private fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun clearMarkedAsDoneItem() {
        _uiState.update {
            it.copy(confirmationSnackbarData = null)
        }
    }

    private fun onToDoCountUpdated() {
        _uiState.update {
            it.copy(updateToDoCount = false)
        }
    }

    private fun startCheckboxDebounceTimer() {
        // Cancel existing timer
        checkboxDebounceJob?.cancel()

        // Only start timer if there are items pending removal
        if (checkboxRemovedItems.isEmpty()) {
            return
        }

        // Start new 1-second timer
        checkboxDebounceJob = viewModelScope.launch {
            delay(1000)

            // Add checkbox-removed items to removingItemIds for animation
            _uiState.update { state ->
                state.copy(
                    removingItemIds = state.removingItemIds + checkboxRemovedItems
                )
            }
            checkboxRemovedItems.clear()
        }
    }

    fun refresh() {
        _uiState.update { _uiState.value.copy(todosLoading = true) }
        loadVisibleWeeks(refresh = true)
    }

    private fun observeCalendarSharedEvents() {
        viewModelScope.launch {
            calendarSharedEvents.events.collect { action ->
                when (action) {
                    is SharedCalendarAction.RefreshToDoList -> {
                        refresh()
                    }
                    else -> {} // Ignore other calendar actions
                }
            }
        }
    }

    private fun observeConfig() {
        viewModelScope.launch {
            observeGlobalConfigUseCase(Unit)
                .catch { crashlytics.recordException(it) }
                .collect { config ->
                    val themedColor = ColorKeeper.createThemedColor(config.backgroundColor)
                    _uiState.update { it.copy(color = themedColor) }
                }
        }
    }
}
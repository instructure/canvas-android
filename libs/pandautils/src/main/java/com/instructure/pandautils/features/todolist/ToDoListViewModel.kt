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
package com.instructure.pandautils.features.todolist

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.todolist.filter.DateRangeSelection
import com.instructure.pandautils.room.appdatabase.daos.ToDoFilterDao
import com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.filterByToDoFilters
import com.instructure.pandautils.utils.getContextNameForPlannerItem
import com.instructure.pandautils.utils.getDateTextForPlannerItem
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.getTagForPlannerItem
import com.instructure.pandautils.utils.getUrl
import com.instructure.pandautils.utils.isComplete
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ToDoListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ToDoListRepository,
    private val networkStateProvider: NetworkStateProvider,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val toDoFilterDao: ToDoFilterDao,
    private val apiPrefs: ApiPrefs,
    private val analytics: Analytics,
    private val toDoListViewModelBehavior: ToDoListViewModelBehavior,
    private val calendarSharedEvents: CalendarSharedEvents,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ToDoListUiState(
            onSnackbarDismissed = { clearSnackbarMessage() },
            onUndoMarkAsDoneUndoneAction = { handleUndoMarkAsDoneUndone() },
            onMarkedAsDoneSnackbarDismissed = { clearMarkedAsDoneItem() },
            onRefresh = { handleRefresh() },
            onFiltersChanged = { dateFiltersChanged -> onFiltersChanged(dateFiltersChanged) }
        ))

    private fun onFiltersChanged(dateFiltersChanged: Boolean) {
        // Update widget
        toDoListViewModelBehavior.updateWidget(false)
        if (dateFiltersChanged) {
            loadData(forceRefresh = false)
        } else {
            applyFiltersLocally()
        }
    }

    private fun applyFiltersLocally() {
        viewModelScope.launch {
            try {
                val todoFilters = toDoFilterDao.findByUser(
                    apiPrefs.fullDomain,
                    apiPrefs.user?.id.orDefault()
                ) ?: ToDoFilterEntity(userDomain = apiPrefs.fullDomain, userId = apiPrefs.user?.id.orDefault())

                val allPlannerItems = plannerItemsMap.values.toList()
                val filteredCourses = courseMap.values.toList()

                processAndUpdateItems(allPlannerItems, filteredCourses, todoFilters)
            } catch (e: Exception) {
                e.printStackTrace()
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    val uiState = _uiState.asStateFlow()

    private val plannerItemsMap = mutableMapOf<String, PlannerItem>()
    private var courseMap = mapOf<Long, Course>()

    // Track items removed via checkbox for debounced clearing
    private val checkboxRemovedItems = mutableSetOf<String>()
    private var checkboxDebounceJob: Job? = null

    init {
        loadData()
        observeCalendarSharedEvents()
    }

    private fun observeCalendarSharedEvents() {
        viewModelScope.launch {
            calendarSharedEvents.events.collect { action ->
                when (action) {
                    is SharedCalendarAction.RefreshToDoList -> {
                        loadData(forceRefresh = true)
                    }
                    else -> {} // Ignore other calendar actions
                }
            }
        }
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = !forceRefresh, isRefreshing = forceRefresh, isError = false) }

                val todoFilters = toDoFilterDao.findByUser(
                    apiPrefs.fullDomain,
                    apiPrefs.user?.id.orDefault()
                ) ?: ToDoFilterEntity(userDomain = apiPrefs.fullDomain, userId = apiPrefs.user?.id.orDefault())

                val startDate = todoFilters.pastDateRange.calculatePastDateRange().toApiString()
                val endDate = todoFilters.futureDateRange.calculateFutureDateRange().toApiString()

                val courses = repository.getCourses(forceRefresh).dataOrThrow
                val plannerItems = repository.getPlannerItems(startDate, endDate, forceRefresh).dataOrThrow
                    .filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }

                // Store planner items for later reference
                plannerItemsMap.clear()
                plannerItems.forEach { plannerItemsMap[it.plannable.id.toString()] = it }

                // Filter courses - exclude access restricted, invited
                val filteredCourses = courses.filter {
                    !it.accessRestrictedByDate && !it.isInvited()
                }
                courseMap = filteredCourses.associateBy { it.id }

                processAndUpdateItems(plannerItems, filteredCourses, todoFilters)

                // Track analytics event for filter loading
                trackFilterLoadingEvent(todoFilters)
            } catch (e: Exception) {
                e.printStackTrace()
                firebaseCrashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isError = true
                    )
                }
            }
        }
    }

    private fun mapToUiState(plannerItem: PlannerItem, courseMap: Map<Long, Course>): ToDoItemUiState {
        val itemType = when (plannerItem.plannableType) {
            PlannableType.ASSIGNMENT -> ToDoItemType.ASSIGNMENT
            PlannableType.SUB_ASSIGNMENT -> ToDoItemType.SUB_ASSIGNMENT
            PlannableType.QUIZ -> ToDoItemType.QUIZ
            PlannableType.DISCUSSION_TOPIC -> ToDoItemType.DISCUSSION
            PlannableType.CALENDAR_EVENT -> ToDoItemType.CALENDAR_EVENT
            PlannableType.PLANNER_NOTE -> ToDoItemType.PLANNER_NOTE
            else -> ToDoItemType.CALENDAR_EVENT
        }

        val itemId = plannerItem.plannable.id.toString()

        // Account-level calendar events should not be clickable
        val isAccountLevelEvent = plannerItem.contextType?.equals("Account", ignoreCase = true) == true
        val isClickable = !(isAccountLevelEvent && itemType == ToDoItemType.CALENDAR_EVENT)

        return ToDoItemUiState(
            id = itemId,
            title = plannerItem.plannable.title,
            date = plannerItem.plannableDate,
            dateLabel = plannerItem.getDateTextForPlannerItem(context),
            contextLabel = plannerItem.getContextNameForPlannerItem(context, courseMap.values),
            canvasContext = plannerItem.canvasContext,
            itemType = itemType,
            isChecked = plannerItem.isComplete(),
            iconRes = plannerItem.getIconForPlannerItem(),
            tag = plannerItem.getTagForPlannerItem(context),
            htmlUrl = plannerItem.getUrl(apiPrefs),
            isClickable = isClickable,
            onSwipeToDone = { handleSwipeToDone(itemId) },
            onCheckboxToggle = { isChecked -> handleCheckboxToggle(itemId, isChecked) }
        )
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

            // Check if we should show completed items
            val todoFilters = toDoFilterDao.findByUser(
                apiPrefs.fullDomain,
                apiPrefs.user?.id.orDefault()
            ) ?: ToDoFilterEntity(userDomain = apiPrefs.fullDomain, userId = apiPrefs.user?.id.orDefault())

            val shouldRemoveFromList = newIsChecked && !todoFilters.showCompleted

            // Immediately add to removing set for animation if item should be hidden
            if (shouldRemoveFromList) {
                _uiState.update {
                    it.copy(removingItemIds = it.removingItemIds + itemId)
                }
            }

            val success = updateItemCompleteState(itemId, newIsChecked)

            // Show marked-as-done snackbar only when marking as done (not when undoing)
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

    private fun handleUndoMarkAsDoneUndone() {
        viewModelScope.launch {
            val markedAsDoneItem = _uiState.value.confirmationSnackbarData ?: return@launch
            val itemId = markedAsDoneItem.itemId

            // If this item was in checkbox-removed items, remove it and reset timer
            if (itemId in checkboxRemovedItems) {
                checkboxRemovedItems.remove(itemId)
                startCheckboxDebounceTimer()
            }

            // Clear the snackbar immediately and restore item to list
            _uiState.update {
                it.copy(
                    confirmationSnackbarData = null,
                    removingItemIds = it.removingItemIds - itemId
                )
            }

            updateItemCompleteState(itemId, !markedAsDoneItem.markedAsDone)
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

            // Check if we should show completed items
            val todoFilters = toDoFilterDao.findByUser(
                apiPrefs.fullDomain,
                apiPrefs.user?.id.orDefault()
            ) ?: ToDoFilterEntity(userDomain = apiPrefs.fullDomain, userId = apiPrefs.user?.id.orDefault())

            val shouldRemoveFromList = isChecked && !todoFilters.showCompleted

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

            applyFiltersLocally()
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
                repository.updatePlannerOverride(
                    plannerOverrideId = plannerItem.plannerOverride?.id.orDefault(),
                    markedComplete = newIsChecked
                ).dataOrThrow
            } else {
                repository.createPlannerOverride(
                    plannableId = plannerItem.plannable.id,
                    plannableType = plannerItem.plannableType,
                    markedComplete = newIsChecked
                ).dataOrThrow
            }

            // Update the stored planner item with new override state
            val updatedPlannerItem = plannerItem.copy(plannerOverride = plannerOverrideResult)
            plannerItemsMap[itemId] = updatedPlannerItem

            // Invalidate planner cache
            repository.invalidateCachedResponses()
            toDoListViewModelBehavior.updateWidget(true)

            // Track analytics event
            if (newIsChecked) {
                analytics.logEvent(AnalyticsEventConstants.TODO_ITEM_MARKED_DONE)
            } else {
                analytics.logEvent(AnalyticsEventConstants.TODO_ITEM_MARKED_UNDONE)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            firebaseCrashlytics.recordException(e)
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
            val updatedItemsByDate = state.itemsByDate.mapValues { (_, items) ->
                items.map { item ->
                    if (item.id == itemId) {
                        item.copy(isChecked = isChecked)
                    } else {
                        item
                    }
                }
            }
            val toDoCount = calculateToDoCount(updatedItemsByDate)
            state.copy(itemsByDate = updatedItemsByDate, toDoCount = toDoCount)
        }
    }

    private fun calculateToDoCount(itemsByDate: Map<Date, List<ToDoItemUiState>>): Int {
        return itemsByDate.values.flatten().count { !it.isChecked }
    }

    private fun handleRefresh() {
        loadData(forceRefresh = true)
    }

    private fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun clearMarkedAsDoneItem() {
        _uiState.update {
            it.copy(confirmationSnackbarData = null)
        }
    }

    private fun processAndUpdateItems(
        plannerItems: List<PlannerItem>,
        filteredCourses: List<Course>,
        todoFilters: ToDoFilterEntity
    ) {
        // Cancel checkbox debounce and clear tracked items when data reloads
        checkboxDebounceJob?.cancel()
        checkboxRemovedItems.clear()

        val filteredItems = plannerItems
            .filterByToDoFilters(todoFilters, filteredCourses)
            .sortedBy { it.comparisonDate }

        val itemsByDate = filteredItems
            .groupBy { DateHelper.getCleanDate(it.comparisonDate.time) }
            .mapValues { (_, items) ->
                items.map { plannerItem ->
                    mapToUiState(plannerItem, courseMap)
                }
            }

        val toDoCount = calculateToDoCount(itemsByDate)
        val isFilterApplied = isFilterApplied(todoFilters)

        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                isError = false,
                itemsByDate = itemsByDate,
                toDoCount = toDoCount,
                isFilterApplied = isFilterApplied,
                removingItemIds = emptySet(), // Clear removing items when data is reprocessed
                confirmationSnackbarData = null
            )
        }
    }

    private fun trackFilterLoadingEvent(filters: ToDoFilterEntity) {
        val isDefaultFilter = !filters.personalTodos &&
                !filters.calendarEvents &&
                !filters.showCompleted &&
                !filters.favoriteCourses &&
                filters.pastDateRange == DateRangeSelection.ONE_WEEK &&
                filters.futureDateRange == DateRangeSelection.ONE_WEEK

        if (isDefaultFilter) {
            analytics.logEvent(AnalyticsEventConstants.TODO_LIST_LOADED_DEFAULT_FILTER)
        } else {
            val bundle = Bundle().apply {
                putString(AnalyticsParamConstants.FILTER_PERSONAL_TODOS, filters.personalTodos.toString())
                putString(AnalyticsParamConstants.FILTER_CALENDAR_EVENTS, filters.calendarEvents.toString())
                putString(AnalyticsParamConstants.FILTER_SHOW_COMPLETED, filters.showCompleted.toString())
                putString(AnalyticsParamConstants.FILTER_FAVOURITE_COURSES, filters.favoriteCourses.toString())
                putString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_PAST, filters.pastDateRange.name.lowercase())
                putString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_FUTURE, filters.futureDateRange.name.lowercase())
            }
            analytics.logEvent(AnalyticsEventConstants.TODO_LIST_LOADED_CUSTOM_FILTER, bundle)
        }
    }

    private fun isFilterApplied(filters: ToDoFilterEntity): Boolean {
        return filters.personalTodos || filters.calendarEvents || filters.showCompleted || filters.favoriteCourses
                || filters.pastDateRange != DateRangeSelection.ONE_WEEK || filters.futureDateRange != DateRangeSelection.ONE_WEEK
    }
}

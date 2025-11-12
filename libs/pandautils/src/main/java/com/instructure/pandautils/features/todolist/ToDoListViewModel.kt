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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.getContextNameForPlannerItem
import com.instructure.pandautils.utils.getDateTextForPlannerItem
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.getTagForPlannerItem
import com.instructure.pandautils.utils.getUrl
import com.instructure.pandautils.utils.isComplete
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ToDoListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ToDoListRepository,
    private val networkStateProvider: NetworkStateProvider,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ToDoListUiState(
            onSnackbarDismissed = { clearSnackbarMessage() },
            onUndoMarkAsDoneUndoneAction = { handleUndoMarkAsDoneUndone() },
            onMarkedAsDoneSnackbarDismissed = { clearMarkedAsDoneItem() },
            onRefresh = { handleRefresh() }
        ))
    val uiState = _uiState.asStateFlow()

    private val plannerItemsMap = mutableMapOf<String, PlannerItem>()

    init {
        loadData()
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = !forceRefresh, isRefreshing = forceRefresh, isError = false) }

                val now = LocalDate.now().atStartOfDay()
                val startDate = now.minusDays(7).toApiString().orEmpty()
                val endDate = now.plusDays(7).toApiString().orEmpty()

                val courses = repository.getCourses(forceRefresh).dataOrThrow
                val plannerItems = repository.getPlannerItems(startDate, endDate, forceRefresh).dataOrThrow

                // Filter courses - exclude access restricted, invited
                val filteredCourses = courses.filter {
                    !it.accessRestrictedByDate && !it.isInvited()
                }
                val courseMap = filteredCourses.associateBy { it.id }

                // Filter planner items - exclude announcements, assessment requests
                val filteredItems = plannerItems
                    .filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }
                    .sortedBy { it.comparisonDate }

                // Store planner items for later reference
                plannerItemsMap.clear()
                filteredItems.forEach { plannerItemsMap[it.plannable.id.toString()] = it }

                // Group items by date
                val itemsByDate = filteredItems
                    .groupBy { DateHelper.getCleanDate(it.comparisonDate.time) }
                    .mapValues { (_, items) ->
                        items.map { plannerItem ->
                            mapToUiState(plannerItem, courseMap)
                        }
                    }

                val toDoCount = calculateToDoCount(itemsByDate)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isError = false,
                        itemsByDate = itemsByDate,
                        toDoCount = toDoCount
                    )
                }
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
            }
        }
    }

    private fun handleUndoMarkAsDoneUndone() {
        viewModelScope.launch {
            val markedAsDoneItem = _uiState.value.confirmationSnackbarData ?: return@launch
            val itemId = markedAsDoneItem.itemId

            // Clear the snackbar immediately
            _uiState.update { it.copy(confirmationSnackbarData = null) }

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
            }
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
        _uiState.update { it.copy(confirmationSnackbarData = null) }
    }
}

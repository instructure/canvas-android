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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getContextNameForPlannerItem
import com.instructure.pandautils.utils.getDateTextForPlannerItem
import com.instructure.pandautils.utils.getIconForPlannerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class ToDoListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ToDoListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToDoListUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ToDoListViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = !forceRefresh, isRefreshing = forceRefresh, isError = false) }

                val now = LocalDate.now().atStartOfDay()
                val startDate = now.minusDays(28).toApiString().orEmpty()
                val endDate = now.plusDays(28).toApiString().orEmpty()

                val coursesResult = repository.getCourses(forceRefresh)
                val plannerItemsResult = repository.getPlannerItems(startDate, endDate, forceRefresh)

                val courses = coursesResult.dataOrNull ?: emptyList()
                val plannerItems = plannerItemsResult.dataOrNull ?: emptyList()

                // Filter courses - exclude access restricted, invited
                val filteredCourses = courses.filter {
                    !it.accessRestrictedByDate && !it.isInvited()
                }
                val courseMap = filteredCourses.associateBy { it.id }

                // Filter planner items - exclude announcements, assessment requests
                val filteredItems = plannerItems
                    .filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }
                    .sortedBy { it.comparisonDate }

                // Group items by date
                val itemsByDate = filteredItems
                    .groupBy { DateHelper.getCleanDate(it.comparisonDate.time) }
                    .mapValues { (_, items) ->
                        items.map { plannerItem ->
                            mapToUiState(plannerItem, courseMap)
                        }
                    }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isError = false,
                        itemsByDate = itemsByDate
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

        return ToDoItemUiState(
            id = plannerItem.plannable.id.toString(),
            title = plannerItem.plannable.title,
            date = plannerItem.plannableDate,
            dateLabel = plannerItem.getDateTextForPlannerItem(context),
            contextLabel = plannerItem.getContextNameForPlannerItem(context, courseMap.values),
            canvasContext = plannerItem.canvasContext,
            itemType = itemType,
            isChecked = false,
            iconRes = plannerItem.getIconForPlannerItem()
        )
    }

    fun handleAction(action: ToDoListActionHandler) {
        when (action) {
            is ToDoListActionHandler.ItemClicked -> {
                viewModelScope.launch {
                    _events.send(ToDoListViewModelAction.OpenToDoItem(action.itemId))
                }
            }

            is ToDoListActionHandler.Refresh -> {
                loadData(forceRefresh = true)
            }

            is ToDoListActionHandler.ToggleItemChecked -> {
                // TODO: Implement toggle checked - will be implemented in future story
            }

            is ToDoListActionHandler.FilterClicked -> {
                // TODO: Implement filter - will be implemented in future story
            }
        }
    }
}

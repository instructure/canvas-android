/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.assignments.list

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.GroupedListViewEvent
import com.instructure.pandautils.compose.composables.GroupedListViewState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListGroupByOption
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ScreenState
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AssignmentListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val apiPrefs: ApiPrefs,
    private val resources: Resources,
    private val repository: AssignmentListRepository,
    private val assignmentListBehavior: AssignmentListBehavior
): ViewModel() {
    private val _uiState = MutableStateFlow(AssignmentListUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AssignmentListFragmentEvent>()
    val events = _events.receiveAsFlow()

    private val courseId: Long? = savedStateHandle.get<Long>(Const.COURSE_ID)

    init {
        getAssignments(false)
    }

    private fun getAssignments(forceRefresh: Boolean = false) {
        if (courseId != null) {
            viewModelScope.tryLaunch {
                val assignmentGroups = repository.getAssignments(courseId, forceRefresh).dataOrThrow
                val gradingPeriods = repository.getGradingPeriodsForCourse(courseId, forceRefresh).dataOrThrow
                val allAssignment = assignmentGroups.flatMap { it.assignments }
                val gradingPeriodAssignments = gradingPeriods.associateWith { gradingPeriod ->
                    repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                        courseId,
                        gradingPeriod.id,
                        false,
                        forceRefresh
                    ).dataOrThrow
                        .flatMap { it.assignments }
                }
                val course = repository.getCourse(courseId).dataOrThrow
                _uiState.update {
                    it.copy(
                        state = ScreenState.Content,
                        subtitle = course.name,
                        course = course,
                        allAssignments = allAssignment,
                        assignmentGroups = assignmentGroups,
                        gradingPeriods = gradingPeriods,
                        gradingPeriodsWithAssignments = gradingPeriodAssignments,
                        listState = GroupedListViewState(
                            assignmentGroups.map { group ->
                                AssignmentGroupState(
                                    group.id,
                                    group.name.orEmpty(),
                                    group.assignments.map { assignment ->
                                        assignmentListBehavior.getAssignmentGroupItemState(assignment)
                                    }
                                )
                            }
                        ),
                        filterState = assignmentListBehavior.getAssignmentListFilterState(course.color, gradingPeriods)
                    )
                }

                uiState.value.filterState.filterGroups.forEach { group ->
                    repository.getSelectedOptions(
                        apiPrefs.fullDomain,
                        apiPrefs.user?.id.orDefault(),
                        course.id,
                        group.groupId
                    )?.let { selectedIndexes ->
                        _uiState.update {
                            it.copy(
                                filterState = it.filterState.copy(
                                    filterGroups = it.filterState.filterGroups.map { filterGroup ->
                                        if (filterGroup.groupId == group.groupId) {
                                            filterGroup.copy(selectedOptionIndexes = selectedIndexes)
                                        } else {
                                            filterGroup
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            } catch {
                _uiState.update { it.copy(state = ScreenState.Error) }
            }
        } else {
            _uiState.update { it.copy(state = ScreenState.Error) }
        }
    }

    fun handleListEvent(event: GroupedListViewEvent<AssignmentGroupState, AssignmentGroupItemState>) {
        when (event) {
            is GroupedListViewEvent.GroupClicked -> {
                _uiState.update {
                    it.copy(
                        listState = it.listState.copy(
                            groups = it.listState.groups.map { group ->
                                if (group.id == event.group.id) {
                                    AssignmentGroupState(
                                        group.id as? Long ?: 0,
                                        group.title,
                                        group.items,
                                        !group.isExpanded
                                    )
                                } else {
                                    group
                                }
                            }
                        )
                    )
                }
            }

            is GroupedListViewEvent.ItemClicked -> {
                viewModelScope.launch {
                    _events.send(AssignmentListFragmentEvent.NavigateToAssignment(uiState.value.course, event.groupItem.assignment.id))
                }
            }
        }
    }

    fun handleAction(action: AssignmentListScreenEvent) {
        when (action) {
            AssignmentListScreenEvent.NavigateBack -> {
                viewModelScope.launch {
                    _events.send(AssignmentListFragmentEvent.NavigateBack)
                }
            }
            is AssignmentListScreenEvent.UpdateFilterState -> {
                _uiState.update { it.copy(filterState = action.filterState) }
                _uiState.update {
                    it.copy(
                        listState = performFilters()
                    )
                }
                viewModelScope.launch {
                    repository.updateSelectedOptions(
                        apiPrefs.fullDomain,
                        apiPrefs.user?.id.orDefault(),
                        uiState.value.course.id,
                        action.filterState
                    )
                }
            }
            AssignmentListScreenEvent.OpenFilterScreen -> {
                _uiState.update { it.copy(screenOption = AssignmentListScreenOption.Filter) }
            }
            AssignmentListScreenEvent.CloseFilterScreen -> {
                _uiState.update { it.copy(screenOption = AssignmentListScreenOption.List) }
            }
        }
    }

    private fun performFilters(): GroupedListViewState<AssignmentGroupState> {
        var groups: List<AssignmentGroupState> = emptyList()

        val allAssignments = uiState.value.allAssignments
        var filteredAssignments = allAssignments.toSet()
        val filters = uiState.value.filterState.filterGroups.filter { it.filterType == AssignmentListFilterType.Filter }
        filters
            .forEach { filterGroup ->
                val newFilteredAssignments = mutableSetOf<Assignment>()
                filterGroup.selectedOptionIndexes.forEach {
                    val filter = filterGroup.options[it]
                    when (filter) {
                        is AssignmentListFilterOption.AllStatusAssignments -> {
                            newFilteredAssignments += filteredAssignments
                        }

                        is AssignmentListFilterOption.AllFilterAssignments -> {
                            newFilteredAssignments += filteredAssignments
                        }

                        is AssignmentListFilterOption.NeedsGrading -> {
                            newFilteredAssignments +=
                                filteredAssignments.filter { it.needsGradingCount > 0 }
                                    .toMutableSet()
                        }

                        is AssignmentListFilterOption.NotSubmitted -> {
                            newFilteredAssignments +=
                                filteredAssignments.filter { it.unpublishable }.toMutableSet()
                        }

                        is AssignmentListFilterOption.Published -> {
                            newFilteredAssignments +=
                                filteredAssignments.filter { it.published }.toMutableSet()
                        }

                        is AssignmentListFilterOption.Unpublished -> {
                            newFilteredAssignments +=
                                filteredAssignments.filter { !it.published }.toMutableSet()
                        }

                        is AssignmentListFilterOption.GradingPeriod -> {
                            if (filter.period == null) {
                                newFilteredAssignments += filteredAssignments
                            } else {
                                newFilteredAssignments += filteredAssignments.filter { uiState.value.gradingPeriodsWithAssignments[filter.period]?.contains(it).orDefault() }
                            }
                        }

                        is AssignmentListFilterOption.NotYetSubmitted -> {
                            newFilteredAssignments +=
                                filteredAssignments.filter { !it.isSubmitted }.toMutableSet()
                        }

                        is AssignmentListFilterOption.ToBeGraded -> {
                            newFilteredAssignments +=
                                filteredAssignments.filter { it.isSubmitted && !it.isGraded() }
                                    .toMutableSet()
                        }

                        is AssignmentListFilterOption.Graded -> {
                            newFilteredAssignments +=
                                filteredAssignments.filter { it.isGraded() }.toMutableSet()
                        }

                        is AssignmentListFilterOption.Other -> {
                            newFilteredAssignments +=
                                filteredAssignments.filterNot { !it.isSubmitted || it.isSubmitted && !it.isGraded() || it.isGraded() }
                                    .toMutableSet()
                        }
                    }
                }
                filteredAssignments = newFilteredAssignments
            }

        val groupByGroup = uiState.value.filterState.filterGroups.firstOrNull { it.filterType == AssignmentListFilterType.GroupBy }
        val groupBy = groupByGroup?.options?.get(groupByGroup.selectedOptionIndexes.firstOrNull() ?: 0)
            ?: AssignmentListGroupByOption.AssignmentGroup(resources)
        groups = when (groupBy) {
            is AssignmentListGroupByOption.AssignmentGroup -> {
                filteredAssignments
                    .groupBy { it.assignmentGroupId }
                    .map { (key, value) ->
                        AssignmentGroupState(
                            id = key,
                            title = uiState.value.assignmentGroups.firstOrNull { it.id == key }?.name.orEmpty(),
                            items = value.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                        )
                    }
            }
            is AssignmentListGroupByOption.AssignmentType -> {
                val discussionsGroup = filteredAssignments.filter {
                    Assignment.SubmissionType.DISCUSSION_TOPIC.apiString in it.submissionTypesRaw
                }.toSet()
                val quizzesGroup = (filteredAssignments - discussionsGroup).filter {
                    Assignment.SubmissionType.ONLINE_QUIZ.apiString in it.submissionTypesRaw
                }.toSet()
                val assignmentGroup = filteredAssignments - discussionsGroup - quizzesGroup

                listOf(
                    AssignmentGroupState(
                        id = 0,
                        title = resources.getString(R.string.assignments),
                        items = assignmentGroup.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                    ),
                    AssignmentGroupState(
                        id = 1,
                        title = resources.getString(R.string.discussion),
                        items = discussionsGroup.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                    ),
                    AssignmentGroupState(
                        id = 2,
                        title = resources.getString(R.string.quizzes),
                        items = quizzesGroup.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                    )
                )
            }
            is AssignmentListGroupByOption.DueDate -> {
                val undated = filteredAssignments.filter {
                    it.dueDate == null
                }
                val upcoming = filteredAssignments.filter {
                    it.dueDate != null && (it.dueDate ?: Date()) > Date()
                }
                val past = filteredAssignments.filter {
                    it.dueDate != null && (it.dueDate ?: Date()) < Date()
                }
                listOf(
                    AssignmentGroupState(
                        id = 0,
                        title = resources.getString(R.string.undatedAssignments),
                        items = undated.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                    ),
                    AssignmentGroupState(
                        id = 1,
                        title = resources.getString(R.string.upcomingAssignments),
                        items = upcoming.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                    ),
                    AssignmentGroupState(
                        id = 2,
                        title = resources.getString(R.string.overdueAssignments),
                        items = past.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                    )
                )
            }
            else -> {
                filteredAssignments
                    .groupBy { it.assignmentGroupId }
                    .map { (key, value) ->
                        AssignmentGroupState(
                            id = key,
                            title = uiState.value.assignmentGroups.firstOrNull { it.id == key }?.name.orEmpty(),
                            items = value.map { assignmentListBehavior.getAssignmentGroupItemState(it) }
                        )
                    }
            }
        }
        return GroupedListViewState(groups)
    }
}
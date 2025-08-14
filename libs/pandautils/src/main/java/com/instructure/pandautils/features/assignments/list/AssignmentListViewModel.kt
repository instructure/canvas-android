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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.GroupedListViewEvent
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOptions
import com.instructure.pandautils.features.assignments.list.filter.AssignmentStatusFilterOption
import com.instructure.pandautils.room.assignment.list.entities.toEntity
import com.instructure.pandautils.room.assignment.list.entities.toModel
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ScreenState
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

    var bookmarker = Bookmarker(true, Course(courseId ?: 0))

    private var customStatuses = listOf<CustomGradeStatusesQuery.Node>()

    init {
        getAssignments(false)
    }

    fun initOverFlowMenu(activity: FragmentActivity, fragment: AssignmentListFragment) {
        _uiState.update { it.copy(overFlowItems = assignmentListBehavior.getOverFlowMenuItems(activity, fragment)) }
    }

    private fun getAssignments(forceRefresh: Boolean = false) {
        if (courseId != null) {
            viewModelScope.tryLaunch {
                val course = repository.getCourse(courseId, forceRefresh)
                customStatuses = repository.getCustomGradeStatuses(courseId, forceRefresh)
                bookmarker = Bookmarker(true, course)
                viewModelScope.launch { _events.send(AssignmentListFragmentEvent.UpdateStatusBarStyle(course)) }
                _uiState.update { it.copy(course = course) }

                val assignmentGroups = repository.getAssignments(courseId, forceRefresh)
                val gradingPeriods = repository.getGradingPeriodsForCourse(courseId, forceRefresh)
                val allAssignment = assignmentGroups.flatMap { it.assignments }
                val gradingPeriodAssignments = gradingPeriods.associateWith { gradingPeriod ->
                    repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                        courseId,
                        gradingPeriod.id,
                        forceRefresh
                    )
                        .flatMap { it.assignments }
                }
                val selectedFilters = if (forceRefresh) {
                    uiState.value.selectedFilterData
                } else {
                    repository.getSelectedOptions(
                        apiPrefs.fullDomain,
                        apiPrefs.user?.id.orDefault(),
                        course.id
                    )?.toModel()?.copy(selectedGradingPeriodFilter = getCurrentGradingPeriod(gradingPeriods))
                        ?: assignmentListBehavior.getDefaultSelection(getCurrentGradingPeriod(gradingPeriods))
                }

                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        subtitle = course.name,
                        course = course,
                        allAssignments = allAssignment,
                        assignmentGroups = assignmentGroups,
                        gradingPeriods = gradingPeriods,
                        currentGradingPeriod = getCurrentGradingPeriod(gradingPeriods),
                        gradingPeriodsWithAssignments = gradingPeriodAssignments,
                        listState = assignmentGroups.associate { assignmentGroup ->
                            assignmentGroup.name.orEmpty() to assignmentGroup.assignments.map { assignment ->
                                assignmentListBehavior.getAssignmentGroupItemState(course, assignment, customStatuses)
                            }
                        },
                        filterOptions = AssignmentListFilterOptions(
                            assignmentFilters = assignmentListBehavior.getAssignmentFilters(),
                            assignmentStatusFilters = assignmentListBehavior.getAssignmentStatusFilters(),
                            groupByOptions = assignmentListBehavior.getGroupByOptions(),
                            gradingPeriodOptions = listOf(null) + gradingPeriods
                        ),
                        selectedFilterData = selectedFilters
                    )
                }

                val listState = performFilters()
                _uiState.update {
                    it.copy(
                        state = if (listState.keys.isEmpty()) ScreenState.Empty else ScreenState.Content,
                        listState = listState
                    )
                }
            } catch {
                _uiState.update { it.copy(state = ScreenState.Error, isRefreshing = false) }
            }
        } else {
            _uiState.update { it.copy(state = ScreenState.Error, isRefreshing = false) }
        }
    }

    fun handleListEvent(event: GroupedListViewEvent<AssignmentGroupItemState>) {
        when (event) {
            is GroupedListViewEvent.ItemClicked -> {
                viewModelScope.launch {
                    _events.send(AssignmentListFragmentEvent.NavigateToAssignment(uiState.value.course, event.groupItem.assignment))
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
                _uiState.update { it.copy(selectedFilterData = action.selectedFilters) }
                val listState = performFilters()
                _uiState.update {
                    it.copy(
                        state = if (listState.keys.isEmpty()) ScreenState.Empty else ScreenState.Content,
                        listState = listState
                    )
                }
                viewModelScope.launch {
                    repository.updateSelectedOptions(
                        action.selectedFilters.toEntity(
                            apiPrefs.fullDomain,
                            apiPrefs.user?.id.orDefault(),
                            uiState.value.course.id
                        )
                    )
                }
            }
            AssignmentListScreenEvent.OpenFilterScreen -> {
                _uiState.update { it.copy(screenOption = AssignmentListScreenOption.Filter) }
            }
            AssignmentListScreenEvent.CloseFilterScreen -> {
                _uiState.update { it.copy(screenOption = AssignmentListScreenOption.List) }
            }
            is AssignmentListScreenEvent.ExpandCollapseSearchBar -> {
                _uiState.update { it.copy(searchBarExpanded = action.expanded) }
            }
            is AssignmentListScreenEvent.SearchContentChanged -> {
                _uiState.update { it.copy(searchQuery = action.query) }
                val listState = performFilters()
                _uiState.update {
                    it.copy(
                        state = if (listState.keys.isEmpty()) ScreenState.Empty else ScreenState.Content,
                        listState = listState
                    )
                }
            }
            is AssignmentListScreenEvent.ChangeOverflowMenuState -> {
                _uiState.update { it.copy(overFlowItemsExpanded = action.expanded) }
            }
            AssignmentListScreenEvent.Refresh -> {
                _uiState.update { it.copy(isRefreshing = true) }
                getAssignments(true)
            }
        }
    }

    private fun performFilters(): Map<String, List<AssignmentGroupItemState>> {
        val searchQuery = uiState.value.searchQuery
        val allAssignments = uiState.value.allAssignments.filter { it.name?.contains(searchQuery, true) ?: true }
        var filteredAssignments = allAssignments
        val course = uiState.value.course
        val selectedFilters = uiState.value.selectedFilterData

        val assignmentFilters = selectedFilters.selectedAssignmentFilters.ifEmpty {
            listOf(AssignmentFilter.All) // Do not filter if no filters are selected
        }
        filteredAssignments = assignmentFilters.flatMap { assignmentFilter ->
            when(assignmentFilter) {
                AssignmentFilter.All -> filteredAssignments
                AssignmentFilter.NotYetSubmitted -> filteredAssignments.filter { !it.isSubmitted && it.isOnlineSubmissionType }
                AssignmentFilter.ToBeGraded -> filteredAssignments.filter { it.isSubmitted && !it.isGraded() && it.isOnlineSubmissionType }
                AssignmentFilter.Graded -> filteredAssignments.filter { it.isGraded() && it.isOnlineSubmissionType }
                AssignmentFilter.Other -> filteredAssignments.filterNot { (!it.isSubmitted && it.isOnlineSubmissionType) || (it.isSubmitted && !it.isGraded() && it.isOnlineSubmissionType) || (it.isGraded() && it.isOnlineSubmissionType) }
                AssignmentFilter.NeedsGrading -> filteredAssignments.filter { it.needsGradingCount > 0 }
                AssignmentFilter.NotSubmitted -> filteredAssignments.filter { it.unpublishable }
            }
        }

        selectedFilters.selectedAssignmentStatusFilter?.let { statusFilter ->
            filteredAssignments = when (statusFilter) {
                AssignmentStatusFilterOption.All -> filteredAssignments
                AssignmentStatusFilterOption.Published -> filteredAssignments.filter { it.published }
                AssignmentStatusFilterOption.Unpublished -> filteredAssignments.filter { !it.published }
            }
        }

        selectedFilters.selectedGradingPeriodFilter?.let { gradingPeriodFilter ->
            if (uiState.value.gradingPeriods.isNotEmpty()) {
                filteredAssignments = filteredAssignments.filter {
                    uiState.value.gradingPeriodsWithAssignments[gradingPeriodFilter]?.map { it.id }
                        ?.contains(it.id).orDefault()
                }
            }
        }

        filteredAssignments = filteredAssignments.sortedBy { it.id }.distinct()

        val groups = when(selectedFilters.selectedGroupByOption) {
            AssignmentGroupByOption.DueDate -> {
                val undated = filteredAssignments.filter {
                    it.dueDate == null
                }
                val upcoming = filteredAssignments.filter {
                    it.dueDate != null && (it.dueDate ?: Date()) >= Date()
                }
                val past = filteredAssignments.filter {
                    it.dueDate != null && (it.dueDate ?: Date()) < Date()
                }
                mapOf(
                    resources.getString(R.string.overdueAssignments) to past.map {
                        assignmentListBehavior.getAssignmentGroupItemState(
                            course, it, customStatuses
                        )
                    },

                    resources.getString(R.string.upcomingAssignments) to upcoming.map {
                        assignmentListBehavior.getAssignmentGroupItemState(
                            course, it, customStatuses
                        )
                    },

                    resources.getString(R.string.undatedAssignments) to undated.map {
                        assignmentListBehavior.getAssignmentGroupItemState(
                            course, it, customStatuses
                        )
                    }
                )
            }
            AssignmentGroupByOption.AssignmentGroup -> {
                filteredAssignments.groupBy { it.assignmentGroupId }.map { (key, value) ->
                    uiState.value.assignmentGroups.firstOrNull { it.id == key }?.name.orEmpty() to value.map {
                        assignmentListBehavior.getAssignmentGroupItemState(
                            course, it, customStatuses
                        )
                    }
                }.toMap()
            }
            AssignmentGroupByOption.AssignmentType -> {
                val discussionsGroup = filteredAssignments.filter {
                    Assignment.SubmissionType.DISCUSSION_TOPIC.apiString in it.submissionTypesRaw
                }.toSet()
                val quizzesGroup = (filteredAssignments - discussionsGroup).filter {
                    Assignment.SubmissionType.ONLINE_QUIZ.apiString in it.submissionTypesRaw
                }.toSet()
                val assignmentGroup = filteredAssignments - discussionsGroup - quizzesGroup

                mapOf(
                    resources.getString(R.string.assignments) to assignmentGroup.map {
                        assignmentListBehavior.getAssignmentGroupItemState(
                            course, it, customStatuses
                        )
                    }, resources.getString(R.string.discussion) to discussionsGroup.map {
                        assignmentListBehavior.getAssignmentGroupItemState(
                            course, it, customStatuses
                        )
                    }, resources.getString(R.string.quizzes) to quizzesGroup.map {
                        assignmentListBehavior.getAssignmentGroupItemState(
                            course, it, customStatuses
                        )
                    }
                )
            }
        }.filter { it.value.isNotEmpty() }

        return groups
    }

    private fun getCurrentGradingPeriod(gradingPeriods: List<GradingPeriod>): GradingPeriod? {
        val currentDate = Date()
        return gradingPeriods.firstOrNull { it.startDate?.toDate()?.before(currentDate).orDefault() && it.endDate?.toDate()?.after(currentDate).orDefault() }
    }
}
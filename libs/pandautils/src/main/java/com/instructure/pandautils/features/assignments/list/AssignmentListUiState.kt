package com.instructure.pandautils.features.assignments.list

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.compose.composables.GroupedListViewGroup
import com.instructure.pandautils.compose.composables.GroupedListViewGroupItem
import com.instructure.pandautils.compose.composables.GroupedListViewState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOptions
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.utils.ScreenState

data class AssignmentListUiState(
    val title: String = "",
    val course: Course = Course(),
    val subtitle: String = "",
    val state: ScreenState = ScreenState.Loading,
    val isRefreshing: Boolean = false,
    val screenOption: AssignmentListScreenOption = AssignmentListScreenOption.List,
    val allAssignments: List<Assignment> = emptyList(),
    val gradingPeriods: List<GradingPeriod> = emptyList(),
    val assignmentGroups: List<AssignmentGroup> = emptyList(),
    val gradingPeriodsWithAssignments: Map<GradingPeriod, List<Assignment>> = emptyMap(),
    val listState: GroupedListViewState<AssignmentGroupState> = GroupedListViewState(emptyList()),
    val filterOptions: AssignmentListFilterOptions? = null,
    val selectedFilterData: AssignmentListSelectedFilters = AssignmentListSelectedFilters(),
    val searchQuery: String = "",
    val searchBarExpanded: Boolean = false,
    val overFlowItemsExpanded: Boolean = false,
    val overFlowItems: List<AssignmentListMenuOverFlowItem> = emptyList()
)

data class AssignmentListMenuOverFlowItem(
    val label: String,
    val onClick: () -> Unit
)

class AssignmentGroupState(
    id: Long,
    title: String,
    items: List<AssignmentGroupItemState>,
    initiallyExpanded: Boolean = true
): GroupedListViewGroup<AssignmentGroupItemState>(
    id = id,
    title = title,
    items = items,
    isExpanded = initiallyExpanded
)

class AssignmentGroupItemState(
    val course: Course,
    val assignment: Assignment,
    val showPublishStateIcon: Boolean = false,
    val showClosedState: Boolean = false,
    val showDueDate: Boolean = false,
    val showSubmissionState: Boolean = false,
    val showGrade: Boolean = false,
    val showNeedsGrading: Boolean = false,
    val showMaxPoints: Boolean = false
): GroupedListViewGroupItem(assignment.id)

sealed class AssignmentListFragmentEvent {
    data class NavigateToAssignment(val canvasContext: CanvasContext, val assignment: Assignment): AssignmentListFragmentEvent()
    data class UpdateStatusBarStyle(val canvasContext: CanvasContext): AssignmentListFragmentEvent()
    data object NavigateBack: AssignmentListFragmentEvent()
}

sealed class AssignmentListScreenEvent {
    data object NavigateBack: AssignmentListScreenEvent()
    data class UpdateFilterState(val selectedFilters: AssignmentListSelectedFilters): AssignmentListScreenEvent()
    data object OpenFilterScreen: AssignmentListScreenEvent()
    data object CloseFilterScreen: AssignmentListScreenEvent()
    data class ExpandCollapseSearchBar(val expanded: Boolean): AssignmentListScreenEvent()
    data class SearchContentChanged(val query: String): AssignmentListScreenEvent()
    data class ChangeOverflowMenuState(val expanded: Boolean): AssignmentListScreenEvent()
    data object Refresh: AssignmentListScreenEvent()
}

sealed class AssignmentListScreenOption {
    data object List: AssignmentListScreenOption()
    data object Filter: AssignmentListScreenOption()
}
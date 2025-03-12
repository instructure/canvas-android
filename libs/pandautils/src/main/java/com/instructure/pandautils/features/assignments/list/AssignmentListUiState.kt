package com.instructure.pandautils.features.assignments.list

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.compose.composables.GroupedListViewGroup
import com.instructure.pandautils.compose.composables.GroupedListViewGroupItem
import com.instructure.pandautils.compose.composables.GroupedListViewState
import com.instructure.pandautils.utils.ScreenState

data class AssignmentListUiState(
    val title: String = "",
    val course: Course = Course(),
    val subTitle: String? = null,
    val state: ScreenState = ScreenState.Loading,
    val listState: GroupedListViewState<AssignmentGroupState> = GroupedListViewState(emptyList())
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
    val assignment: Assignment,
    val showAssignmentDetails: Boolean = false,
    val showSubmissionDetails: Boolean = false
): GroupedListViewGroupItem(assignment.id)

sealed class AssignmentListFragmentEvent {
    data class NavigateToAssignment(val canvasContext: CanvasContext, val assignmentId: Long): AssignmentListFragmentEvent()
    data object NavigateBack: AssignmentListFragmentEvent()
}
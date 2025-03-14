package com.instructure.pandautils.features.assignments.list.filter

data class AssignmentListFilterState(
    val filterGroups: List<AssignmentListFilterGroup>,
)

data class AssignmentListFilterGroup(
    val title: String,
    val options: List<AssignmentListGroup>,
    val selectedOptions: List<AssignmentListGroup>,
    val groupType: AssignmentListFilterGroupType,
)

enum class AssignmentListFilterType {
    GroupBy,
    Filter
}

enum class AssignmentListFilterGroupType {
    SingleChoice,
    MultiChoice
}

abstract class AssignmentListGroup(val type: AssignmentListFilterType)

sealed class AssignmentListGroupByOption: AssignmentListGroup(AssignmentListFilterType.GroupBy) {
    data object AssignmentGroup: AssignmentListGroupByOption()
    data object AssignmentType: AssignmentListGroupByOption()
    data object DueDate: AssignmentListGroupByOption()
}

sealed class AssignmentListFilterOption: AssignmentListGroup(AssignmentListFilterType.Filter) {
    data object NeedsGrading: AssignmentListFilterOption()
    data object NotSubmitted: AssignmentListFilterOption()
    data object Published: AssignmentListFilterOption()
    data object Unpublished: AssignmentListFilterOption()
    data class GradingPeriod(val period: com.instructure.canvasapi2.models.GradingPeriod?): AssignmentListFilterOption()
    data object NotYetSubmitted: AssignmentListFilterOption()
    data object ToBeGraded: AssignmentListFilterOption()
    data object Graded: AssignmentListFilterOption()
    data object Other: AssignmentListFilterOption()
}
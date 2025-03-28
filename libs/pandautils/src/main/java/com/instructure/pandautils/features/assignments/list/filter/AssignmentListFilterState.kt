package com.instructure.pandautils.features.assignments.list.filter

import com.instructure.canvasapi2.models.GradingPeriod

data class AssignmentListFilterOptions(
    val assignmentFilters: AssignmentListFilterData,
    val assignmentStatusFilters: List<AssignmentStatusFilterOption>?,
    val groupByOptions: List<AssignmentGroupByOption>,
    val gradingPeriodOptions: List<GradingPeriod?>?
)

data class AssignmentListSelectedFilters(
    val selectedAssignmentFilters: List<AssignmentFilter> = emptyList(),
    val selectedAssignmentStatusFilter: AssignmentStatusFilterOption? = null,
    val selectedGroupByOption: AssignmentGroupByOption = AssignmentGroupByOption.AssignmentGroup,
    val selectedGradingPeriodFilter: GradingPeriod? = null
)

data class AssignmentListFilterData(
    val assignmentFilterOptions: List<AssignmentFilter>,
    val assignmentFilterType: AssignmentListFilterType,
)

enum class AssignmentListFilterType {
    SingleChoice,
    MultiChoice
}

enum class AssignmentFilter {
    All,
    NotYetSubmitted,
    ToBeGraded,
    Graded,
    Other,
    NeedsGrading,
    NotSubmitted,
}

enum class AssignmentStatusFilterOption {
    All,
    Published,
    Unpublished,
}

enum class AssignmentGroupByOption {
    DueDate,
    AssignmentGroup,
    AssignmentType,
}
package com.instructure.pandautils.features.assignments.list.filter

import android.content.res.Resources
import androidx.annotation.ColorInt
import com.instructure.pandares.R

data class AssignmentListFilterState(
    @ColorInt val contextColor: Int = 0,
    val filterGroups: List<AssignmentListFilterGroup> = emptyList(),
)

data class AssignmentListFilterGroup(
    val title: String,
    val options: List<AssignmentListGroupItem>,
    val selectedOptions: List<AssignmentListGroupItem>,
    val groupType: AssignmentListFilterGroupType,
    val filterType: AssignmentListFilterType,
)

enum class AssignmentListFilterType {
    GroupBy,
    Filter
}

enum class AssignmentListFilterGroupType {
    SingleChoice,
    MultiChoice
}

abstract class AssignmentListGroupItem(val stringValue: String) {
    final override fun toString(): String = stringValue
}

sealed class AssignmentListGroupByOption(stringValue: String): AssignmentListGroupItem(stringValue) {
    data class AssignmentGroup(val resources: Resources): AssignmentListGroupByOption(resources.getString(R.string.assignmentGroup))
    data class AssignmentType(val resources: Resources): AssignmentListGroupByOption(resources.getString(R.string.assignmentType))
    data class DueDate(val resources: Resources): AssignmentListGroupByOption(resources.getString(R.string.dueDate))
}

sealed class AssignmentListFilterOption(stringValue: String): AssignmentListGroupItem(stringValue) {
    data class AllStatusAssignments(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.allAssignments))
    data class AllFilterAssignments(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.allAssignments))
    data class NeedsGrading(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.needsGrading))
    data class NotSubmitted(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.notSubmitted))
    data class Published(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.published))
    data class Unpublished(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.unpublished))
    data class GradingPeriod(
        val period: com.instructure.canvasapi2.models.GradingPeriod?,
        val resources: Resources
    ): AssignmentListFilterOption(period?.title ?: resources.getString(R.string.allGradingPeriods))
    data class NotYetSubmitted(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.notYetSubmitted))
    data class ToBeGraded(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.toBeGraded))
    data class Graded(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.graded))
    data class Other(val resources: Resources): AssignmentListFilterOption(resources.getString(R.string.other))
}
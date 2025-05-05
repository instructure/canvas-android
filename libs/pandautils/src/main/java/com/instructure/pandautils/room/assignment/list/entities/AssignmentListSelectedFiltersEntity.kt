package com.instructure.pandautils.room.assignment.list.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.assignments.list.filter.AssignmentStatusFilterOption

@Entity(tableName = "assignment_filter")
data class AssignmentListSelectedFiltersEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userDomain: String,
    val userId: Long,
    val contextId: Long,
    val selectedAssignmentFilters: List<AssignmentFilter>,
    val selectedAssignmentStatusFilter: AssignmentStatusFilterOption?,
    val selectedGroupByOption: AssignmentGroupByOption,
)

fun AssignmentListSelectedFiltersEntity.toModel(): AssignmentListSelectedFilters {
    return AssignmentListSelectedFilters(
        selectedAssignmentFilters = selectedAssignmentFilters,
        selectedAssignmentStatusFilter = selectedAssignmentStatusFilter,
        selectedGroupByOption = selectedGroupByOption,
    )
}

fun AssignmentListSelectedFilters.toEntity(
    userDomain: String,
    userId: Long,
    contextId: Long,
): AssignmentListSelectedFiltersEntity {
    return AssignmentListSelectedFiltersEntity(
        userDomain = userDomain,
        userId = userId,
        contextId = contextId,
        selectedAssignmentFilters = selectedAssignmentFilters,
        selectedAssignmentStatusFilter = selectedAssignmentStatusFilter,
        selectedGroupByOption = selectedGroupByOption,
    )
}
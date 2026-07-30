/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades.strategy

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterData
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOptions
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.assignments.list.filter.applyAssignmentTypeFilters
import com.instructure.pandautils.features.assignments.list.filter.applyStatusFilter
import com.instructure.pandautils.features.grades.AssignmentGroupUiState
import com.instructure.pandautils.features.grades.GradesFilterUiState
import com.instructure.pandautils.features.grades.GradesUiState
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import javax.inject.Inject

class NGCGradesStrategy @Inject constructor() : GradesExperienceStrategy {

    override fun initialFilterState(
        course: Course,
        gradingPeriods: List<GradingPeriod>,
        currentGradingPeriod: GradingPeriod?,
    ): GradesFilterUiState {
        val gradingPeriodOptions = if (gradingPeriods.isEmpty()) null else listOf(null) + gradingPeriods
        return GradesFilterUiState(
            courseName = course.name,
            isFilterScreenOpen = false,
            selectedFilters = AssignmentListSelectedFilters(
                selectedAssignmentFilters = listOf(
                    AssignmentFilter.NotYetSubmitted,
                    AssignmentFilter.ToBeGraded,
                    AssignmentFilter.Graded,
                    AssignmentFilter.Other,
                ),
                selectedAssignmentStatusFilter = null,
                selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup,
                selectedGradingPeriodFilter = currentGradingPeriod,
            ),
            filterOptions = AssignmentListFilterOptions(
                assignmentFilters = AssignmentListFilterData(
                    assignmentFilterOptions = listOf(
                        AssignmentFilter.NotYetSubmitted,
                        AssignmentFilter.ToBeGraded,
                        AssignmentFilter.Graded,
                        AssignmentFilter.Other,
                    ),
                    assignmentFilterType = AssignmentListFilterType.MultiChoice,
                ),
                assignmentStatusFilters = null,
                groupByOptions = listOf(
                    AssignmentGroupByOption.DueDate,
                    AssignmentGroupByOption.AssignmentGroup,
                ),
                gradingPeriodOptions = gradingPeriodOptions,
            ),
        )
    }

    override fun applyFilters(
        assignments: List<Assignment>,
        uiState: GradesUiState,
    ): List<Assignment> {
        val filter = uiState.filter ?: return assignments
        val selected = filter.selectedFilters
        return assignments
            .applyAssignmentTypeFilters(selected.selectedAssignmentFilters)
            .applyStatusFilter(selected.selectedAssignmentStatusFilter)
    }

    override fun onFilterScreenOpenChanged(
        uiState: GradesUiState,
        open: Boolean,
    ): GradesUiState {
        val filter = uiState.filter ?: return uiState
        return uiState.copy(filter = filter.copy(isFilterScreenOpen = open))
    }

    override fun onFilterUpdated(
        uiState: GradesUiState,
        selected: AssignmentListSelectedFilters,
    ): GradesUiState {
        val filter = uiState.filter ?: return uiState
        return uiState.copy(filter = filter.copy(selectedFilters = selected))
    }

    override fun decorateGroupItems(
        items: List<AssignmentGroupUiState>,
        course: Course?,
        domainGroups: List<AssignmentGroup>,
        sortBy: SortBy,
        onlyGraded: Boolean,
        showWhatIfScore: Boolean,
    ): List<AssignmentGroupUiState> {
        if (sortBy != SortBy.GROUP) return items

        // Hide groups with no weight when the course applies assignment-group weights.
        return if (course?.isApplyAssignmentGroupWeights == true) {
            val weightedGroupIds = domainGroups
                .filter { it.groupWeight > 0.0 }
                .map { it.id }
                .toSet()
            items.filter { it.id in weightedGroupIds }
        } else {
            items
        }
    }
}

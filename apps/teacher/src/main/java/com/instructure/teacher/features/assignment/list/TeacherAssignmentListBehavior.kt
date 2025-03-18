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
package com.instructure.teacher.features.assignment.list

import android.content.res.Resources
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.assignments.list.AssignmentGroupItemState
import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroup
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroupType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListGroupByOption
import com.instructure.teacher.R

class TeacherAssignmentListBehavior(private val resources: Resources): AssignmentListBehavior {
    override fun getAssignmentGroupItemState(assignment: Assignment): AssignmentGroupItemState {
        return AssignmentGroupItemState(assignment, showAssignmentDetails = true)
    }

    override fun getAssignmentListFilterState(
        contextColor: Int,
        gradingPeriods: List<GradingPeriod>?
    ): AssignmentListFilterState {
        val groups = mutableListOf(
            AssignmentListFilterGroup(
                groupIndex = 0,
                title = resources.getString(R.string.assignmentFilter),
                options = listOf(
                    AssignmentListFilterOption.AllFilterAssignments(resources),
                    AssignmentListFilterOption.NeedsGrading(resources),
                    AssignmentListFilterOption.NotSubmitted(resources),
                ),
                selectedOptionIndexes = (0..3).toList(),
                groupType = AssignmentListFilterGroupType.SingleChoice,
                filterType = AssignmentListFilterType.Filter
            ),
            AssignmentListFilterGroup(
                groupIndex = 1,
                title = resources.getString(R.string.statusFilter),
                options = listOf(
                    AssignmentListFilterOption.AllStatusAssignments(resources),
                    AssignmentListFilterOption.Published(resources),
                    AssignmentListFilterOption.Unpublished(resources),
                ),
                selectedOptionIndexes = listOf(0),
                groupType = AssignmentListFilterGroupType.SingleChoice,
                filterType = AssignmentListFilterType.Filter
            ),
            AssignmentListFilterGroup(
                groupIndex = 2,
                title = resources.getString(R.string.groupedBy),
                options = listOf(
                    AssignmentListGroupByOption.AssignmentGroup(resources),
                    AssignmentListGroupByOption.AssignmentType(resources),
                ),
                selectedOptionIndexes = listOf(0),
                groupType = AssignmentListFilterGroupType.SingleChoice,
                filterType = AssignmentListFilterType.GroupBy
            ),
        )
        if (gradingPeriods != null && gradingPeriods.size > 1) {
            val allGradingPeriod = AssignmentListFilterOption.GradingPeriod(null, resources)
            groups.add(
                AssignmentListFilterGroup(
                    groupIndex = 4,
                    title = resources.getString(R.string.gradingPeriod),
                    options = listOf(allGradingPeriod) + gradingPeriods.map {
                        AssignmentListFilterOption.GradingPeriod(it, resources)
                    },
                    selectedOptionIndexes = listOf(0),
                    groupType = AssignmentListFilterGroupType.SingleChoice,
                    filterType = AssignmentListFilterType.Filter
                )
            )
        }

        return AssignmentListFilterState(contextColor, groups)
    }
}
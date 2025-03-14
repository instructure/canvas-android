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
package com.instructure.student.features.assignments.list

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.assignments.list.AssignmentGroupItemState
import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroup
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroupType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListGroupByOption

class StudentAssignmentListBehavior: AssignmentListBehavior {
    override fun getAssignmentGroupItemState(assignment: Assignment): AssignmentGroupItemState {
        return AssignmentGroupItemState(assignment, showSubmissionDetails = true)
    }

    override fun getAssignmentListFilterState(gradingPeriods: List<GradingPeriod>): AssignmentListFilterState {
       val groups = mutableListOf(
            AssignmentListFilterGroup(
                title = "Assignment filter",
                options = listOf(
                    AssignmentListFilterOption.NotYetSubmitted,
                    AssignmentListFilterOption.ToBeGraded,
                    AssignmentListFilterOption.Graded,
                    AssignmentListFilterOption.Other,
                ),
                selectedOptions = listOf(
                    AssignmentListFilterOption.NotYetSubmitted,
                    AssignmentListFilterOption.ToBeGraded,
                    AssignmentListFilterOption.Graded,
                    AssignmentListFilterOption.Other,
                ),
                groupType = AssignmentListFilterGroupType.MultiChoice
            ),
            AssignmentListFilterGroup(
                title = "Grouped By",
                options = listOf(
                    AssignmentListGroupByOption.DueDate,
                    AssignmentListGroupByOption.AssignmentGroup,
                ),
                selectedOptions = listOf(AssignmentListGroupByOption.DueDate),
                groupType = AssignmentListFilterGroupType.SingleChoice
            ),
        )
        if (gradingPeriods.size > 1) {
            val allGradingPeriod = AssignmentListFilterOption.GradingPeriod(null)
            groups.add(
                AssignmentListFilterGroup(
                    title = "Grading Period",
                    options = listOf(allGradingPeriod) + gradingPeriods.map {
                        AssignmentListFilterOption.GradingPeriod(it)
                     },
                    selectedOptions = listOf(allGradingPeriod),
                    groupType = AssignmentListFilterGroupType.SingleChoice
                )
            )
        }

        return AssignmentListFilterState(groups)
    }
}
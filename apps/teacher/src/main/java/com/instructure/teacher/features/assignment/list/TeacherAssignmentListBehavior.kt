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

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.assignments.list.AssignmentGroupItemState
import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.assignments.list.AssignmentListMenuOverFlowItem
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterData
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.assignments.list.filter.AssignmentStatusFilterOption

class TeacherAssignmentListBehavior : AssignmentListBehavior {
    override fun getAssignmentGroupItemState(
        course: Course,
        assignment: Assignment,
        customStatuses: List<CustomGradeStatusesQuery.Node>
    ): AssignmentGroupItemState {
        return AssignmentGroupItemState(
            course,
            assignment,
            customStatuses,
            showPublishStateIcon = true,
            showClosedState = true,
            showDueDate = true,
            showNeedsGrading = true,
            showMaxPoints = true
        )
    }

    override fun getAssignmentFilters(): AssignmentListFilterData {
        return AssignmentListFilterData(
            listOf(
                AssignmentFilter.All,
                AssignmentFilter.NeedsGrading,
                AssignmentFilter.NotSubmitted,
            ),
            AssignmentListFilterType.SingleChoice
        )
    }

    override fun getAssignmentStatusFilters(): List<AssignmentStatusFilterOption>? {
        return listOf(
            AssignmentStatusFilterOption.All,
            AssignmentStatusFilterOption.Published,
            AssignmentStatusFilterOption.Unpublished
        )
    }

    override fun getGroupByOptions(): List<AssignmentGroupByOption> {
        return listOf(
            AssignmentGroupByOption.AssignmentGroup,
            AssignmentGroupByOption.AssignmentType,
        )
    }

    override fun getDefaultSelection(currentGradingPeriod: GradingPeriod?): AssignmentListSelectedFilters {
        return AssignmentListSelectedFilters(
            selectedAssignmentFilters = listOf(AssignmentFilter.All),
            selectedAssignmentStatusFilter = AssignmentStatusFilterOption.All,
            selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup,
            selectedGradingPeriodFilter = currentGradingPeriod
        )
    }

    override fun getOverFlowMenuItems(activity: FragmentActivity, fragment: AssignmentListFragment): List<AssignmentListMenuOverFlowItem> = emptyList()
}
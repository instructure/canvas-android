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
package com.instructure.pandautils.features.assignments.list

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterData
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.assignments.list.filter.AssignmentStatusFilterOption

interface AssignmentListBehavior {
    fun getAssignmentGroupItemState(
        course: Course,
        assignment: Assignment,
        customStatuses: List<CustomGradeStatusesQuery.Node>,
        checkpoints: List<DiscussionCheckpointUiState>
    ): AssignmentGroupItemState

    fun getAssignmentFilters(): AssignmentListFilterData

    fun getAssignmentStatusFilters(): List<AssignmentStatusFilterOption>?

    fun getGroupByOptions(): List<AssignmentGroupByOption>

    fun getDefaultSelection(currentGradingPeriod: GradingPeriod?): AssignmentListSelectedFilters

    fun getOverFlowMenuItems(activity: FragmentActivity, fragment: AssignmentListFragment): List<AssignmentListMenuOverFlowItem>
}
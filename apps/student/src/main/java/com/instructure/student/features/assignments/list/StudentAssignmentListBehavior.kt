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

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
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
import com.instructure.student.R
import com.instructure.student.dialog.BookmarkCreationDialog

class StudentAssignmentListBehavior : AssignmentListBehavior {
    override fun getAssignmentGroupItemState(
        course: Course,
        assignment: Assignment,
        customStatuses: List<CustomGradeStatusesQuery.Node>,
        checkpoints: List<DiscussionCheckpointUiState>
    ): AssignmentGroupItemState {
        return AssignmentGroupItemState(
            course,
            assignment,
            customStatuses,
            checkpoints,
            showClosedState = true,
            showDueDate = true,
            showSubmissionState = true,
            showGrade = true
        )
    }

    override fun getAssignmentFilters(): AssignmentListFilterData {
        return AssignmentListFilterData(
            listOf(
                AssignmentFilter.NotYetSubmitted,
                AssignmentFilter.ToBeGraded,
                AssignmentFilter.Graded,
                AssignmentFilter.Other,
            ),
            AssignmentListFilterType.MultiChoice
        )
    }

    override fun getAssignmentStatusFilters(): List<AssignmentStatusFilterOption>? {
        return null
    }

    override fun getGroupByOptions(): List<AssignmentGroupByOption> {
        return listOf(
            AssignmentGroupByOption.DueDate,
            AssignmentGroupByOption.AssignmentGroup,
        )
    }

    override fun getDefaultSelection(currentGradingPeriod: GradingPeriod?): AssignmentListSelectedFilters {
        return AssignmentListSelectedFilters(
            listOf(AssignmentFilter.NotYetSubmitted, AssignmentFilter.ToBeGraded, AssignmentFilter.Graded, AssignmentFilter.Other),
            null,
            AssignmentGroupByOption.DueDate,
            currentGradingPeriod
        )
    }

    override fun getOverFlowMenuItems(activity: FragmentActivity, fragment: AssignmentListFragment): List<AssignmentListMenuOverFlowItem> {
        return listOf(
            AssignmentListMenuOverFlowItem(activity.getString(R.string.addBookmark)) {
                val dialog = BookmarkCreationDialog.newInstance(activity, fragment, null)
                dialog?.show(activity.supportFragmentManager, BookmarkCreationDialog::class.java.simpleName)
            }
        )
    }
}
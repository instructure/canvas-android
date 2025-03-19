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

import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.assignments.list.AssignmentGroupItemState
import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.assignments.list.AssignmentListMenuOverFlowItem
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroup
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroupType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListGroupByOption
import com.instructure.student.R
import com.instructure.student.dialog.BookmarkCreationDialog

class StudentAssignmentListBehavior(
    private val resources: Resources
): AssignmentListBehavior {
    override fun getAssignmentGroupItemState(assignment: Assignment): AssignmentGroupItemState {
        return AssignmentGroupItemState(assignment, showSubmissionDetails = true)
    }

    override fun getAssignmentListFilterState(@ColorInt contextColor: Int, courseName: String, gradingPeriods: List<GradingPeriod>?): AssignmentListFilterState {
        val groups = mutableListOf(
            AssignmentListFilterGroup(
                groupId = 0,
                title = resources.getString(R.string.assignmentFilter),
                options = listOf(
                    AssignmentListFilterOption.NotYetSubmitted(resources),
                    AssignmentListFilterOption.ToBeGraded(resources),
                    AssignmentListFilterOption.Graded(resources),
                    AssignmentListFilterOption.Other(resources),
                ),
                selectedOptionIndexes = (0..3).toList(),
                groupType = AssignmentListFilterGroupType.MultiChoice,
                filterType = AssignmentListFilterType.Filter
            ),
            AssignmentListFilterGroup(
                groupId = 1,
                title = resources.getString(R.string.groupedBy),
                options = listOf(
                    AssignmentListGroupByOption.DueDate(resources),
                    AssignmentListGroupByOption.AssignmentGroup(resources),
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
                    groupId = 2,
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

        return AssignmentListFilterState(contextColor, courseName, groups)
    }

    override fun getOverFlowMenuItems(activity: FragmentActivity, fragment: AssignmentListFragment): List<AssignmentListMenuOverFlowItem> {
        return listOf(
            AssignmentListMenuOverFlowItem(resources.getString(R.string.addBookmark)) {
                val dialog = BookmarkCreationDialog.newInstance(activity, fragment, null)
                dialog?.show(activity.supportFragmentManager, BookmarkCreationDialog::class.java.simpleName)
            }
        )
    }
}
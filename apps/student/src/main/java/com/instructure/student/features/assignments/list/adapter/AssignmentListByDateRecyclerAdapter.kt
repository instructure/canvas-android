/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.student.features.assignments.list.adapter

import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.student.R
import com.instructure.student.features.assignments.list.AssignmentListRepository
import com.instructure.student.interfaces.AdapterToAssignmentsCallback
import java.util.*

private const val HEADER_POSITION_OVERDUE = 0
private const val HEADER_POSITION_UPCOMING = 1
private const val HEADER_POSITION_UNDATED = 2
private const val HEADER_POSITION_PAST = 3

class AssignmentListByDateRecyclerAdapter(
    context: Context,
    canvasContext: CanvasContext,
    adapterToAssignmentsCallback: AdapterToAssignmentsCallback,
    isTesting: Boolean = false,
    filter: AssignmentListFilter = AssignmentListFilter.ALL,
    repository: AssignmentListRepository
) : AssignmentListRecyclerAdapter(context, canvasContext, adapterToAssignmentsCallback, isTesting, filter, repository) {

    private val overdue = AssignmentGroup(name = context.getString(R.string.overdueAssignments), position = HEADER_POSITION_OVERDUE)
    private val upcoming = AssignmentGroup(name = context.getString(R.string.upcomingAssignments), position = HEADER_POSITION_UPCOMING)
    private val undated = AssignmentGroup(name = context.getString(R.string.undatedAssignments), position = HEADER_POSITION_UNDATED)
    private val past = AssignmentGroup(name = context.getString(R.string.pastAssignments), position = HEADER_POSITION_PAST)

    override fun createItemCallback() = object : GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> {
        private val sameCheck = compareBy<Assignment>({ it.dueAt }, { it.name })
        override fun areContentsTheSame(old: Assignment, new: Assignment) = sameCheck.compare(old, new) == 0
        override fun areItemsTheSame(item1: Assignment, item2: Assignment) = item1.id == item2.id
        override fun getChildType(group: AssignmentGroup, item: Assignment) = Types.TYPE_ITEM
        override fun getUniqueItemId(item: Assignment) = item.id
        override fun compare(group: AssignmentGroup, o1: Assignment, o2: Assignment): Int {
            return when (group.position) {
                HEADER_POSITION_UNDATED -> o1.name?.lowercase(Locale.getDefault())
                    ?.compareTo(o2.name?.lowercase(Locale.getDefault()) ?: "") ?: 0
                HEADER_POSITION_PAST -> o2.dueAt?.compareTo(o1.dueAt ?: "") ?: 0 // Sort newest date first (o1 and o2 switched places)
                else -> o1.dueAt?.compareTo(o2.dueAt ?: "") ?: 0 // Sort oldest date first
            }
        }
    }

    override fun populateData() {
        val today = Date()
        for (assignmentGroup in assignmentGroups) {
            // TODO canHaveOverDueAssignment
            // web does it like this
            // # only handles observer observing one student, this needs to change to handle multiple users in the future
            // canHaveOverdueAssignment = !ENV.current_user_has_been_observer_in_this_course || ENV.observed_student_ids?.length == 1I
            // endtodo
            assignmentGroup.assignments
                    .filterWithQuery(searchQuery, Assignment::name)
                    .filter {
                        when (filter) {
                            AssignmentListFilter.ALL -> true
                            AssignmentListFilter.MISSING -> it.isMissing()
                            AssignmentListFilter.LATE -> it.submission?.late ?: false
                            AssignmentListFilter.GRADED -> it.submission?.isGraded ?: false
                            AssignmentListFilter.UPCOMING -> !it.isSubmitted && it.dueDate?.after(Date()) ?: false
                        }
                    }
                    .forEach { assignment ->
                        val dueAt = assignment.dueAt
                        val submission = assignment.submission
                        assignment.submission = submission
                        val isWithoutGradedSubmission = submission == null || submission.isWithoutGradedSubmission
                        val isOverdue = assignment.isAllowedToSubmit && isWithoutGradedSubmission
                        if (dueAt == null) {
                            addOrUpdateItem(undated, assignment)
                        } else {
                            when {
                                today.before(dueAt.toDate()) -> addOrUpdateItem(upcoming, assignment)
                                isOverdue -> addOrUpdateItem(overdue, assignment)
                                else -> addOrUpdateItem(past, assignment)
                            }
                        }
                    }
        }
        isAllPagesLoaded = true
    }

}

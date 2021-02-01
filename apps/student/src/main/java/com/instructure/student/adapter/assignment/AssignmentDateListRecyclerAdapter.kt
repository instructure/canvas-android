/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.adapter.assignment

import android.content.Context
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.student.R
import com.instructure.student.interfaces.AdapterToAssignmentsCallback
import java.util.*

class AssignmentDateListRecyclerAdapter(
        context: Context,
        canvasContext: CanvasContext,
        adapterToAssignmentsCallback: AdapterToAssignmentsCallback,
        isTesting: Boolean = false
) : AssignmentRecyclerAdapter(context, canvasContext, adapterToAssignmentsCallback, isTesting) {

    private val overdue: AssignmentGroup
    private val upcoming: AssignmentGroup
    private val undated: AssignmentGroup
    private val past: AssignmentGroup

    init {
        overdue = AssignmentGroup(name = context.getString(R.string.overdueAssignments), position = HEADER_POSITION_OVERDUE)
        upcoming = AssignmentGroup(name = context.getString(R.string.upcomingAssignments), position = HEADER_POSITION_UPCOMING)
        undated = AssignmentGroup(name = context.getString(R.string.undatedAssignments), position = HEADER_POSITION_UNDATED)
        past = AssignmentGroup(name = context.getString(R.string.pastAssignments), position = HEADER_POSITION_PAST)
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

/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.utils

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import java.util.*

object AssignmentUtils2 {

    const val ASSIGNMENT_STATE_UNKNOWN = -1
    const val ASSIGNMENT_STATE_SUBMITTED = 1000
    const val ASSIGNMENT_STATE_SUBMITTED_LATE = 1001
    const val ASSIGNMENT_STATE_DUE = 1002
    const val ASSIGNMENT_STATE_MISSING = 1003
    const val ASSIGNMENT_STATE_GRADED = 1004
    const val ASSIGNMENT_STATE_GRADED_LATE = 1005 // Graded late -> submitted late but has been graded
    const val ASSIGNMENT_STATE_GRADED_MISSING = 1006 // Graded missing -> not submitted but has been graded
    const val ASSIGNMENT_STATE_EXCUSED = 1007
    const val ASSIGNMENT_STATE_IN_CLASS = 1008
    const val ASSIGNMENT_STATE_DROPPED = 1009 //not yet used....

    fun getAssignmentState(assignment: Assignment?, submission: Submission?, isTeacher: Boolean = false): Int {
        // Case - Error
        if (assignment == null) {
            return ASSIGNMENT_STATE_UNKNOWN
        }

        // Case - Assignment does not take submissions, but is not 'on paper' (not graded, etc) and it has not been graded
        // Result - DUE
        if (assignment.turnInType == Assignment.TurnInType.NONE && submission?.grade == null) {
            return ASSIGNMENT_STATE_DUE
        }

        // Case - We have an assignment with no submission
        // Result - MISSING or DUE
        if (submission == null) {
            return if (assignment.dueAt != null && assignment.dueDate!!.time >= Calendar.getInstance().timeInMillis) {
                checkInClassOrDue(assignment)
            } else {
                checkInClassOrMissing(assignment)
            }
        } else {
            // Edge Case - Excused Assignment
            // Result - EXCUSED State
            if (submission.excused) {
                return ASSIGNMENT_STATE_EXCUSED
            }

            if (submission.hasSubAssignmentSubmissions) {
                return if (submission.subAssignmentSubmissions.any { it.excused }) {
                    ASSIGNMENT_STATE_EXCUSED
                } else if (submission.subAssignmentSubmissions.all { it.missing }) {
                    ASSIGNMENT_STATE_MISSING
                } else if (submission.subAssignmentSubmissions.all { it.grade != null }) {
                    ASSIGNMENT_STATE_GRADED
                } else if (submission.subAssignmentSubmissions.any { it.late }) {
                    ASSIGNMENT_STATE_SUBMITTED_LATE
                } else {
                    ASSIGNMENT_STATE_SUBMITTED
                }
            }

            // Edge Case - Assignment with "fake submission" and no grade
            // Result - MISSING or DUE
            return if (submission.attempt == 0L && submission.grade == null) {
                if (assignment.dueAt != null && assignment.dueDate!!.time >= Calendar.getInstance().timeInMillis) {
                    checkInClassOrDue(assignment)
                } else if (assignment.checkpoints?.any {it.dueAt != null && it.dueDate!!.time >= Calendar.getInstance().timeInMillis} == true) {
                    checkOnTimeOrLate(submission, hasNoGrade(assignment, submission, isTeacher))
                } else {
                    checkInClassOrMissing(assignment)
                }
            } else if (submission.missing) {
                ASSIGNMENT_STATE_GRADED_MISSING
            } else {
                checkOnTimeOrLate(submission, hasNoGrade(assignment, submission, isTeacher))
            }

        }
    }

    // Check to see if an assignment either
    // 1. Has not been graded
    // 2. Is "Pending Review"
    private fun hasNoGrade(assignment: Assignment, submission: Submission, isTeacher: Boolean): Boolean {
        return !submission.isGraded || Const.PENDING_REVIEW == submission.workflowState || (!isTeacher && assignment.submission?.postedAt == null)
    }

    // Edge Case - Assignment is either due in the future or an unknown "paper" hand in
    // Result - IN_CLASS or DUE
    private fun checkInClassOrDue(assignment: Assignment): Int {
        return if (assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ON_PAPER)) {
            ASSIGNMENT_STATE_IN_CLASS
        } else {
            ASSIGNMENT_STATE_DUE
        }
    }

    // Edge Case - Assignment is either past due or an unknown "paper" hand in
    // Result - IN_CLASS or MISSING
    private fun checkInClassOrMissing(assignment: Assignment): Int {
        // Edge Case - Check for paper submission
        return if (assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ON_PAPER)) {
            ASSIGNMENT_STATE_IN_CLASS
        } else {
            ASSIGNMENT_STATE_MISSING
        }
    }

    private fun checkOnTimeOrLate(submission: Submission, hasNoGrade: Boolean): Int {
        return if (hasNoGrade) {
            // Case - Assignment with a submission but "no grade"
            // Result - SUBMITTED or SUBMITTED_LATE
            if (submission.late) {
                ASSIGNMENT_STATE_SUBMITTED_LATE
            } else {
                ASSIGNMENT_STATE_SUBMITTED
            }
        } else {
            // Case - Assignment with a submission
            // Result - GRADED or GRADED_LATE
            if (submission.late) {
                ASSIGNMENT_STATE_GRADED_LATE
            } else {
                ASSIGNMENT_STATE_GRADED
            }
        }
    }
}

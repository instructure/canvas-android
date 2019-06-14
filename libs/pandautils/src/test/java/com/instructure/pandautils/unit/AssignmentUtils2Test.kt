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

package com.instructure.pandautils.unit

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.AssignmentUtils2
import org.junit.Assert
import org.junit.Test
import java.util.*

class AssignmentUtils2Test : Assert() {

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_unknownStateNullAssignment() {
        val assignment: Assignment? = null
        val submission = Submission()

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_UNKNOWN.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateDue() {
        val submission: Submission? = null

        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val assignment = Assignment(submission = submission, dueAt = date.toApiString())

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_DUE.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateInClass() {
        val submission: Submission? = null
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString(),
            submissionTypesRaw = listOf(Assignment.SubmissionType.ON_PAPER.apiString)
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateMissingNullDueDate() {
        val submission: Submission? = null
        val assignment = Assignment(submission = submission)

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_MISSING.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateSubmitted() {
        val submission = Submission(attempt = 1)

        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateGraded() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 1,
            grade = "A"
        )
        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_GRADED.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateSubmittedLate() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 1,
            late = true
        )

        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateGradedLate() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 1,
            grade = "A",
            late = true
        )

        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateExcused() {
        val submission = Submission(excused = true)
        val assignment = Assignment(submission = submission)

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_isTeacher_mutedGradeWithNoSubmission_stateGraded() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 0,
            grade = "A"
        )
        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString(),
            muted = true
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission, true)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_GRADED.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_isStudent_mutedGradeWithNoSubmission_stateGraded() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 0,
            grade = "A"
        )
        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString(),
            muted = true
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        Assert.assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED.toLong())
    }

}

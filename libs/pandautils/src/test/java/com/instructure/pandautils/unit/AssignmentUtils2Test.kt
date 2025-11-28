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
import com.instructure.canvasapi2.models.AssignmentDueDate
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.isAllowedToSubmitWithOverrides
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

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_UNKNOWN.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateDue() {
        val submission: Submission? = null

        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val assignment = Assignment(submission = submission, dueAt = date.toApiString())

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_DUE.toLong())
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

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateMissingNullDueDate() {
        val submission: Submission? = null
        val assignment = Assignment(submission = submission, submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString))

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_MISSING.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateSubmitted() {
        val submission = Submission(attempt = 1)

        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString(),
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString)
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateGraded() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 1,
            grade = "A",
            postedAt = Date()
        )
        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_GRADED.toLong())
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
            dueAt = date.toApiString(),
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString)
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateGradedLate() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 1,
            grade = "A",
            late = true,
            postedAt = Date()
        )

        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_stateExcused() {
        val submission = Submission(excused = true)
        val assignment = Assignment(submission = submission, submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString))

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_isTeacher_gradeWithNoSubmission_stateGraded() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 0,
            grade = "A"
        )
        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission, true)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_GRADED.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentState_isStudent_gradeWithNoSubmission_stateGraded() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val submission = Submission(
            attempt = 0,
            grade = "A"
        )
        val assignment = Assignment(
            submission = submission,
            dueAt = date.toApiString()
        )

        val testValue = AssignmentUtils2.getAssignmentState(assignment, submission)

        assertEquals("", testValue.toLong(), AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED.toLong())
    }

    @Test
    fun isAllowedToSubmitWithOverrides_notLockedAssignment_returnsTrue() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = false
        )
        val course = Course()

        assertEquals(true, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithNoOverrides_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = false
        )
        val course = Course()

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithNoCourse_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true
        )

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(null))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithNoEnrollments_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true
        )
        val course = Course(enrollments = null)

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithEmptyEnrollments_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true
        )
        val course = Course(enrollments = mutableListOf())

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithNoStudentEnrollments_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true
        )
        val course = Course(enrollments = mutableListOf(
            Enrollment(courseSectionId = 123L, type = Enrollment.EnrollmentType.Teacher)
        ))

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithValidSectionOverride_returnsTrue() {
        val currentTime = Calendar.getInstance()
        val futureTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        val futureDate = Date(futureTime.timeInMillis)

        val sectionId = 52878L
        val enrollment = Enrollment(
            courseSectionId = sectionId,
            type = Enrollment.EnrollmentType.Student
        )

        val override = AssignmentOverride(
            id = 1L,
            courseSectionId = sectionId,
            lockAt = futureDate
        )

        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true,
            overrides = mutableListOf(override)
        )

        val course = Course(enrollments = mutableListOf(enrollment))

        assertEquals(true, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithExpiredSectionOverride_returnsFalse() {
        val pastTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
        val pastDate = Date(pastTime.timeInMillis)

        val sectionId = 52878L
        val enrollment = Enrollment(
            courseSectionId = sectionId,
            type = Enrollment.EnrollmentType.Student
        )

        val override = AssignmentOverride(
            id = 1L,
            courseSectionId = sectionId,
            lockAt = pastDate
        )

        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true,
            overrides = mutableListOf(override)
        )

        val course = Course(enrollments = mutableListOf(enrollment))

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithFutureUnlockDate_returnsFalse() {
        val futureUnlockTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        val futureUnlockDate = Date(futureUnlockTime.timeInMillis)

        val sectionId = 52878L
        val enrollment = Enrollment(
            courseSectionId = sectionId,
            type = Enrollment.EnrollmentType.Student
        )

        val override = AssignmentOverride(
            id = 1L,
            courseSectionId = sectionId,
            unlockAt = futureUnlockDate
        )

        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true,
            overrides = mutableListOf(override)
        )

        val course = Course(enrollments = mutableListOf(enrollment))

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithValidDueDateOverride_returnsTrue() {
        val futureTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        val futureDate = Date(futureTime.timeInMillis)

        val sectionId = 52878L
        val enrollment = Enrollment(
            courseSectionId = sectionId,
            type = Enrollment.EnrollmentType.Student
        )

        val override = AssignmentOverride(
            id = 1L,
            courseSectionId = sectionId
        )

        val dueDate = AssignmentDueDate(
            id = 1L,
            lockAt = futureDate.toApiString()
        )

        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true,
            overrides = mutableListOf(override),
            allDates = mutableListOf(dueDate)
        )

        val course = Course(enrollments = mutableListOf(enrollment))

        assertEquals(true, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithMultipleSections_firstSectionValid_returnsTrue() {
        val futureTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        val futureDate = Date(futureTime.timeInMillis)

        val section1 = 111L
        val section2 = 222L

        val enrollment1 = Enrollment(courseSectionId = section1, type = Enrollment.EnrollmentType.Student)
        val enrollment2 = Enrollment(courseSectionId = section2, type = Enrollment.EnrollmentType.Student)

        val override1 = AssignmentOverride(id = 1L, courseSectionId = section1, lockAt = futureDate)
        val override2 = AssignmentOverride(id = 2L, courseSectionId = section2)

        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true,
            overrides = mutableListOf(override1, override2)
        )

        val course = Course(enrollments = mutableListOf(enrollment1, enrollment2))

        assertEquals(true, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_quizSubmission_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_QUIZ.apiString),
            lockedForUser = false
        )
        val course = Course()

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_attendanceSubmission_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ATTENDANCE.apiString),
            lockedForUser = false
        )
        val course = Course()

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_noSubmissionType_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.NONE.apiString),
            lockedForUser = false
        )
        val course = Course()

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_onPaperSubmission_returnsFalse() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ON_PAPER.apiString),
            lockedForUser = false
        )
        val course = Course()

        assertEquals(false, assignment.isAllowedToSubmitWithOverrides(course))
    }

    @Test
    fun isAllowedToSubmitWithOverrides_lockedWithSectionOverrideMatchingBoth_returnsTrue() {
        val pastUnlockTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -10) }
        val pastUnlockDate = Date(pastUnlockTime.timeInMillis)

        val futureLockTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 10) }
        val futureLockDate = Date(futureLockTime.timeInMillis)

        val sectionId = 52878L
        val enrollment = Enrollment(
            courseSectionId = sectionId,
            type = Enrollment.EnrollmentType.Student
        )

        val override = AssignmentOverride(
            id = 1L,
            courseSectionId = sectionId,
            unlockAt = pastUnlockDate,
            lockAt = futureLockDate
        )

        val assignment = Assignment(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString),
            lockedForUser = true,
            hasOverrides = true,
            overrides = mutableListOf(override)
        )

        val course = Course(enrollments = mutableListOf(enrollment))

        assertEquals(true, assignment.isAllowedToSubmitWithOverrides(course))
    }

}

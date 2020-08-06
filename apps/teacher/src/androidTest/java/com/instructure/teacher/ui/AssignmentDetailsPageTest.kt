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
 */
package com.instructure.teacher.ui

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvas.espresso.mockCanvas.utils.Randomizer
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_TEXT_ENTRY
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_UPLOAD
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_URL
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ON_PAPER
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.TestRail
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class AssignmentDetailsPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3109579")
    override fun displaysPageObjects() {
        getToAssignmentDetailsPage(
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                students = 1,
                withSubmission = true)
        assignmentDetailsPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3109579")
    fun displaysCorrectDetails() {
        val assignment = getToAssignmentDetailsPage()
        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    @Test
    @TestRail(ID = "C3109579")
    fun displaysInstructions() {
        getToAssignmentDetailsPage(withDescription = true)
        assignmentDetailsPage.assertDisplaysInstructions()
    }

    @Test
    @TestRail(ID = "C3134480")
    fun displaysNoInstructionsMessage() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertDisplaysNoInstructionsView()
    }

    @Test
    @TestRail(ID = "C3134481")
    fun displaysClosedAvailability() {
        getToAssignmentDetailsPage(lockAt = 7.days.ago.iso8601)
        assignmentDetailsPage.assertAssignmentClosed()
    }

    @Test
    @TestRail(ID = "C3134482")
    fun displaysNoFromDate() {
        val lockAt = 7.days.fromNow.iso8601
        getToAssignmentDetailsPage(lockAt = lockAt)
        assignmentDetailsPage.assertToFilledAndFromEmpty()
    }

    @Test
    @TestRail(ID = "C3134483")
    fun displaysNoToDate() {
        getToAssignmentDetailsPage(unlockAt = 7.days.ago.iso8601)
        assignmentDetailsPage.assertFromFilledAndToEmpty()
    }

    @Test
    fun displaysSubmissionTypeNone() {
        getToAssignmentDetailsPage(submissionTypes = listOf(SubmissionType.NONE))
        assignmentDetailsPage.assertSubmissionTypeNone()
    }

    @Test
    fun displaysSubmissionTypeOnPaper() {
        getToAssignmentDetailsPage(submissionTypes = listOf(ON_PAPER))
        assignmentDetailsPage.assertSubmissionTypeOnPaper()
    }

    @Test
    fun displaysSubmissionTypeOnlineTextEntry() {
        getToAssignmentDetailsPage(submissionTypes = listOf(ONLINE_TEXT_ENTRY))
        assignmentDetailsPage.assertSubmissionTypeOnlineTextEntry()
    }

    @Test
    fun displaysSubmissionTypeOnlineUrl() {
        getToAssignmentDetailsPage(submissionTypes = listOf(ONLINE_URL))
        assignmentDetailsPage.assertSubmissionTypeOnlineUrl()
    }

    @Test
    fun displaysSubmissionTypeOnlineUpload() {
        getToAssignmentDetailsPage(submissionTypes = listOf(ONLINE_UPLOAD))
        assignmentDetailsPage.assertSubmissionTypeOnlineUpload()
    }

    @Test
    fun displaysSubmittedDonut() {
        getToAssignmentDetailsPage(
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                students = 1,
                withSubmission = true)
        assignmentDetailsPage.assertHasSubmitted()
    }

    @Test
    fun displaysNotSubmittedDonut() {
        getToAssignmentDetailsPage(students = 1)
        assignmentDetailsPage.assertNotSubmitted()
    }

    private fun getToAssignmentDetailsPage(
            withDescription: Boolean = false,
            lockAt: String? = null,
            unlockAt: String? = null,
            submissionTypes: List<SubmissionType> = emptyList(),
            students: Int = 0,
            dueAt: String? = null,
            withSubmission: Boolean = false): Assignment {

        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1, studentCount = students)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = if(submissionTypes.isEmpty()) SubmissionType.ONLINE_TEXT_ENTRY else submissionTypes.first(),
                lockAt = lockAt,
                unlockAt = unlockAt,
                description = if(withDescription) Randomizer.randomCourseDescription() else "",
                dueAt = dueAt
        )

        if(withSubmission) {
            if(students == 0) {
                throw Exception("Can't have withSubmission == true and student count == 0")
            }
            if(!submissionTypes.contains(ONLINE_TEXT_ENTRY)) {
                throw Exception("If withSubmission == true, ONLINE_TEXT_ENTRY needs to be allowed")
            }
            data.addSubmissionForAssignment(
                    assignmentId = assignment.id,
                    userId = data.students[0].id,
                    type = "online_text_entry",
                    body = "A submission"
            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.waitForRender()
        return assignment
    }

}

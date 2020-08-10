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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class AssignmentSubmissionListPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertPageObjects()
    }

    @Test
    fun displaysNoSubmissionsView() {
        goToAssignmentSubmissionListPage(
                students = 0,
                submissions = 0
        )
        assignmentSubmissionListPage.assertDisplaysNoSubmissionsView()
    }

    @Test
    fun filterLateSubmissions() {
        goToAssignmentSubmissionListPage(
                dueAt = 7.days.ago.iso8601
        )
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmissions()
        assignmentSubmissionListPage.clickFilterSubmittedLate()
        assignmentSubmissionListPage.clickFilterDialogOk()
        assignmentSubmissionListPage.assertDisplaysClearFilter()
        assignmentSubmissionListPage.assertFilterLabelText(R.string.submitted_late)
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun filterUngradedSubmissions() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmissions()
        assignmentSubmissionListPage.clickFilterUngraded()
        assignmentSubmissionListPage.clickFilterDialogOk()
        assignmentSubmissionListPage.assertDisplaysClearFilter()
        assignmentSubmissionListPage.assertFilterLabelText(R.string.havent_been_graded)
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun displaysAssignmentStatusSubmitted() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertSubmissionStatusSubmitted()
    }

    @Test
    fun displaysAssignmentStatusMissing() {
        goToAssignmentSubmissionListPage(
                students = 1,
                submissions = 0,
                dueAt = 1.days.ago.iso8601
        )
        assignmentSubmissionListPage.assertSubmissionStatusMissing()
    }

    @Test
    fun displaysAssignmentStatusNotSubmitted() {
        goToAssignmentSubmissionListPage(
                students = 1,
                submissions = 0
        )
        assignmentSubmissionListPage.assertSubmissionStatusNotSubmitted()
    }

    @Test
    fun displaysAssignmentStatusLate() {
        goToAssignmentSubmissionListPage(
                dueAt = 7.days.ago.iso8601
        )
        assignmentSubmissionListPage.assertSubmissionStatusLate()
    }

    @Test
    fun messageStudentsWho() {
        val data = goToAssignmentSubmissionListPage(
                students = 1
        )
        val student = data.students[0]
        assignmentSubmissionListPage.clickAddMessage()
        addMessagePage.assertPageObjects()
        addMessagePage.assertHasStudentRecipient(student)
    }

    private fun goToAssignmentSubmissionListPage(
            students: Int = 1,
            submissions: Int = 1,
            dueAt: String? = null
    ): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = students, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()
        val teacher = data.teachers[0]

        // TODO: Make this part of MockCanvas.init()
        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                dueAt = dueAt
        )

        for (s in 0 until submissions) {
            if(students == 0) {
                throw Exception("Can't specify submissions without students")
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
        assignmentDetailsPage.openSubmissionsPage()

        return data
    }
}

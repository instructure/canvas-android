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
import com.instructure.canvasapi2.models.Assignment.SubmissionType.EXTERNAL_TOOL
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_TEXT_ENTRY
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_URL
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ON_PAPER
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class SpeedGraderPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderPage()
        speedGraderPage.assertPageObjects()
    }

    @Test
    fun displaysSubmissionDropDown() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY, students = 1, submissions = listOf(2))
        speedGraderPage.assertHasSubmissionDropDown()
    }

    @Test
    fun displaySubmissionPickerDialog() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY, students = 1, submissions = listOf(2))
        speedGraderPage.openSubmissionsDialog()
        speedGraderPage.assertSubmissionDialogDisplayed()
    }

    @Test
    fun opensToCorrectSubmission() {
        val data = goToSpeedGraderPage(students = 4, submissionType = ONLINE_TEXT_ENTRY)
        val students = data.students
        for (i in 0 until students.size) {
            val student = students[i]
            assignmentSubmissionListPage.clickSubmission(student)
            speedGraderPage.assertGradingStudent(student)
            speedGraderPage.clickBackButton()
        }
    }

    @Test
    fun hasCorrectPageCount() {
        goToSpeedGraderPage(students = 4)
        speedGraderPage.assertPageCount(4)
    }

    /* TODO: Uncomment and implement if we come up with a way to create/modify submissions dates
    @Test
    fun displaysSelectedSubmissionInDropDown() {
        goToSpeedGraderPage()
        speedGraderPage.openSubmissionsDialog()
        getNextSubmission()
        val submission = getNextSubmission()
        speedGraderPage.selectSubmissionFromDialog(submission)
        speedGraderPage.assertSubmissionSelected(submission)
    }
    */

    @Test
    fun displaysTextSubmission() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY, submissions = listOf(1))
        speedGraderPage.assertDisplaysTextSubmissionView()
    }

    @Test
    fun displaysUnsubmittedEmptyState() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY)
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    @Test
    fun displaysNoSubmissionsAllowedEmptyState() {
        goToSpeedGraderPage(submissionType = Assignment.SubmissionType.NONE)
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderNoneMessage)
    }

    @Test
    fun displaysOnPaperEmptyState() {
        goToSpeedGraderPage(submissionType = ON_PAPER)
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderOnPaperMessage)
    }

    @Test
    fun displaysExternalToolEmptyState() {
        goToSpeedGraderPage(submissionType = EXTERNAL_TOOL)
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    @Test
    fun displaysUrlSubmission() {
        val data = goToSpeedGraderPage(submissionType = ONLINE_URL, submissions = listOf(1))
        val assignment = data.assignments.values.first()
        val submission = data.submissions[assignment.id]!!.first()
        speedGraderPage.assertDisplaysUrlSubmissionLink(submission)
        speedGraderPage.assertDisplaysUrlWebView()
    }

    private fun goToSpeedGraderPage(
            students: Int = 1,
            submissionType: Assignment.SubmissionType = Assignment.SubmissionType.NONE,
            submissions: List<Int> = listOf(0),
            selectStudent: Int = 0
    ): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = students, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = submissionType
        )

        val assignmentSubmissions =
                (0 until submissions.size).map {
                    if(students < it + 1) throw Exception("student count does not agree with submissions")
                    val student = data.students[it]
                    val submissionCount = submissions[it]
                    repeat(submissionCount) { index ->
                        data.addSubmissionForAssignment(
                                assignmentId = assignment.id,
                                userId = student.id,
                                type = submissionType.apiString,
                                body = if(submissionType == Assignment.SubmissionType.ONLINE_URL) null else "AssignmentBody $index",
                                url = if(submissionType == Assignment.SubmissionType.ONLINE_URL) "www.google.com" else null
                        )
                    }
                }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(data.students[selectStudent])

        return data
    }
}

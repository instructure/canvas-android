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

import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionListApiModel
import com.instructure.espresso.ditto.Ditto
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.SubmissionType.*
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class SpeedGraderPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        goToSpeedGraderPage()
        speedGraderPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysSubmissionDropDown() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY, students = 1, submissions = listOf(2))
        speedGraderPage.assertHasSubmissionDropDown()
    }

    @Test
    @Ditto
    fun displaySubmissionPickerDialog() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY, students = 1, submissions = listOf(2))
        speedGraderPage.openSubmissionsDialog()
        speedGraderPage.assertSubmissionDialogDisplayed()
    }

    @Test
    @Ditto
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
    @Ditto
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
    @Ditto
    fun displaysTextSubmission() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY, submissions = listOf(1))
        speedGraderPage.assertDisplaysTextSubmissionView()
    }

    @Test
    @Ditto
    fun displaysUnsubmittedEmptyState() {
        goToSpeedGraderPage(submissionType = ONLINE_TEXT_ENTRY)
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    @Test
    @Ditto
    fun displaysNoSubmissionsAllowedEmptyState() {
        goToSpeedGraderPage(submissionType = NO_TYPE)
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderNoneMessage)
    }

    @Test
    @Ditto
    fun displaysOnPaperEmptyState() {
        goToSpeedGraderPage(submissionType = ON_PAPER)
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderOnPaperMessage)
    }

    @Test
    @Ditto
    fun displaysExternalToolEmptyState() {
        goToSpeedGraderPage(submissionType = EXTERNAL_TOOL)
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    // Ditto doesn't support WebViews
    @Test
    fun displaysUrlSubmission() {
        val submission = goToSpeedGraderPage(submissionType = ONLINE_URL, submissions = listOf(1)).submissions.submissionList[0]
        speedGraderPage.assertDisplaysUrlSubmissionLink(submission)
        speedGraderPage.assertDisplaysUrlWebView()
    }

    private fun goToSpeedGraderPage(
            students: Int = 1,
            submissionType: SubmissionType = SubmissionType.NO_TYPE,
            submissions: List<Int> = listOf(0),
            selectStudent: Int = 0
    ): SpeedGraderPageData {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = students)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val assignment = seedAssignments(
                assignments = 1,
                courseId = course.id,
                submissionTypes = listOf(submissionType),
                teacherToken = teacher.token).assignmentList[0]

        val assignmentSubmissions =
                (0 until submissions.size).map {
                    seedAssignmentSubmission(
                            submissionSeeds = listOf(
                                    SubmissionsApi.SubmissionSeedInfo(submissionType = submissionType, amount = submissions[it])
                            ),
                            assignmentId = assignment.id,
                            courseId = course.id,
                            studentToken = data.studentsList[it].token
                    )
                }

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(data.studentsList[selectStudent])

        return SpeedGraderPageData(
                submissions = assignmentSubmissions[0],
                students = data.studentsList
        )
    }
}

data class SpeedGraderPageData(
        val submissions: SubmissionListApiModel,
        val students: List<CanvasUserApiModel>
)

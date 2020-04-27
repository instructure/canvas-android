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

import android.util.Log
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.dataseeding.model.SubmissionType.ONLINE_TEXT_ENTRY
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class AssignmentSubmissionListPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysNoSubmissionsView() {
        goToAssignmentSubmissionListPage(
                students = 0,
                submissions = 0
        )
        assignmentSubmissionListPage.assertDisplaysNoSubmissionsView()
    }

    @Test
    @Ditto
    fun filterLateSubmissions() {
        goToAssignmentSubmissionListPage(
                dueAt = 7.days.ago.iso8601,
                checkForLateStatus = true
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
    @Ditto
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
    @Ditto
    fun displaysAssignmentStatusSubmitted() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertSubmissionStatusSubmitted()
    }

    @Test
    @Ditto
    fun displaysAssignmentStatusMissing() {
        goToAssignmentSubmissionListPage(
                students = 1,
                submissions = 0,
                dueAt = 1.days.ago.iso8601
        )
        assignmentSubmissionListPage.assertSubmissionStatusMissing()
    }

    @Test
    @Ditto
    fun displaysAssignmentStatusNotSubmitted() {
        goToAssignmentSubmissionListPage(
                students = 1,
                submissions = 0
        )
        assignmentSubmissionListPage.assertSubmissionStatusNotSubmitted()
    }

    @Test
    @Ditto
    fun displaysAssignmentStatusLate() {
        goToAssignmentSubmissionListPage(
                dueAt = 7.days.ago.iso8601
        )
        assignmentSubmissionListPage.assertSubmissionStatusLate()
    }

    @Test
    @Ditto
    fun messageStudentsWho() {
        val data = goToAssignmentSubmissionListPage(
                students = 1
        )
        val student = data.studentsList[0]
        assignmentSubmissionListPage.clickAddMessage()
        addMessagePage.assertPageObjects()
        addMessagePage.assertHasStudentRecipient(student)
    }

    private fun goToAssignmentSubmissionListPage(
            students: Int = 1,
            submissions: Int = 1,
            dueAt: String = "",
            checkForLateStatus: Boolean = false
    ): SeedApi.SeededDataApiModel {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = students)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val assignment = seedAssignments(
                courseId = course.id,
                assignments = 1,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                dueAt = dueAt,
                teacherToken = teacher.token).assignmentList[0]

        for (s in 0 until submissions) {
            seedAssignmentSubmission(
                    listOf(
                            SubmissionsApi.SubmissionSeedInfo(
                                    amount = 1,
                                    submissionType = ONLINE_TEXT_ENTRY,
                                    checkForLateStatus = checkForLateStatus)
                    ),
                    assignment.id,
                    course.id,
                    data.studentsList[s].token)
        }

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()

        return data
    }
}

/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssignmentE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testAssignmentsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 3, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]
        val gradedStudent = data.studentsList[1]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course.")
        dashboardPage.openCourse(course.name)

        Log.d(STEP_TAG,"Navigate to ${course.name} course's Assignments Tab and assert that there isn't any assignment displayed.")
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.assertDisplaysNoAssignmentsView()

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        Log.d(STEP_TAG,"Refresh Assignment List Page and assert that the previously seeded ${assignment[0].name} assignment has been displayed.")
        assignmentListPage.refresh()
        assignmentListPage.assertHasAssignment(assignment[0])

        Log.d(STEP_TAG,"Click on ${assignment[0].name} assignment and assert the numbers of 'Not Submitted' and 'Needs Grading' submissions.")
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(3,3)
        assignmentDetailsPage.assertNeedsGrading(0,3)

        Log.d(PREPARATION_TAG,"Seed a submission for ${student.name} student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = student.token
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that because of the previously seeded submission, the number of 'Needs Grading' is increased and the number of 'Not Submitted' is decreased.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(2,3)
        assignmentDetailsPage.assertNeedsGrading(1,3)

        Log.d(PREPARATION_TAG,"Seed a submission for ${gradedStudent.name} student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for ${gradedStudent.name} student.")
        SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = assignment[0].id,
                studentId = gradedStudent.id,
                postedGrade = "15",
                excused = false
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that the number of 'Graded' is increased and the number of 'Not Submitted' and 'Needs Grading' are decreased.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(1,3)
        assignmentDetailsPage.assertNeedsGrading(1,3)
        assignmentDetailsPage.assertHasGraded(1,3)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testEditAssignmentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to ${course.name} course's Assignments Tab and assert that there isn't any assignment displayed.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.assertDisplaysNoAssignmentsView()

        Log.d(PREPARATION_TAG,"Seeding assignment.")
        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        Log.d(STEP_TAG,"Refresh Assignment List Page and assert that the previously seeded ${assignment[0].name} assignment has been displayed.")
        assignmentListPage.refresh()
        assignmentListPage.assertHasAssignment(assignment[0])

        Log.d(STEP_TAG,"Click on ${assignment[0].name} assignment and assert the numbers of 'Not Submitted' and 'Needs Grading' submissions.")
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentDetails(assignment[0])
        assignmentDetailsPage.assertNotSubmitted(1,1)
        assignmentDetailsPage.assertNeedsGrading(0,1)
        assignmentDetailsPage.assertSubmissionTypeOnlineTextEntry()

        val newAssignmentName = "New Assignment Name"
        Log.d(STEP_TAG,"Edit ${assignment[0].name} assignment's name  to: $newAssignmentName.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickAssignmentNameEditText()
        editAssignmentDetailsPage.editAssignmentName(newAssignmentName)

        Log.d(STEP_TAG,"Refresh the page. Assert that the name has been changed to $newAssignmentName.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentNameChanged(newAssignmentName)

        Log.d(STEP_TAG,"Edit $newAssignmentName assignment's points to 20.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPointsPossibleEditText()
        editAssignmentDetailsPage.editAssignmentPoints(20.0)

        Log.d(STEP_TAG,"Refresh the page. Assert that the points of $newAssignmentName assignment has been changed to 20.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentPointsChanged("20")

        Log.d(STEP_TAG,"Publish $newAssignmentName assignment. Click on Save.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()

        Log.d(STEP_TAG,"Refresh the page. Assert that $newAssignmentName assignment has been published.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertPublishedStatus(false)
    }

}
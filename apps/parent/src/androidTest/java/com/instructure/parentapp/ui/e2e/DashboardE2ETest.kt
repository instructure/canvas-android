/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DashboardE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E, SecondaryFeatureCategory.ADD_STUDENT)
    fun testDashboardAddStudentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG, "Create a brand new canvas user and enroll it to the '${course.name}' as a Student.")
        val newStudent = UserApi.createCanvasUser()
        EnrollmentsApi.enrollUserAsStudent(course.id, newStudent.id)

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Generate a pairing code for the '${newStudent.name}' (new) student to be able to pair with an observer.")
        val responsePairingCodeObject = UserApi.postGeneratePairingCode(newStudent.id)

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(STEP_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        Log.d(STEP_TAG, "Open the student selector.")
        dashboardPage.openStudentSelector()

        Log.d(ASSERTION_TAG, "Assert that the 'Add Student' (+) icon is displayed and the '${student.shortName}' student is displayed and selected.")
        dashboardPage.assertSelectedStudent(student.shortName)
        dashboardPage.assertAddStudentDisplayed()

        Log.d(STEP_TAG, "Click on the 'Add Student' (+) icon.")
        dashboardPage.clickAddStudent()

        Log.d(ASSERTION_TAG, "Assert that the 'Add student with...' label and both the 'Pairing Code' and the 'QR Code' options are displayed on the Add Student (bottom) Page.")
        addStudentBottomPage.assertAddStudentWithLabel()
        addStudentBottomPage.assertPairingCodeOptionDisplayed()
        addStudentBottomPage.assertQRCodeOptionDisplayed()

        Log.d(STEP_TAG, "Click on the 'Pairing Code' to add a student via pairing code.")
        addStudentBottomPage.clickOnPairingCode()

        Log.d(STEP_TAG, "Enter the pairing code of the student and click on the 'OK' button to apply.")
        pairingCodePage.enterPairingCode(responsePairingCodeObject.pairingCode.toString())
        pairingCodePage.clickOkButton()
        composeTestRule.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the '${newStudent.shortName}' (new) student is displayed and selected as it is the recently added student.")
        dashboardPage.assertSelectedStudent(newStudent.shortName)

        Log.d(STEP_TAG, "Select '${student.shortName}' student.")
        dashboardPage.selectStudent(student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed.")
        coursesPage.assertCourseItemDisplayed(course)
    }
}
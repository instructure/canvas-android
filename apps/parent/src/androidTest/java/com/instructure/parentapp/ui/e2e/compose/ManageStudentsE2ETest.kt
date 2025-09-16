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
package com.instructure.parentapp.ui.e2e.compose

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
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
import com.instructure.parentapp.utils.extensions.seedData
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ManageStudentsE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MANAGE_STUDENTS, TestCategory.E2E, SecondaryFeatureCategory.ADD_STUDENT)
    fun testManageStudentsAddStudentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG, "Create a brand new canvas user and enroll it to the '${course.name}' as a Student.")
        val newStudent = UserApi.createCanvasUser()
        EnrollmentsApi.enrollUserAsStudent(course.id, newStudent.id)

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Generate a pairing code for the '${newStudent.name}' (new) student to be able to pair with an observer.")
        val responsePairingCodeObject = UserApi.postGeneratePairingCode(newStudent.id)

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        Thread.sleep(1000) // Need to wait a bit because sometimes it wants to open the left side menu too fast.

        Log.d(STEP_TAG, "Open the Left Side Menu by clicking on the hamburger icon on the top-left corner.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open the Manage Students Page.")
        leftSideNavigationDrawerPage.clickManageStudents()

        Log.d(ASSERTION_TAG, "Assert that the toolbar's title is 'Manage Students' so we are arrived to the Manage Students Page.")
        manageStudentsPage.assertToolbarTitle()

        Log.d(STEP_TAG, "Click on the 'Add Student' FAB (+) button on the bottom-right corner.")
        manageStudentsPage.tapAddStudent()

        Log.d(ASSERTION_TAG, "Assert that by default only the '${student.shortName}' student is displayed yet, the '${newStudent.shortName}' isn't because it's not added yet.")
        manageStudentsPage.assertStudentItemDisplayed(student.shortName)
        manageStudentsPage.assertStudentItemNotDisplayed(newStudent.shortName)

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

        Log.d(ASSERTION_TAG, "Assert that both the '${student.shortName}' and '${newStudent.shortName}' students are displayed yet.")
        manageStudentsPage.assertStudentItemDisplayed(student.shortName)
        manageStudentsPage.assertStudentItemDisplayed(newStudent.shortName)

        Log.d(STEP_TAG, "Select '${newStudent.shortName}' student.")
        manageStudentsPage.clickStudent(newStudent.shortName)

        Log.d(ASSERTION_TAG, "Assert that the student alert settings toolbar is displayed.")
        studentAlertSettingsPage.assertToolbarTitle()

        Log.d(STEP_TAG, "Click on the overflow menu and click on the 'DELETE' option of the overflow menu.")
        studentAlertSettingsPage.clickOverflowMenu()
        studentAlertSettingsPage.clickDeleteStudent()

        Log.d(ASSERTION_TAG, "Assert the details of the Delete Student dialog.")
        studentAlertSettingsPage.assertDeleteStudentDialogDetails()

        Log.d(STEP_TAG, "Click on the 'Delete' button of the pop-up dialog to delete the student.")
        studentAlertSettingsPage.clickDeleteStudentButton()

        Log.d(ASSERTION_TAG, "Assert that the '${newStudent.shortName}' student is not displayed in the list anymore since we just deleted it. Assert that the other student is still displayed.")
        manageStudentsPage.assertStudentItemDisplayed(student.shortName)
        manageStudentsPage.assertStudentItemNotDisplayed(newStudent.shortName)
    }
}
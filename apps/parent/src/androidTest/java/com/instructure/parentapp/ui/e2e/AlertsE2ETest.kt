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
 */
package com.instructure.parentapp.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.models.AlertType
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ViewUtils
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AlertsE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ALERTS, TestCategory.E2E)
    fun testAlertsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open the Manage Students Page.")
        leftSideNavigationDrawerPage.clickManageStudents()

        Log.d(STEP_TAG, "Open the Student Alert Settings Page and enable alerts")
        manageStudentsPage.clickStudent(student.shortName)
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_MISSING)
        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_ANNOUNCEMENT)
        studentAlertSettingsPage.clickThreshold(AlertType.INSTITUTION_ANNOUNCEMENT)
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("80")
        studentAlertSettingsPage.tapThresholdSaveButton()

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Open the Alerts Page.")
        dashboardPage.clickAlertsBottomMenu()

        Log.d(ASSERTION_TAG, "Assert that the Alerts Page is empty.")
        alertsPage.assertEmptyState()

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 20.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test Assignment")

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignment.name}' with 18 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "18")

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Refresh the Alerts Page")
        alertsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the 'Assignment Grade Above 80' alert is displayed.")
        alertsPage.assertAlertItemDisplayed("Assignment Grade Above 80")
        alertsPage.assertAlertItemDisplayed("Assignment graded: 18 on Test Assignment in ${course.courseCode}")

        Log.d(STEP_TAG, "Select the 'Assignment graded: 18 on Test Assignment in '${course.courseCode}' alert.")
        alertsPage.clickOnAlert("Assignment graded: 18 on Test Assignment in ${course.courseCode}")

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed.")
        assignmentDetailsPage.assertAssignmentTitle("Test Assignment")

        Log.d(STEP_TAG, "Navigate back to the alerts settings.")
        Espresso.pressBack()
        dashboardPage.openLeftSideMenu()
        leftSideNavigationDrawerPage.clickManageStudents()
        manageStudentsPage.clickStudent(student.shortName)

        Log.d(STEP_TAG, "Change some alert settings")
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.tapThresholdNeverButton()
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_LOW)
        studentAlertSettingsPage.enterThreshold("20")
        studentAlertSettingsPage.tapThresholdSaveButton()


        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignmentBelow = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 20.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test Assignment Below")

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignmentBelow.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignmentBelow.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignmentBelow.name}' with 1 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignmentBelow.id, student.id, postedGrade = "1")

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Navigate back to Alerts Page and refresh it.")
        ViewUtils.pressBackButton(2)
        alertsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the 'Assignment Grade Below 20' alert is displayed.")
        alertsPage.assertAlertItemDisplayed("Assignment Grade Below 20")
        alertsPage.assertAlertItemDisplayed("Assignment graded: 1 on Test Assignment Below in ${course.courseCode}")

        Log.d(STEP_TAG, "Dismiss the alerts")
        alertsPage.dismissAlert("Assignment graded: 1 on Test Assignment Below in ${course.courseCode}")
        alertsPage.dismissAlert("Assignment graded: 18 on Test Assignment in ${course.courseCode}")

        Log.d(ASSERTION_TAG, "Assert that the alerts are dismissed and refresh the page.")
        alertsPage.refresh()
        alertsPage.assertEmptyState()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ALERTS, TestCategory.E2E)
    fun testAlertsE2EMultipleStudents() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select student '${student.shortName}'.")
        dashboardPage.openStudentSelector()
        dashboardPage.selectStudent(student.shortName)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open the Manage Students Page.")
        leftSideNavigationDrawerPage.clickManageStudents()

        Log.d(STEP_TAG, "Open the Student Alert Settings Page and enable alerts")
        manageStudentsPage.clickStudent(student.shortName)
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("80")
        studentAlertSettingsPage.tapThresholdSaveButton()

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Open the Alerts Page.")
        dashboardPage.clickAlertsBottomMenu()

        Log.d(ASSERTION_TAG, "Assert that the Alerts Page is empty.")
        alertsPage.assertEmptyState()

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 20.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test Assignment")

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignment.name}' with 18 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "18")

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Refresh the Alerts Page")
        alertsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the 'Assignment Grade Above 80' alert is displayed.")
        alertsPage.assertAlertItemDisplayed("Assignment Grade Above 80")
        alertsPage.assertAlertItemDisplayed("Assignment graded: 18 on Test Assignment in ${course.courseCode}")

        val secondStudent = data.studentsList[1]
        Log.d(STEP_TAG, "Select student '${secondStudent.shortName}'.")
        dashboardPage.openStudentSelector()
        dashboardPage.selectStudent(secondStudent.shortName)

        Log.d(STEP_TAG, "Assert alerts are empty for the second student.")
        alertsPage.assertEmptyState()

        Log.d(STEP_TAG, "Change to the first student")
        dashboardPage.openStudentSelector()
        dashboardPage.selectStudent(student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the 'Assignment Grade Above 80' alert is displayed.")
        alertsPage.assertAlertItemDisplayed("Assignment Grade Above 80")
        alertsPage.assertAlertItemDisplayed("Assignment graded: 18 on Test Assignment in ${course.courseCode}")
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ALERTS, TestCategory.E2E)
    fun testAlertsSettingsE2EdontReceiveAlertsWhenAlertSettingsAreDisabled() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open the Manage Students Page.")
        leftSideNavigationDrawerPage.clickManageStudents()

        Log.d(STEP_TAG, "Open the Student Alert Settings Page and enable alerts")
        manageStudentsPage.clickStudent(student.shortName)
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_MISSING)
        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_ANNOUNCEMENT)
        studentAlertSettingsPage.clickThreshold(AlertType.INSTITUTION_ANNOUNCEMENT)

        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("80")
        studentAlertSettingsPage.tapThresholdSaveButton()

        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_LOW)
        studentAlertSettingsPage.enterThreshold("20")
        studentAlertSettingsPage.tapThresholdSaveButton()

        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("80")
        studentAlertSettingsPage.tapThresholdSaveButton()

        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_GRADE_LOW)
        studentAlertSettingsPage.enterThreshold("20")
        studentAlertSettingsPage.tapThresholdSaveButton()

        Log.d(STEP_TAG, "Reopen the alerts screen")
        Espresso.pressBack()
        manageStudentsPage.clickStudent(student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the alert settings are saved")
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.ASSIGNMENT_MISSING, true)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.COURSE_ANNOUNCEMENT, true)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.INSTITUTION_ANNOUNCEMENT, true)
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_HIGH, "80%")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_LOW, "20%")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_HIGH, "80%")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_LOW, "20%")

        Log.d(STEP_TAG, "Disable the alerts")
        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_MISSING)
        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_ANNOUNCEMENT)
        studentAlertSettingsPage.clickThreshold(AlertType.INSTITUTION_ANNOUNCEMENT)

        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.tapThresholdNeverButton()

        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_LOW)
        studentAlertSettingsPage.tapThresholdNeverButton()

        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_GRADE_HIGH)
        studentAlertSettingsPage.tapThresholdNeverButton()

        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_GRADE_LOW)
        studentAlertSettingsPage.tapThresholdNeverButton()

        Log.d(ASSERTION_TAG, "Assert that the alert settings are disabled")
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.ASSIGNMENT_MISSING, false)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.COURSE_ANNOUNCEMENT, false)
        studentAlertSettingsPage.assertSwitchThreshold(AlertType.INSTITUTION_ANNOUNCEMENT, false)
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_HIGH, "Never")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_LOW, "Never")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_HIGH, "Never")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_LOW, "Never")

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Open the Alerts Page.")
        dashboardPage.clickAlertsBottomMenu()

        Log.d(ASSERTION_TAG, "Assert that the Alerts Page is empty.")
        alertsPage.assertEmptyState()

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 20.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test Assignment")

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignment.name}' with 18 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "18")

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignmentBelow = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 20.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test Assignment Below")

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignmentBelow.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignmentBelow.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignmentBelow.name}' with 1 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignmentBelow.id, student.id, postedGrade = "1")

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Refresh the Alerts Page")
        alertsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the alerts are empty.")
        alertsPage.assertEmptyState()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ALERTS, TestCategory.E2E)
    fun testAlertsE2EUndoMechanism() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open the Manage Students Page.")
        leftSideNavigationDrawerPage.clickManageStudents()

        Log.d(STEP_TAG, "Open the Student Alert Settings Page and enable alerts")
        manageStudentsPage.clickStudent(student.shortName)

        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("80")
        studentAlertSettingsPage.tapThresholdSaveButton()

        studentAlertSettingsPage.clickThreshold(AlertType.ASSIGNMENT_GRADE_LOW)
        studentAlertSettingsPage.enterThreshold("20")
        studentAlertSettingsPage.tapThresholdSaveButton()

        studentAlertSettingsPage.clickThreshold(AlertType.COURSE_GRADE_HIGH)
        studentAlertSettingsPage.enterThreshold("80")
        studentAlertSettingsPage.tapThresholdSaveButton()

        Log.d(STEP_TAG, "Reopen the alerts screen")
        Espresso.pressBack()
        manageStudentsPage.clickStudent(student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the alert settings are saved")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_HIGH, "80%")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_LOW, "20%")
        studentAlertSettingsPage.assertPercentageThreshold(AlertType.COURSE_GRADE_HIGH, "80%")

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Open the Alerts Page.")
        dashboardPage.clickAlertsBottomMenu()

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 20.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test Assignment")

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignment.name}' with 18 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "18")

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignmentBelow = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 20.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test Assignment Below")

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignmentBelow.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignmentBelow.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignmentBelow.name}' with 1 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignmentBelow.id, student.id, postedGrade = "1")

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Refresh the Alerts Page")
        alertsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the alerts are displayed.")
        alertsPage.assertAlertItemDisplayed("Assignment Grade Below 20")
        alertsPage.assertAlertItemDisplayed("Assignment graded: 1 on Test Assignment Below in ${course.courseCode}")
        alertsPage.assertAlertItemDisplayed("Course Grade Above 80")
        alertsPage.assertAlertItemDisplayed("Course grade: 90.0% in ${course.courseCode}")
        alertsPage.assertAlertItemDisplayed("Assignment Grade Above 80")
        alertsPage.assertAlertItemDisplayed("Assignment graded: 18 on Test Assignment in ${course.courseCode}")

        Log.d(STEP_TAG, "Dismiss the TOP 1 alert.")
        alertsPage.dismissAlert("Assignment graded: 1 on Test Assignment Below in ${course.courseCode}")

        Log.d(STEP_TAG, "Click on the UNDO button in the snackbar.")
        alertsPage.clickUndo()

        Log.d(ASSERTION_TAG, "Assert that the alert is displayed again and it is no longer marked as unread" +
                "Assert that the other alerts remain unread.")
        alertsPage.assertAlertItemDisplayed("Assignment Grade Below 20")
        alertsPage.assertAlertItemDisplayed("Assignment graded: 1 on Test Assignment Below in ${course.courseCode}")
        alertsPage.assertAlertRead("Assignment graded: 1 on Test Assignment Below in ${course.courseCode}")
        alertsPage.assertAlertUnread("Course Grade Above 80")
        alertsPage.assertAlertUnread("Assignment Grade Above 80")

        Log.d(STEP_TAG, "Dismiss the 'Course Grade Above 80' alert.")
        alertsPage.dismissAlert("Course grade: 90.0% in ${course.courseCode}")

        Log.d(STEP_TAG, "Click on the UNDO button in the snackbar.")
        alertsPage.clickUndo()

        Log.d(ASSERTION_TAG, "Assert that the alert is displayed again and it is no longer marked as unread same as the TOP 1" +
                "Assert that the other alert (bottommost) remain unread.")
        alertsPage.assertAlertItemDisplayed("Course Grade Above 80")
        alertsPage.assertAlertItemDisplayed("Course grade: 90.0% in ${course.courseCode}")
        alertsPage.assertAlertRead("Assignment graded: 1 on Test Assignment Below in ${course.courseCode}")
        alertsPage.assertAlertRead("Course grade: 90.0% in ${course.courseCode}")
        alertsPage.assertAlertUnread("Assignment Grade Above 80")
    }

}
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
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class GradesListE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GRADES, TestCategory.E2E)
    fun testGradesListE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course submitted by the observed student but not graded by the teacher.")
        val submittedAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 30.0, dueAt = 2.days.ago.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Grade submission: '${testAssignment.name}' with 13 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "13")

        Log.d(PREPARATION_TAG,"Submit assignment: '${submittedAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, submittedAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course which is not submitted by the observed student.")
        val notSubmittedAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.ago.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on the '${course.name}' course and assert that the details of the course has opened.")
        coursesPage.clickCourseItem(course.name)
        courseDetailsPage.assertCourseNameDisplayed(course) //Course Details Page is actually the Grades page by default when there are no tabs.

        Log.d(ASSERTION_TAG, "Assert that the Grades Card text is 'Total' by default and the 'Based on graded assignments' label is displayed.")
        gradesPage.assertCardText("Total")
        gradesPage.assertBasedOnGradedAssignmentsLabel()

        Log.d(ASSERTION_TAG, "Assert that the group header 'Upcoming Assignments' is displayed since the '${testAssignment.name}' assignment's due date is in the future and it's already graded.")
        gradesPage.assertGroupHeaderIsDisplayed("Upcoming Assignments")

        Log.d(ASSERTION_TAG, "Assert that the group header 'Past Assignments' is displayed since the '${submittedAssignment.name}' assignment's due date is in the past and it's already submitted but hasn't graded yet.")
        gradesPage.assertGroupHeaderIsDisplayed("Past Assignments")

        Log.d(ASSERTION_TAG, "Assert that the group header 'Overdue Assignments' is displayed since the '${notSubmittedAssignment.name}' assignment's due date is in the past and there is no submission for '${student.name}' student.")
        gradesPage.assertGroupHeaderIsDisplayed("Overdue Assignments")

        Log.d(ASSERTION_TAG, "Assert that all the three seeded assignments are displayed.")
        gradesPage.assertAssignmentIsDisplayed(testAssignment.name)
        gradesPage.assertAssignmentIsDisplayed(submittedAssignment.name)
        gradesPage.assertAssignmentIsDisplayed(notSubmittedAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${testAssignment.name}' graded assignment's score (aka. grade text) is 13/15 and the Total grade is 86.67%.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = {
            composeTestRule.waitForIdle()
            gradesPage.refresh() })
        {
            gradesPage.assertAssignmentGradeText(testAssignment.name, "13/15")
            gradesPage.assertTotalGradeText("86.67%") //Sometimes the Total grade cannot be displayed because of API issues and even the retry logic does not solve it.
        }

        Log.d(ASSERTION_TAG, "Assert that none of the '${submittedAssignment.name}' and '${notSubmittedAssignment.name}' has valid score (aka. grade text) since one of the is submitted but not graded yet, the other not even submitted.")
        gradesPage.assertAssignmentGradeText(submittedAssignment.name, "-/30")
        gradesPage.assertAssignmentGradeText(notSubmittedAssignment.name, "-/15")

        Log.d(STEP_TAG, "Click on the '${testAssignment.name}' assignment to open it's details.")
        courseDetailsPage.clickAssignment(testAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${testAssignment.name}' assignment's title and details are displayed and the proper score (13) is displayed as well. Assert that the selected attempt is 'Attempt 1'.")
        assignmentDetailsPage.assertAssignmentTitle(testAssignment.name)
        assignmentDetailsPage.assertAssignmentDetails(testAssignment)
        assignmentDetailsPage.assertAssignmentGraded("13")

        Log.d(STEP_TAG, "Navigate back to the Assignment (Grades) List page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that there is no filter applied by default.")
        gradesPage.assertFilterNotApplied()

        Log.d(STEP_TAG, "Click on the 'Filter' icon (next to the total grade) and choose 'Group' filter option.")
        gradesPage.clickFilterButton()
        gradesPage.clickFilterOption("Group")

        Log.d(ASSERTION_TAG, "Assert the Grades Preferences (aka. Filter) screen labels and toolbar title.")
        gradesPage.assertGradesPreferencesFilterScreenLabels()

        Log.d(STEP_TAG, "Save the previously set filter.")
        gradesPage.clickSaveButton()

        Log.d(ASSERTION_TAG, "Assert that there is filter applied se we just set it recently.")
        gradesPage.assertFilterApplied()

        Log.d(ASSERTION_TAG, "Assert that only the 'Assignments' group header is displayed since we are filtering based on groups, and all of our items are Assignments.")
        gradesPage.assertGroupHeaderIsDisplayed("Assignments")

        Log.d(ASSERTION_TAG, "Assert that none of the 'Due Date' filter group headers are displayed since we are filtering based on 'Group' now.")
        gradesPage.assertGroupHeaderIsNotDisplayed("Upcoming Assignments")
        gradesPage.assertGroupHeaderIsNotDisplayed("Overdue Assignments")
        gradesPage.assertGroupHeaderIsNotDisplayed("Past Assignments")

        Log.d(STEP_TAG, "Turn off 'Based on graded assignments' toggle.")
        gradesPage.clickBasedOnGradedAssignments()

        Log.d(ASSERTION_TAG, "Assert that the Total grade is 21.67%, since we are counting missing and not graded assignments as well when calculating the Total Grade.")
        composeTestRule.waitForIdle()
        gradesPage.assertTotalGradeText("21.67%")
    }
}
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
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ViewUtils
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssignmentDetailsE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E, SecondaryFeatureCategory.ASSIGNMENT_DETAILS)
    fun testCourseAssignmentDetailsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG, "Grade submission: '${testAssignment.name}' with 13 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "13")

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        Log.d(STEP_TAG, "Open the student selector.")
        dashboardPage.openStudentSelector()

        Log.d(ASSERTION_TAG, "Assert that the 'Add student' selector is displayed.")
        dashboardPage.assertAddStudentDisplayed()

        Log.d(STEP_TAG, "Select the student which has a grade for its assignment submission.")
        dashboardPage.selectStudent(student.shortName)

        Log.d(STEP_TAG, "Click on the '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert that the details of the course has opened.")
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(STEP_TAG, "Click on the '${testAssignment.name}' assignment to open it's details.")
        retryWithIncreasingDelay {
            courseDetailsPage.clickAssignment(testAssignment.name)
        }

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Assignment Details' as the user is on the assignment details page and the subtitle is the '${course.name}' course's name.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that the '${testAssignment.name}' assignment's title and details are displayed and the proper score (13) is displayed as well. " +
                "Assert that the selected attempt is 'Attempt 1'.")
        assignmentDetailsPage.assertAssignmentTitle(testAssignment.name)
        assignmentDetailsPage.assertAssignmentDetails(testAssignment)
        assignmentDetailsPage.assertAssignmentGraded("13")
        assignmentDetailsPage.assertNoAttemptSpinner()
        assignmentDetailsPage.assertSelectedAttempt(1)

        Log.d(STEP_TAG, "Navigate back to the course list page of the selected student.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Select the other student, '${student2.name}', who does not have any grade (and submission) for the given assignment and select this student.")
        dashboardPage.openStudentSelector()
        dashboardPage.selectStudent(student2.shortName)

        Log.d(STEP_TAG, "Click on the '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert that the details of the course has opened.")
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(STEP_TAG, "Click on the '${testAssignment.name}' assignment to open it's details.")
        courseDetailsPage.clickAssignment(testAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Assignment Details' as the user is on the assignment details page and the subtitle is the '${course.name}' course's name.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that the assignment status for '${student2.name}' student is 'Not Submitted' and the 'Submission & Rubric' label is displayed and the submission type is 'Text Entry'.")
        assignmentDetailsPage.assertStatusNotSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()
        assignmentDetailsPage.assertSubmissionTypeDisplayed("Text Entry")
        assignmentDetailsPage.assertReminderViewDisplayed()
        assignmentDetailsPage.assertNoDescriptionViewDisplayed()
    }
}
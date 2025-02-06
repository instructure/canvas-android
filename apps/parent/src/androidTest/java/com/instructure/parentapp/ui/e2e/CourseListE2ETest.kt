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
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CourseListE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COURSE_LIST, TestCategory.E2E)
    fun testCourseListE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit assignment: '${testAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, testAssignment.id, submissionSeedsList = listOf(
            SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Thread.sleep(5000) // Allow the submission seeding to propagate

        Log.d(PREPARATION_TAG,"Grade submission: '${testAssignment.name}' with 13 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "13")

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(STEP_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        val selectedShortName = if (student.sortableName.compareTo(student2.sortableName) < 0) {
            student.shortName
        } else {
            student2.shortName
        }

        Log.d(ASSERTION_TAG, "Assert that by default the selected student is that the one is the first ordered by 'sortableName'.")
        dashboardPage.assertSelectedStudent(selectedShortName)

        Log.d(STEP_TAG, "Open the student selector and make sure that '${student.name}' student will be selected.")
        dashboardPage.openStudentSelector()
        dashboardPage.assertAddStudentDisplayed()

        val otherStudentName = if (selectedShortName == student.shortName) {
            student2.shortName
        } else {
            student.shortName
        }

        Log.d(STEP_TAG, "Select the other student which was not initially selected (default selection is based on 'sortable' name alphabetic order.")
        dashboardPage.selectStudent(otherStudentName)

        if (otherStudentName == student.shortName) {
            Log.d(ASSERTION_TAG, "Assert that grade is '86.67%' as there is only one graded assignment with 13 points out of 15 for the student of this course.")
            coursesPage.assertGradeTextDisplayed(course.name, "86.67%")
            dashboardPage.assertSelectedStudent(student.shortName)
        } else {
            Log.d(ASSERTION_TAG, "Assert that the 'No Grade' text is displayed in the course items because there is really no grade yet the course for any student.")
            coursesPage.assertGradeTextDisplayed(course.name, "No Grade")
            dashboardPage.assertSelectedStudent(student2.shortName)
        }

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed.")
        coursesPage.assertCourseItemDisplayed(course)

        Log.d(ASSERTION_TAG, "Assert that the course has its course code.")
        coursesPage.assertCourseCodeTextDisplayed(course.name, course.courseCode)

        Log.d(STEP_TAG, "Click on the '${course.name}' course and assert that the details of the course has opened.")
        coursesPage.clickCourseItem(course.name)
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(STEP_TAG, "Navigate back to the course list page of the selected student.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()
    }
}
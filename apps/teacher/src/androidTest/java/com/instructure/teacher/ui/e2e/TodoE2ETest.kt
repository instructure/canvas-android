/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedAssignmentSubmission
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class TodoE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testTodoE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val assignments = seedAssignments(courseId = course.id, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), teacherToken = teacher.token, pointsPossible = 15.0)

        Log.d(PREPARATION_TAG, "Seed a submission for '${assignments[0].name}' assignment with '${student.name}' student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )), assignmentId = assignments[0].id, courseId = course.id, studentToken = student.token)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to 'To Do' Page.")
        dashboardPage.openTodo()
        todoPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the previously seeded '${assignments[0].name}' assignment is displayed as a To Do element for the '${course.name}' course." +
                "Assert that the '1 Needs Grading' text is under the corresponding assignment's details, and assert that the To Do element count is 1.")
        todoPage.assertTodoElementDetailsDisplayed(course.name)
        todoPage.assertNeedsGradingCountOfTodoElement(assignments[0].name, 1)
        todoPage.assertTodoElementCount(1)

        Log.d(PREPARATION_TAG, "Grade the previously seeded submission for '${student.name}' student.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignments[0].id, student.id, postedGrade = "15")

        Log.d(ASSERTION_TAG, "Refresh the To Do Page. Assert that the empty view is displayed so that the To Do has disappeared because it has been graded.")
        todoPage.refresh()
        todoPage.assertEmptyView()
    }

}
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
package com.instructure.teacher.ui.e2e.classic

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.pressBackButton
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.seedAssignmentSubmission
import com.instructure.teacher.ui.utils.extensions.seedAssignments
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.seedQuizQuestion
import com.instructure.teacher.ui.utils.extensions.seedQuizSubmission
import com.instructure.teacher.ui.utils.extensions.seedQuizzes
import com.instructure.teacher.ui.utils.extensions.tokenLogin
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
        val testAssignment = assignments[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to 'To Do' Page.")
        dashboardPage.openTodo()
        todoPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed because there are no To Do items yet.")
        todoPage.assertEmptyView()

        Log.d(PREPARATION_TAG, "Seed a submission for '${testAssignment.name}' assignment with '${student.name}' student.")
        seedAssignmentSubmission(submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)), assignmentId = testAssignment.id, courseId = course.id, studentToken = student.token)

        Log.d(PREPARATION_TAG, "Seed a quiz for the '${course.name}' course. Also, seed a question into the quizzes and publish it to make it visible for users.")
        val testQuizList = seedQuizzes(courseId = course.id, quizzes = 1, withDescription = true, dueAt = 3.days.fromNow.iso8601, teacherToken = teacher.token, published = false)
        seedQuizQuestion(courseId = course.id, quizId = testQuizList.quizList[0].id, teacherToken = teacher.token)

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        dashboardPage.openDashboard()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Quizzes Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()

        val testQuiz = testQuizList.quizList[0]

        // region Make seeded quiz published manually

        // We need to make the seeded quiz manually published because if we seed it published by default, seeding a submission for it will be automatically 'Graded' status so won't be displayed among the 'To Do' items.
        Log.d(STEP_TAG, "Click on the '${testQuiz.title}' quiz.")
        quizListPage.clickQuiz(testQuiz.title)

        val newQuizTitle = "This is a new quiz"
        Log.d(STEP_TAG, "Open 'Edit' page and switch on the 'Published' checkbox, so publish the '$newQuizTitle' quiz. Click on 'Save'.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.switchPublish()
        editQuizDetailsPage.saveQuiz()

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that '$newQuizTitle' quiz has been unpublished.")
        quizDetailsPage.refresh()
        quizDetailsPage.assertQuizPublished()

        // endregion

        Log.d(PREPARATION_TAG, "Submit the '${testQuiz.title}' quiz.")
        seedQuizSubmission(courseId = course.id, quizId = testQuiz.id, studentToken = student.token)

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        pressBackButton(3)

        Log.d(STEP_TAG, "Navigate to 'To Do' Page.")
        dashboardPage.openTodo()
        todoPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the previously seeded '${testAssignment.name}' assignment is displayed as a To Do element for the '${course.name}' course." +
                "Assert that the '1 Needs Grading' text is under the corresponding assignment's details.")
        todoPage.assertTodoElementDetailsDisplayed(course.name, testAssignment.name)
        todoPage.assertNeedsGradingCountOfTodoElement(assignments[0].name, 1)

        Log.d(ASSERTION_TAG, "Assert that the previously seeded '${testQuiz.title}' quiz is displayed as a To Do element for the '${course.name}' course." +
                "Assert that the '1 Needs Grading' text is under the corresponding quiz's details.")
        todoPage.assertTodoElementDetailsDisplayed(course.name, testQuiz.title)
        todoPage.assertNeedsGradingCountOfTodoElement(testQuiz.title, 1)

        Log.d(ASSERTION_TAG, "Assert that the 'To Do' element count is 2, since we have a quiz and an assignment which needs to be graded.")
        todoPage.assertTodoElementCount(2)

        Log.d(PREPARATION_TAG, "Grade the previously seeded '${testAssignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "15")

        Log.d(STEP_TAG, "Refresh the To Do Page.")
        todoPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the 'To Do' element count is 1, since we just graded the '${testAssignment.id}' assignment but we haven't graded the '${testQuiz.title}' quiz yet.")
        todoPage.assertTodoElementCount(1)
    }

}
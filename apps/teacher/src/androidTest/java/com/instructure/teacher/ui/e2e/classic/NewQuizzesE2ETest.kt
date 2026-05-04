/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
import androidx.test.espresso.Espresso.pressBack
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.utils.pressBackButton
import com.instructure.dataseeding.api.NewQuizzesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class NewQuizzesE2ETest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun testNewQuizzesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val quizTitle = "New Quiz E2E Test"
        val quizInstructions = "Answer the following True/False questions."
        Log.d(PREPARATION_TAG, "Seed a NEW quiz with instructions for '${course.name}' course.")
        val quiz = NewQuizzesApi.createNewQuiz(
            courseId = course.id,
            token = teacher.token,
            title = quizTitle,
            instructions = quizInstructions,
            published = true
        )

        Log.d(PREPARATION_TAG, "Add a True/False question to '${quiz.title}' quiz (correct answer: True).")
        NewQuizzesApi.createTrueFalseQuestion(
            courseId = course.id,
            quizId = quiz.id,
            token = teacher.token,
            questionTitle = "True or False Question 1",
            questionText = "<p>The Earth is round.</p>",
            pointsPossible = 1.0,
            correctAnswer = true,
            position = 1
        )

        Log.d(PREPARATION_TAG, "Add a second True/False question to '${quiz.title}' quiz (correct answer: False).")
        NewQuizzesApi.createTrueFalseQuestion(
            courseId = course.id,
            quizId = quiz.id,
            token = teacher.token,
            questionTitle = "True or False Question 2",
            questionText = "<p>The sun is a planet.</p>",
            pointsPossible = 1.0,
            correctAnswer = false,
            position = 2
        )

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Quizzes Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()

        Log.d(ASSERTION_TAG, "Assert that '${quiz.title}' quiz is displayed on the Quiz List page.")
        quizListPage.assertHasQuiz(quiz.title)

        Log.d(STEP_TAG, "Open the search bar and search for '${quiz.title}'.")
        quizListPage.searchable.clickOnSearchButton()
        quizListPage.searchable.typeToSearchBar(quiz.title)

        Log.d(ASSERTION_TAG, "Assert that '${quiz.title}' quiz is displayed in search results.")
        quizListPage.assertHasQuiz(quiz.title)

        Log.d(STEP_TAG, "Clear the search bar.")
        quizListPage.searchable.clickOnClearSearchButton()

        Log.d(STEP_TAG, "Search for a non-existing quiz.")
        quizListPage.searchable.typeToSearchBar("Non-existing Quiz")

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed and '${quiz.title}' quiz is not displayed.")
        quizListPage.assertDisplaysNoQuizzesView()
        quizListPage.assertQuizNotDisplayed(quiz.title)

        Log.d(STEP_TAG, "Clear the search bar.")
        quizListPage.searchable.clickOnClearSearchButton()

        Log.d(ASSERTION_TAG, "Assert that '${quiz.title}' quiz is displayed again after clearing search.")
        quizListPage.assertHasQuiz(quiz.title)

        Log.d(STEP_TAG, "Click on '${quiz.title}' quiz to open it.")
        quizListPage.clickQuiz(quiz.title)

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details page is displayed with '${quiz.title}' title.")
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentName(quiz.title)

        Log.d(ASSERTION_TAG, "Assert that the quiz is published, the submission type is 'Quiz' and 1 out of 1 students has not submitted the quiz yet.")
        assignmentDetailsPage.assertPublishedStatus(published = true)
        assignmentDetailsPage.assertSubmissionTypes(R.string.canvasAPI_quiz)
        assignmentDetailsPage.assertNotSubmitted(actual = 1, outOf = 1)

        Log.d(STEP_TAG, "Open the New Quiz in the Build webview by clicking the 'Submission Types: Quiz' row.")
        assignmentDetailsPage.openNewQuizBuildView()

        Log.d(ASSERTION_TAG, "Assert that the Build webview is displayed.")
        newQuizWebViewPage.waitForWebView()
        newQuizWebViewPage.assertBuildViewDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the quiz title '${quiz.title}' and the instructions are displayed in the Build webview.")
        newQuizWebViewPage.assertTitleDisplayed(quiz.title)
        newQuizWebViewPage.assertInstructionsDisplayed(quizInstructions)

        Log.d(ASSERTION_TAG, "Assert that both True/False questions are displayed in the Build webview.")
        newQuizWebViewPage.assertQuestionDisplayed("The Earth is round.")
        newQuizWebViewPage.assertQuestionDisplayed("The sun is a planet.")

        Log.d(STEP_TAG, "Navigate back to the Assignment Details page.")
        pressBack()

        Log.d(STEP_TAG, "Open not-submitted submissions to view '${student.name}' in SpeedGrader before any submission.")
        assignmentDetailsPage.clickNotSubmittedSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(ASSERTION_TAG, "Assert that '${student.name}' has 'Not Submitted' status in SpeedGrader.")
        speedGraderPage.assertCurrentStudent(student.name)
        speedGraderPage.assertCurrentStudentStatus("Not Submitted")

        Log.d(ASSERTION_TAG, "Assert that the empty 'No Submission' view is displayed in SpeedGrader.")
        speedGraderPage.assertEmptyViewDisplayed()

        Log.d(STEP_TAG, "Navigate back to the Assignment Details page.")
        pressBackButton(2)

        Log.d(PREPARATION_TAG, "Grade '${student.name}' directly via Canvas API (New Quizzes submissions require an LTI session and cannot be seeded via REST).")
        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = course.id,
            assignmentId = quiz.id,
            studentId = student.id,
            postedGrade = "1"
        )

        Log.d(STEP_TAG, "Refresh Assignment Details and wait for the submission to be graded.")
        retryWithIncreasingDelay(times = 15, maxDelay = 3000, catchBlock = { assignmentDetailsPage.refresh() }) {
            assignmentDetailsPage.assertHasGraded(actual = 1, outOf = 1)
        }

        Log.d(STEP_TAG, "Open the graded submissions to view '${student.name}' result in SpeedGrader.")
        assignmentDetailsPage.clickGradedSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(ASSERTION_TAG, "Assert that '${student.name}' has 'Graded' status in SpeedGrader.")
        speedGraderPage.assertCurrentStudent(student.name)
        speedGraderPage.assertCurrentStudentStatus("Graded")
    }
}
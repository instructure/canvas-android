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
package com.instructure.student.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.utils.pressBackButton
import com.instructure.dataseeding.api.NewQuizzesApi
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class NewQuizzesE2ETest : StudentComposeTest() {
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

        val quizWithoutQuestionsDescription = "This quiz has no questions and should display an error."
        Log.d(PREPARATION_TAG, "Seed a NEW quiz for '${course.name}' course.")
        val quizWithoutQuestions = NewQuizzesApi.createNewQuiz(
            courseId = course.id,
            token = teacher.token,
            instructions = quizWithoutQuestionsDescription,
            published = true
        )

        Log.d(PREPARATION_TAG, "Seed another NEW quiz for '${course.name}' course.")
        val quizWithQuestion = NewQuizzesApi.createNewQuiz(
            courseId = course.id,
            token = teacher.token,
            published = true
        )

        Log.d(PREPARATION_TAG, "Add a first True/False question to '${quizWithQuestion.title}' quiz (correct answer: True).")
        NewQuizzesApi.createTrueFalseQuestion(
            courseId = course.id,
            quizId = quizWithQuestion.id,
            token = teacher.token,
            questionTitle = "True or False Question 1",
            questionText = "<p>The Earth is round.</p>",
            pointsPossible = 1.0,
            correctAnswer = true,
            position = 1
        )

        Log.d(PREPARATION_TAG, "Add a second True/False question to '${quizWithQuestion.title}' quiz (correct answer: False).")
        NewQuizzesApi.createTrueFalseQuestion(
            courseId = course.id,
            quizId = quizWithQuestion.id,
            token = teacher.token,
            questionTitle = "True or False Question 2",
            questionText = "<p>The sun is a planet.</p>",
            pointsPossible = 1.0,
            correctAnswer = false,
            position = 2
        )

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course and navigate to Quizzes Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectQuizzes()

        Log.d(ASSERTION_TAG, "Assert that both NEW quizzes ('${quizWithoutQuestions.title}' and '${quizWithQuestion.title}') are displayed.")
        quizListPage.assertNewQuizDisplayed(quizWithoutQuestions)
        quizListPage.assertNewQuizDisplayed(quizWithQuestion)

        Log.d(STEP_TAG, "Open the search bar and search for '${quizWithQuestion.title}' quiz.")
        quizListPage.searchable.clickOnSearchButton()
        quizListPage.searchable.typeToSearchBar(quizWithQuestion.title)

        Log.d(ASSERTION_TAG, "Assert that only '${quizWithQuestion.title}' quiz is displayed and the '${quizWithoutQuestions.title}' in NOT in search results.")
        quizListPage.assertNewQuizDisplayed(quizWithQuestion)
        quizListPage.assertNewQuizNotDisplayed(quizWithoutQuestions)

        Log.d(STEP_TAG, "Clear the search bar.")
        quizListPage.searchable.clickOnClearSearchButton()

        Log.d(ASSERTION_TAG, "Assert that both quizzes are displayed again after clearing search.")
        quizListPage.assertNewQuizDisplayed(quizWithoutQuestions)
        quizListPage.assertNewQuizDisplayed(quizWithQuestion)

        Log.d(STEP_TAG, "Search for a non-existing quiz.")
        quizListPage.searchable.typeToSearchBar("Non-existing Quiz")

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed and none of the quizzes are displayed.")
        quizListPage.assertEmptyStateDisplayed()
        quizListPage.assertNewQuizNotDisplayed(quizWithoutQuestions)
        quizListPage.assertNewQuizNotDisplayed(quizWithQuestion)

        Log.d(STEP_TAG, "Clear the search bar.")
        quizListPage.searchable.clickOnClearSearchButton()

        Log.d(ASSERTION_TAG, "Assert that both quizzes are displayed again.")
        quizListPage.assertNewQuizDisplayed(quizWithoutQuestions)
        quizListPage.assertNewQuizDisplayed(quizWithQuestion)

        Log.d(STEP_TAG, "Select '${quizWithoutQuestions.title}' quiz (the quiz without questions).")
        quizListPage.selectNewQuiz(quizWithoutQuestions)

        Log.d(ASSERTION_TAG, "Assert that we are on the Assignment Details page.")
        assignmentDetailsPage.assertDisplayToolbarTitle()

        Log.d(ASSERTION_TAG, "Assert that the submission type is 'Quiz'.")
        assignmentDetailsPage.assertSubmissionTypeDisplayed("Quiz")

        Log.d(STEP_TAG, "Click 'Open the Quiz' button.")
        assignmentDetailsPage.clickSubmit()

        Log.d(ASSERTION_TAG, "Assert that the NEW quiz is displayed in a webview (external LTI tool), that '${quizWithoutQuestions.title}' title is displayed in the toolbar with the description '$quizWithoutQuestionsDescription'.")
        newQuizWebViewPage.waitForWebView()
        canvasWebViewPage.assertTitle(quizWithoutQuestions.title)
        newQuizWebViewPage.assertDescriptionDisplayed(quizWithoutQuestionsDescription)

        Log.d(ASSERTION_TAG, "Assert that the error message is displayed in the webview because the quiz has no questions.")
        newQuizWebViewPage.assertErrorMessageDisplayed()

        Log.d(STEP_TAG, "Navigate back to the Quiz List page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Select '${quizWithQuestion.title}' quiz (the quiz with a question).")
        quizListPage.selectNewQuiz(quizWithQuestion)

        Log.d(ASSERTION_TAG, "Assert that we are on the Assignment Details page.")
        assignmentDetailsPage.assertDisplayToolbarTitle()

        Log.d(ASSERTION_TAG, "Assert that the submission type is 'Quiz'.")
        assignmentDetailsPage.assertSubmissionTypeDisplayed("Quiz")

        Log.d(STEP_TAG, "Click 'Open the Quiz' button.")
        assignmentDetailsPage.clickSubmit()

        Log.d(ASSERTION_TAG, "Assert that the NEW quiz is displayed in a webview (external LTI tool) with the '${quizWithQuestion.title}' title.")
        newQuizWebViewPage.waitForWebView()
        canvasWebViewPage.assertTitle(quizWithQuestion.title)

        Log.d(ASSERTION_TAG, "Assert that the 'Begin' button is displayed.")
        newQuizWebViewPage.assertBeginButtonDisplayed()

        Log.d(STEP_TAG, "Click the 'Begin' button to start the quiz.")
        newQuizWebViewPage.clickBeginButton()

        Log.d(ASSERTION_TAG, "Assert that both True/False questions are displayed.")
        newQuizWebViewPage.assertQuestionDisplayed("The Earth is round.")
        newQuizWebViewPage.assertQuestionDisplayed("The sun is a planet.")

        Log.d(STEP_TAG, "Navigate back to the Assignment Details page without submitting the quiz.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that we are back on the Assignment Details page.")
        assignmentDetailsPage.assertDisplayToolbarTitle()

        Log.d(STEP_TAG, "Click 'Open the Quiz' button again to re-open the quiz.")
        assignmentDetailsPage.clickSubmit()

        Log.d(ASSERTION_TAG, "Assert that the NEW quiz webview is displayed again.")
        newQuizWebViewPage.waitForWebView()

        Log.d(ASSERTION_TAG, "Assert that only the 'Resume' button is displayed (no 'Begin') since the quiz was already started but not submitted.")
        newQuizWebViewPage.assertResumeButtonDisplayed()

        Log.d(STEP_TAG, "Click the 'Resume' button to continue the quiz.")
        newQuizWebViewPage.clickResumeButton()

        Log.d(ASSERTION_TAG, "Assert that both True/False questions are displayed again after resuming.")
        newQuizWebViewPage.assertQuestionDisplayed("The Earth is round.")
        newQuizWebViewPage.assertQuestionDisplayed("The sun is a planet.")

        Log.d(STEP_TAG, "Select 'True' as the answer for the first question (correct answer).")
        newQuizWebViewPage.clickAnswerByTextAtPosition("True", 1)

        Log.d(STEP_TAG, "Select 'True' as the (incorrect) answer for the second question (correct answer is 'False').")
        newQuizWebViewPage.clickAnswerByTextAtPosition("True", 2)

        Log.d(STEP_TAG, "Click the 'Submit' button to submit the quiz.")
        newQuizWebViewPage.clickSubmitButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Confirm Submission' dialog is displayed with the confirmation message.")
        newQuizWebViewPage.assertSubmitConfirmationDisplayed()

        Log.d(STEP_TAG, "Click the 'Submit' button in the confirmation dialog to confirm the submission.")
        newQuizWebViewPage.clickConfirmSubmitButton()

        Log.d(ASSERTION_TAG, "Assert that the results page shows 'Out of 2 points' and '50%' since only the first question was answered correctly.")
        newQuizWebViewPage.assertResultScoreDisplayed("Out of 2 points")
        newQuizWebViewPage.assertResultPercentageDisplayed("50%")

        Log.d(STEP_TAG, "Navigate back to the Assignment Details page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Refresh the Assignment Details page. Assert that the assignment is graded with score '25' (50% of 50 pts) and 'Out of 50 pts' is displayed.")
        assignmentDetailsPage.refresh()
        retryWithIncreasingDelay(times = 15, maxDelay = 3000, catchBlock = { assignmentDetailsPage.refresh() }) {
            assignmentDetailsPage.assertAssignmentGraded("25")
            assignmentDetailsPage.assertOutOfTextDisplayed("Out of 50 pts")
        }

        Log.d(STEP_TAG, "Navigate to the Submission Details page.")
        assignmentDetailsPage.goToSubmissionDetails()

        Log.d(ASSERTION_TAG, "Assert that the Quiz Submission content is displayed on the Submission Details page.")
        submissionDetailsPage.assertNewQuizSubmissionDisplayed()

        Log.d(STEP_TAG, "Click the 'Open the Quiz' button on the Submission Details page.")
        submissionDetailsPage.clickOpenTheQuizButton()

        Log.d(ASSERTION_TAG, "Assert that the NEW quiz webview is displayed.")
        newQuizWebViewPage.waitForWebView()

        Log.d(ASSERTION_TAG, "Assert that the 'View Results' button is displayed since the quiz is already submitted.")
        newQuizWebViewPage.assertViewResultsButtonDisplayed()

        Log.d(STEP_TAG, "Click the 'View Results' button.")
        newQuizWebViewPage.clickViewResultsButton()

        Log.d(ASSERTION_TAG, "Assert that the results page shows 'Out of 2 points' and '50%'.")
        newQuizWebViewPage.assertResultScoreDisplayed("Out of 2 points")
        newQuizWebViewPage.assertResultPercentageDisplayed("50%")
    }
}
/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e

import android.util.Log
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.QuizAnswer
import com.instructure.dataseeding.model.QuizQuestion
import com.instructure.student.R
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.containsString
import org.junit.Test

@HiltAndroidTest
class QuizzesE2ETest: StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Stub("Grades screen has been redesigned, needs to be fixed in ticket MBL-19258")
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.PAGES, TestCategory.E2E)
    fun testQuizzesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a quiz for '${course.name}' course.")
        val quizUnpublished = QuizzesApi.createQuiz(course.id, teacher.token, published = false)

        Log.d(PREPARATION_TAG, "Seed another quiz for '${course.name}' with some questions.")
        val quizQuestions = makeQuizQuestions()

        Log.d(PREPARATION_TAG, "Publish the previously seeded quiz.")
        val quizPublished = QuizzesApi.createAndPublishQuiz(course.id, teacher.token, quizQuestions)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to Quizzes Page.")
        courseBrowserPage.selectQuizzes()

        Log.d(ASSERTION_TAG, "Assert that '${quizPublished.title}' published quiz is displayed and '${quizUnpublished.title}' unpublished quiz has not displayed and the quiz group name is displayed.")
        quizListPage.assertQuizDisplayed(quizPublished)
        quizListPage.assertQuizNotDisplayed(quizUnpublished)
        quizListPage.assertAssignmentQuizzesGroupDisplayed()

        Log.d(STEP_TAG, "Collapse the quiz group.")
        quizListPage.collapseAssignmentQuizzesGroup()

        Log.d(ASSERTION_TAG, "Assert that the '${quizPublished.title}' quiz is NOT displayed.")
        quizListPage.assertQuizNotDisplayed(quizPublished)

        Log.d(STEP_TAG, "Expand the quiz group.")
        quizListPage.expandAssignmentQuizzesGroup()

        Log.d(ASSERTION_TAG, "Assert that the '${quizPublished.title}' quiz is displayed again.")
        quizListPage.assertQuizDisplayed(quizPublished)

        Log.d(STEP_TAG, "Open the search bar and search for the '${quizUnpublished.title}' quiz.")
        quizListPage.searchable.clickOnSearchButton()
        quizListPage.searchable.typeToSearchBar(quizUnpublished.title)

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed.")
        quizListPage.assertEmptyStateDisplayed()

        Log.d(STEP_TAG, "Clear the search bar and search for the '${quizPublished.title}' quiz.")
        quizListPage.searchable.clickOnClearSearchButton()
        quizListPage.searchable.typeToSearchBar(quizUnpublished.title)

        Log.d(ASSERTION_TAG, "Assert that ONLY the '${quizPublished.title}' quiz is displayed in the search results page.")
        quizListPage.assertQuizDisplayed(quizPublished)
        quizListPage.assertQuizItemCount(1)

        Log.d(STEP_TAG, "Select '${quizPublished.title}' quiz.")
        quizListPage.selectQuiz(quizPublished)

        Log.d(ASSERTION_TAG, "Assert that the '${quizPublished.title}' quiz title is displayed on the quiz details page.")
        canvasWebViewPage.runTextChecks(WebViewTextCheck(locatorType = Locator.ID, locatorValue = "quiz_title", textValue = quizPublished.title))

        Log.d(ASSERTION_TAG, "Assert that the 'Take the Quiz' button is displayed on the quiz details page.")
        canvasWebViewPage.runTextChecks(
            WebViewTextCheck(
                locatorType = Locator.ID,
                locatorValue = "take_quiz_link",
                textValue = "Take the Quiz"
            )
        )

        Log.d(STEP_TAG, "Press 'Take the Quiz' button.")
        canvasWebViewPage.pressButton(locatorType = Locator.ID, locatorValue = "take_quiz_link")

        Log.d(STEP_TAG, "Enter answers to the questions.")
        Thread.sleep(2000) // Wait for the quiz to load
        for(question in quizQuestions) {
            Log.d(ASSERTION_TAG, "Assert that the following question is displayed: '${question.questionText}'.")
            quizTakingPage.assertQuestionDisplayed(question.id!!, question.questionText!!)
            if(question.questionType == "multiple_choice_question") {
                Log.d(STEP_TAG, "Choosing an answer for the following question: '${question.questionText}'.")
                quizTakingPage.selectAnyAnswer(question.id!!)
            }
        }

        Log.d(STEP_TAG, "Submit the '${quizPublished.title}' quiz.")
        quizTakingPage.submitQuiz()

        Thread.sleep(3000) // Wait for the quiz submission to finish.
        Log.d(ASSERTION_TAG, "Assert (on web) that the '${quizPublished.title}' quiz now has a history.")
        onWebView(withId(R.id.contentWebView))
                .withElement(findElement(Locator.ID, "quiz-submission-version-table"))
                .withContextualElement(findElement(Locator.CLASS_NAME, "desc"))
                .perform(webScrollIntoView())
                .check(webMatches(getText(),containsString("Attempt History")))
        onWebView(withId(R.id.contentWebView))
                .withElement(findElement(Locator.CLASS_NAME, "ic-Table--header-row"))
                .perform(webScrollIntoView())
                .check(webMatches(getText(),containsString("LATEST")))

        Log.d(STEP_TAG, "Navigate back to Course Browser Page.")
        ViewUtils.pressBackButton(3)

        Log.d(STEP_TAG, "Navigate to Grades Page.")
        courseBrowserPage.selectGrades()

        Log.d(ASSERTION_TAG, "Assert that the corresponding grade (10) is displayed for '${quizPublished.title}' quiz.")
        courseGradesPage.assertGradeDisplayed(withText(quizPublished.title), containsTextCaseInsensitive("10"))
    }

    private fun makeQuizQuestions() = listOf(
        QuizQuestion(
            questionText = "What's your favorite color?",
            questionType = "multiple_choice_question",
            pointsPossible = 5,
            answers = listOf(
                QuizAnswer(id = 1, weight = 0, text = "Red"),
                QuizAnswer(id = 1, weight = 1, text = "Blue"),
                QuizAnswer(id = 1, weight = 0, text = "Yellow")
            )
        ),
        QuizQuestion(
            questionText = "Who let the dogs out?",
            questionType = "multiple_choice_question",
            pointsPossible = 5,
            answers = listOf(
                QuizAnswer(id = 1, weight = 1, text = "Who Who Who-Who"),
                QuizAnswer(id = 1, weight = 0, text = "Who Who-Who-Who"),
                QuizAnswer(id = 1, weight = 0, text = "Who-Who Who-Who")
            )
        )
    )

}
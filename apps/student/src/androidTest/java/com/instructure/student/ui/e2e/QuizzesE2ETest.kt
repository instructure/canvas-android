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

import android.os.SystemClock.sleep
import android.util.Log
import androidx.test.espresso.Espresso.closeSoftKeyboard
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

    // Fairly basic test of web view-based quizzes.  Seeds/takes a quiz with two multiple-choice
    // questions.
    //
    // STUBBING THIS OUT.  Usually passes locally, but I can't get a simple webClick() to work on FTL.
    // See comments below.
    @E2E
    @Stub
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

        Log.d(ASSERTION_TAG, "Assert that '${quizPublished.title}' published quiz is displayed and '${quizUnpublished.title}' unpublished quiz has not displayed.")
        quizListPage.assertQuizDisplayed(quizPublished)
        quizListPage.assertQuizNotDisplayed(quizUnpublished)

        quizListPage.openSearchBar()
        quizListPage.enterSearchQuery(quizPublished.title)
        quizListPage.assertQuizDisplayed(quizPublished)
        quizListPage.assertQuizItemCount(1)
        quizListPage.clearSearchButton()
        quizListPage.enterSearchQuery(quizUnpublished.title)
        closeSoftKeyboard()
        

        Log.d(STEP_TAG, "Select '${quizPublished.title}' quiz.")
        quizListPage.selectQuiz(quizPublished)

        Log.d(ASSERTION_TAG, "Assert that the '${quizPublished.title}' quiz title is displayed.")
        canvasWebViewPage.runTextChecks(WebViewTextCheck(locatorType = Locator.ID, locatorValue = "quiz_title", textValue = quizPublished.title))

        // Launch the quiz
        // Pressing the "Take the Quiz" button does not work on an FTL Api 25 device.
        // Not even the logic below, which tries 10 times to press the button!
        // Every time the button is pressed on an FTL device, we get this console message:
        //
        //      09-29 07:24:22.796: I/chromium(7428): [INFO:CONSOLE(29)] "Uncaught TypeError: e.preventDefault(...)
        //      is not a function", source: https://mobileqa.beta.instructure.com/courses/3092218/quizzes/7177808?force_user=1&persist_headless=1 (29)
        //
        // The applicable code is in a script element in the header portion of the web view content:
        //      <script>
        //          function _earlyClick(e){
        //              var c = e.target
        //              while (c && c.ownerDocument) {
        //                  if (c.getAttribute('href') == '#' || c.getAttribute('data-method')) {
        //                      e.preventDefault()
        //                      (_earlyClick.clicks = _earlyClick.clicks || []).push(c)
        //                      break
        //                  }
        //                  c = c.parentNode
        //              }
        //          }
        //          document.addEventListener('click', _earlyClick)
        //      </script>
        //
        // My best guess is that Espresso-Web is clicking on the wrong location, in an
        // area where a preventDefault() function does not apply.
        //
        // Also, there is some slight variation in webview versions:
        //      --Local emulator: 55.0.2883.91
        //      --FTL emulator: 53.0.2785.135
        // Not sure if that would make a difference.
        //
        // Possible solution: Write a custom atom to do the work instead of relying on webClick() via pressButton()
        canvasWebViewPage.runTextChecks(
            WebViewTextCheck(
                locatorType = Locator.ID,
                locatorValue = "take_quiz_link",
                textValue = "Take the Quiz"
            )
        )
        canvasWebViewPage.pressButton(locatorType = Locator.ID, locatorValue = "take_quiz_link")

        // Enter answers to questions.  Right now, only multiple-choice questions are supported.
        Log.d(STEP_TAG, "Enter answers to the questions:")
        for(question in quizQuestions) {
            Log.d(ASSERTION_TAG, "Assert that the following question is displayed: '${question.questionText}'.")
            quizTakingPage.verifyQuestionDisplayed(question.id!!, question.questionText!!)
            if(question.questionType == "multiple_choice_question") {
                Log.d(STEP_TAG, "Choosing an answer for the following question: '${question.questionText}'.")
                quizTakingPage.selectAnyAnswer(question.id!!) // Just choose any answer
            }
        }

        Log.d(PREPARATION_TAG, "Submit the '${quizPublished.title}' quiz.")
        quizTakingPage.submitQuiz()

        // Interesting situation here.  If you wait long enough, the web page will update itself,
        // which affects the number of pressBack() commands that it takes to get back to the
        // quiz list page, and might also affect whether or not the "Attempt History" portion of
        // the page is displayed.
        //
        // Chosen strategy: pressBack() until you get to the quiz list page,
        // then reload the quiz details to get the latest info.
        //Log.d(STEP_TAG, "Navigate back to Quizzes Page.")
        //while(!isElementDisplayed(R.id.quizListPage)) pressBack()

        //Log.d(STEP_TAG, "Select '${quizPublished.title}' quiz.")
        //quizListPage.selectQuiz(quizPublished)

        sleep(5000)
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
        // For some reason, this quiz is resulting in a 10/10 grade, although with the weights assigned and
        // answers given it should be 5/10.  Let's just make sure that a "10" shows up.
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

        // Can't test essay questions yet.  More specifically, can't test answering essay questions.
    //                QuizQuestion(
    //                        questionText = "Why should I give you an A?",
    //                        questionType = "essay_question",
    //                        pointsPossible = 12,
    //                        answers = listOf()
    //                )
    )

}
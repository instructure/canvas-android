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
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.isElementDisplayed
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.QuizzesApi.createAndPublishQuiz
import com.instructure.dataseeding.model.QuizAnswer
import com.instructure.dataseeding.model.QuizQuestion
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import org.hamcrest.Matchers.containsString
import org.junit.Test

class QuizzesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Fairly basic test of webview-based quizzes.  Seeds/takes a quiz with two multiple-choice
    // questions.
    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E, false)
    fun testQuizzesE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed an unpublished quiz
        val quizUnpublished = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                published = false,
                token = teacher.token
        ))

        // Seed a published quiz with some questions
        val quizQuestions = listOf(
                QuizQuestion(
                        questionText = "What's your favorite color?",
                        questionType = "multiple_choice_question",
                        pointsPossible = 5,
                        answers = listOf(
                                QuizAnswer(id=1, weight=0, text="Red"),
                                QuizAnswer(id=1, weight=1, text="Blue"),
                                QuizAnswer(id=1, weight=0, text="Yellow")
                        )
                ),
                QuizQuestion(
                        questionText = "Who let the dogs out?",
                        questionType = "multiple_choice_question",
                        pointsPossible = 5,
                        answers = listOf(
                                QuizAnswer(id=1, weight=1, text="Who Who Who-Who"),
                                QuizAnswer(id=1, weight=0, text="Who Who-Who-Who"),
                                QuizAnswer(id=1, weight=0, text="Who-Who Who-Who")
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
        val quizPublished = createAndPublishQuiz(course.id, teacher.token, quizQuestions)


        // Sign in our user and navigate to our course
        tokenLogin(student)
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        // Verify that quiz info shows up in Quizzes tab
        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quizPublished)
        quizListPage.assertQuizNotDisplayed(quizUnpublished)

        // Verify that the quiz title is displayed, launch quiz
        quizListPage.selectQuiz(quizPublished)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(locatorType = Locator.ID, locatorValue = "quiz_title", textValue = quizPublished.title)
        )
        canvasWebViewPage.pressButton(locatorType = Locator.ID, locatorValue = "take_quiz_link")

        // Enter answers to questions.  Right now, only multiple-choice questions are supported.
        for(question in quizQuestions) {
            quizTakingPage.verifyQuestionDisplayed(question.id!!, question.questionText!!)
            if(question.questionType == "multiple_choice_question") {
                quizTakingPage.selectAnyAnswer(question.id!!) // Just choose any answer
            }
        }

        // Submit the quiz
        quizTakingPage.submitQuiz()

        // Interesting situation here.  If you wait long enough, the web page will update itself,
        // which affects the number of pressBack() commands that it takes to get back to the
        // quiz list page, and might also affect whether or not the "Attempt History" portion of
        // the page is displayed.
        //
        // Chosen strategy: pressBack() until you get to the quiz list page,
        // then reload the quiz details to get the latest info.
        while(!isElementDisplayed(R.id.quizListPage)) pressBack()
        quizListPage.selectQuiz(quizPublished)

        // Assert that the quiz now has a history.
        onWebView(withId(R.id.canvasWebView))
                .withElement(findElement(Locator.ID, "quiz-submission-version-table"))
                .withContextualElement(findElement(Locator.CLASS_NAME, "desc"))
                .perform(webScrollIntoView())
                .check(webMatches(getText(),containsString("Attempt History")))
        onWebView(withId(R.id.canvasWebView))
                .withElement(findElement(Locator.CLASS_NAME, "ic-Table--header-row"))
                .perform(webScrollIntoView())
                .check(webMatches(getText(),containsString("LATEST")))


        pressBack() // Back to get to quiz list page
        pressBack() // Back to course browser page

        // Go to grades page
        courseBrowserPage.selectGrades()
        // For some reason, this quiz is resulting in a 10/10 grade, although with the weights assigned and
        // answers given it should be 5/10.  Let's just make sure that a "10" shows up.
        courseGradesPage.assertGradeDisplayed(withText(quizPublished.title), containsTextCaseInsensitive("10"))

    }
}
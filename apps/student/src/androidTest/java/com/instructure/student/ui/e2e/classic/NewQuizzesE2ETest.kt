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
import com.instructure.dataseeding.api.NewQuizzesApi
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

        Log.d(PREPARATION_TAG, "Seed a NEW quiz for '${course.name}' course.")
        val newQuiz1 = NewQuizzesApi.createNewQuiz(courseId = course.id, token = teacher.token, published = true)

        Log.d(PREPARATION_TAG, "Seed another NEW quiz for '${course.name}' course.")
        val newQuiz2 = NewQuizzesApi.createNewQuiz(courseId = course.id, token = teacher.token,)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to Quizzes Page.")
        courseBrowserPage.selectQuizzes()

        Thread.sleep(2000)

        Log.d(ASSERTION_TAG, "Assert that both NEW quizzes ('${newQuiz1.title}' and '${newQuiz2.title}') are displayed.")
        quizListPage.assertNewQuizDisplayed(newQuiz1)
        quizListPage.assertNewQuizDisplayed(newQuiz2)

        Log.d(STEP_TAG, "Select '${newQuiz1.title}' quiz.")
        quizListPage.selectNewQuiz(newQuiz1)

        Thread.sleep(2000)

        Log.d(ASSERTION_TAG, "Assert that the NEW quiz is displayed in a webview (external LTI tool).")
        canvasWebViewPage.waitForWebView()

        Log.d(STEP_TAG, "Navigate back to Quiz List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the search bar and search for '${newQuiz2.title}' quiz.")
        quizListPage.searchable.clickOnSearchButton()
        quizListPage.searchable.typeToSearchBar(newQuiz2.title)

        Thread.sleep(1000)

        Log.d(ASSERTION_TAG, "Assert that only '${newQuiz2.title}' quiz is displayed in search results.")
        quizListPage.assertNewQuizDisplayed(newQuiz2)
        quizListPage.assertNewQuizNotDisplayed(newQuiz1)

        Log.d(STEP_TAG, "Clear the search bar.")
        quizListPage.searchable.clickOnClearSearchButton()

        Thread.sleep(1000)

        Log.d(ASSERTION_TAG, "Assert that both quizzes are displayed again after clearing search.")
        quizListPage.assertNewQuizDisplayed(newQuiz1)
        quizListPage.assertNewQuizDisplayed(newQuiz2)

        Log.d(STEP_TAG, "Search for a non-existing quiz.")
        quizListPage.searchable.typeToSearchBar("Non-existing Quiz")

        Thread.sleep(1000)

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed.")
        quizListPage.assertEmptyStateDisplayed()

        Log.d(STEP_TAG, "Clear the search bar.")
        quizListPage.searchable.clickOnClearSearchButton()

        Thread.sleep(1000)

        Log.d(ASSERTION_TAG, "Assert that both quizzes are displayed again.")
        quizListPage.assertNewQuizDisplayed(newQuiz1)
        quizListPage.assertNewQuizDisplayed(newQuiz2)

        Log.d(STEP_TAG, "Select '${newQuiz2.title}' quiz.")
        quizListPage.selectNewQuiz(newQuiz2)

        Thread.sleep(2000)

        Log.d(ASSERTION_TAG, "Assert that the NEW quiz is displayed in a webview (external LTI tool).")
        canvasWebViewPage.waitForWebView()
    }
}
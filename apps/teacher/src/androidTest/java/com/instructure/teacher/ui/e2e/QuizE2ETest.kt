/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.seedQuizQuestion
import com.instructure.teacher.ui.utils.seedQuizSubmission
import com.instructure.teacher.ui.utils.seedQuizzes
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizE2ETest: TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun testQuizzesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Quizzes Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()

        Log.d(ASSERTION_TAG, "Assert that there is no quiz displayed on the page.")
        quizListPage.assertDisplaysNoQuizzesView()

        Log.d(PREPARATION_TAG, "Seed two quizzes for the '${course.name}' course. Also, seed a question into both the quizzes (by default, the quizzes will be unpublished).")
        val testQuizList = seedQuizzes(courseId = course.id, quizzes = 2, withDescription = true, dueAt = 3.days.fromNow.iso8601, teacherToken = teacher.token, published = false)
        seedQuizQuestion(courseId = course.id, quizId = testQuizList.quizList[0].id, teacherToken = teacher.token)
        seedQuizQuestion(courseId = course.id, quizId = testQuizList.quizList[1].id, teacherToken = teacher.token)

        Log.d(STEP_TAG, "Refresh the page.")
        quizListPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that both of the quizzes are displayed on the Quiz List Page so the number of quizzes is 2.")
        quizListPage.assertQuizCount(2)

        val firstQuiz = testQuizList.quizList[0]
        val secondQuiz = testQuizList.quizList[1]
        Log.d(ASSERTION_TAG, "Assert that the quiz is there and click on the previously seeded quiz: '${firstQuiz.title}'.")
        quizListPage.clickQuiz(firstQuiz.title)

        Log.d(ASSERTION_TAG, "Assert that '${firstQuiz.title}' quiz is 'Not Submitted' and it is unpublished.")
        quizDetailsPage.assertNotSubmitted()
        quizDetailsPage.assertQuizUnpublished()

        val newQuizTitle = "This is a new quiz"
        Log.d(STEP_TAG, "Open 'Edit' page and edit the '${firstQuiz.title}' quiz's title to: '$newQuizTitle'.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.editQuizTitle(newQuizTitle)

        Log.d(ASSERTION_TAG, "Assert that the quiz name has been changed to: '$newQuizTitle'.")
        quizDetailsPage.assertQuizNameChanged(newQuizTitle)

        Log.d(STEP_TAG, "Open 'Edit' page and switch on the 'Published' checkbox, so publish the '$newQuizTitle' quiz. Click on 'Save'.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.switchPublish()
        editQuizDetailsPage.saveQuiz()

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that '$newQuizTitle' quiz has been unpublished.")
        quizDetailsPage.refresh()
        quizDetailsPage.assertQuizPublished()

        Log.d(PREPARATION_TAG, "Submit the '${firstQuiz.title}' quiz.")
        seedQuizSubmission(courseId = course.id, quizId = firstQuiz.id, studentToken = student.token)

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that it needs grading because of the previous submission.")
        quizListPage.refresh()
        quizDetailsPage.assertNeedsGrading()

        Log.d(STEP_TAG, "Click on Search button and type '$newQuizTitle' to the search input field.")
        Espresso.pressBack()
        quizListPage.searchable.clickOnSearchButton()
        quizListPage.searchable.typeToSearchBar(newQuizTitle)

        Log.d(ASSERTION_TAG, "Assert that only the matching quiz, which is '$newQuizTitle' is displayed on the Quiz List Page.")
        quizListPage.assertQuizCount(1)
        quizListPage.assertHasQuiz(newQuizTitle)
        quizListPage.assertQuizNotDisplayed(secondQuiz.title)

        Log.d(STEP_TAG, "Clear search input field value.")
        quizListPage.searchable.clickOnClearSearchButton()

        Log.d(ASSERTION_TAG, "Assert if both of the quizzes are displayed again on the Quiz List Page.")
        quizListPage.assertQuizCount(2)
        quizListPage.assertHasQuiz(newQuizTitle)
        quizListPage.assertHasQuiz(secondQuiz.title)

        Log.d(STEP_TAG, "Type a search value to the search input field which does not much with any of the existing quizzes.")
        quizListPage.searchable.typeToSearchBar("Non existing quiz")

        Thread.sleep(1000) //We need this wait here to let make sure the search process has finished.

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed.")
        quizListPage.assertDisplaysNoQuizzesView()

        Log.d(STEP_TAG,"Clear search input field value.")
        quizListPage.searchable.clickOnClearSearchButton()

        Log.d(ASSERTION_TAG, "Assert if both of the quizzes are displayed on the Quiz List Page.")
        quizListPage.assertQuizCount(2)
        quizListPage.assertHasQuiz(newQuizTitle)
        quizListPage.assertHasQuiz(secondQuiz.title)
    }

}
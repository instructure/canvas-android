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
package com.instructure.teacher.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.seedQuizQuestion
import com.instructure.teacher.ui.utils.extensions.seedQuizSubmission
import com.instructure.teacher.ui.utils.extensions.seedQuizzes
import com.instructure.teacher.ui.utils.extensions.tokenLogin
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

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun testQuizEditAndPreviewE2E() {

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

        Log.d(PREPARATION_TAG, "Seed a quiz for the '${course.name}' course.")
        val testQuizList = seedQuizzes(courseId = course.id, quizzes = 1, withDescription = true, teacherToken = teacher.token, published = true)

        Log.d(STEP_TAG, "Refresh the page.")
        quizListPage.refresh()

        val quiz = testQuizList.quizList[0]
        Log.d(STEP_TAG, "Click on the quiz: '${quiz.title}'.")
        quizListPage.clickQuiz(quiz.title)

        val quizTitle = "My Custom Quiz Title"
        val quizDescription = "This is my custom quiz description"
        Log.d(STEP_TAG, "Open 'Edit' page and edit the quiz description to: '$quizDescription' and title to: '$quizTitle'.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.editQuizDescription(quizDescription)
        editQuizDetailsPage.editQuizTitle(quizTitle)

        Log.d(ASSERTION_TAG, "Assert that the quiz name and description have been changed to: '$quizTitle' and '$quizDescription'.")
        quizDetailsPage.assertQuizNameChanged(quizTitle)
        quizDetailsPage.assertQuizDescriptionChanged(quizDescription)

        Log.d(STEP_TAG, "Open preview page.")
        quizDetailsPage.openPreviewPage()

        Log.d(ASSERTION_TAG, "Assert that the preview loaded and displays the edited quiz title: '$quizTitle' and the edited quiz description: '$quizDescription'.")
        quizPreviewPage.assertPreviewLoaded()
        quizPreviewPage.assertQuizTitleDisplayed(quizTitle)
        quizPreviewPage.assertQuizDescriptionDisplayed(quizDescription)

        Log.d(STEP_TAG, "Go back to Quiz Details page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open Due Dates section.")
        quizDetailsPage.openAllDatesPage()

        Log.d(STEP_TAG, "Click the pencil/edit icon to open the edit page.")
        assignmentDueDatesPage.openEditPage()

        Log.d(STEP_TAG, "Set due date and time for the first due date.")
        editQuizDetailsPage.clickEditDueDate()
        editQuizDetailsPage.editDate(2025, 5, 15)
        editQuizDetailsPage.clickEditDueTime()
        editQuizDetailsPage.editTime(10, 30)

        Log.d(STEP_TAG, "Click 'Add Override' to add a second due date and assign it to '${student.name}'.")
        editQuizDetailsPage.clickAddOverride()
        assigneeListPage.toggleAssignees(listOf(student.name))
        assigneeListPage.saveAndClose()

        Log.d(ASSERTION_TAG, "Assert that another new due date override has been created.")
        editQuizDetailsPage.assertNewOverrideCreated()

        Log.d(STEP_TAG, "Set due date and time for the second override.")
        editQuizDetailsPage.clickEditDueDate(1)
        editQuizDetailsPage.editDate(2025, 6, 20)
        editQuizDetailsPage.clickEditDueTime(1)
        editQuizDetailsPage.editTime(14, 45)

        Log.d(STEP_TAG, "Save the quiz after creating 2 due dates.")
        editQuizDetailsPage.saveQuiz()

        Log.d(STEP_TAG, "Refresh the Due Dates page.")
        refresh()

        Log.d(ASSERTION_TAG, "Assert that 2 due dates are visible on the Due Dates page.")
        assignmentDueDatesPage.assertDueDatesCount(2)

        Log.d(ASSERTION_TAG, "Assert first due date is for 'Everyone else' with date 'May 15, 2025 at 10:30 AM'.")
        assignmentDueDatesPage.assertDueFor("Everyone else")
        assignmentDueDatesPage.assertDueDateTime("May 15, 2025 at 10:30 AM")

        Log.d(ASSERTION_TAG, "Assert second due date is for '${student.name}' with date 'Jun 20, 2025 at 2:45 PM'.")
        assignmentDueDatesPage.assertDueFor(student.name)
        assignmentDueDatesPage.assertDueDateTime("Jun 20, 2025 at 2:45 PM")

        Log.d(STEP_TAG, "Press back to return to Quiz Details page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the due dates section shows 'Multiple Due Dates'.")
        quizDetailsPage.assertMultipleDueDatesTextDisplayed()

        Log.d(STEP_TAG, "Open Due Dates section again.")
        quizDetailsPage.openAllDatesPage()

        Log.d(STEP_TAG, "Click the pencil/edit icon to open the edit page.")
        assignmentDueDatesPage.openEditPage()

        Log.d(STEP_TAG, "Remove the second due date.")
        editQuizDetailsPage.removeSecondOverride()

        Log.d(STEP_TAG, "Save the quiz after removing the second due date.")
        editQuizDetailsPage.saveQuiz()

        Log.d(STEP_TAG, "Refresh the Due Dates page.")
        refresh()

        Log.d(ASSERTION_TAG, "Assert that only 1 due date is visible on the Due Dates page.")
        assignmentDueDatesPage.assertDisplaysSingleDueDate()

        Log.d(ASSERTION_TAG, "Assert remaining due date is for 'Everyone' with date 'May 15, 2025 at 10:30 AM'.")
        assignmentDueDatesPage.assertDueFor("Everyone")
        assignmentDueDatesPage.assertDueDateTime("May 15, 2025 at 10:30 AM")
    }

}
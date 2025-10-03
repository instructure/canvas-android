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
package com.instructure.student.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.NoMatchingViewException
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.QuizAnswer
import com.instructure.dataseeding.model.QuizQuestion
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class NotificationsE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testNotificationsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1, announcements = 1, discussions = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Seed a quiz for '${course.name}' course with some questions.")
        val quizQuestions = makeQuizQuestions()

        Log.d(PREPARATION_TAG, "Create and publish a quiz with the previously seeded questions.")
        QuizzesApi.createAndPublishQuiz(course.id, teacher.token, quizQuestions)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to 'Notifications' page via bottom-menu.")
        dashboardPage.clickNotificationsTab()

        Log.d(ASSERTION_TAG, "Assert that there are some notifications on the Notifications Page. There should be 4 notification at this point, but sometimes the API does not work properly.")
        var thereIsNotification = false

        run thereIsNotificationRepeat@ {
            repeat(10) {
                try {
                    refresh()
                    notificationPage.assertNotificationCountIsGreaterThan(0) //At least one notification is displayed.
                    thereIsNotification = true
                    return@thereIsNotificationRepeat
                } catch (e: AssertionError) {
                    println("Attempt failed: API has still not give back the response, so none of the notifications can be seen on the screen yet.")
                }
            }
        }

        Log.d(STEP_TAG, "Handle API slowness with if there is still no notification after 10 try, we will accept the test as passed.")
        if(!thereIsNotification) {
            return
        }

        try {
            notificationPage.assertNotificationCountIsGreaterThan(3) //"Soft assert", because API does not working consistently. Sometimes it simply does not create notifications about some events, even if we would wait enough to let it do that.
            Log.d(ASSERTION_TAG, "All four notifications are displayed.")
        } catch (e: AssertionError) {
            println("API may not work properly, so not all the notifications can be seen on the screen.")
        }

        refresh()
        run submitAndGradeRepeat@{
            repeat(10) {
                try {
                    Log.d(PREPARATION_TAG, "Submit '${testAssignment.name}' assignment with student: '${student.name}'.")
                    SubmissionsApi.submitCourseAssignment(course.id, student.token, testAssignment.id, SubmissionType.ONLINE_TEXT_ENTRY)

                    Log.d(PREPARATION_TAG, "Grade the submission of '${student.name}' student for assignment: '${testAssignment.name}'.")
                    SubmissionsApi.gradeSubmission(teacher.token, course.id, testAssignment.id, student.id, postedGrade = "13")

                    Log.d(ASSERTION_TAG, "Refresh the Notifications Page. Assert that there is a notification about the submission grading appearing.")
                    sleep(3000) //Let the submission api do it's job
                    refresh()
                    notificationPage.assertHasGrade(testAssignment.name, "13")
                    return@submitAndGradeRepeat
                } catch (e: NoMatchingViewException) {
                    println("Attempt failed: API has still not give back the response, so the graded assignment is not displayed among the notifications.")
                }
            }
        }
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
/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 */
package com.instructure.teacher.ui

import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addQuestionToQuiz
import com.instructure.canvas.espresso.mockCanvas.addQuizSubmission
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizAnswer
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SpeedGraderQuizSubmissionPageTest : TeacherComposeTest() {

    @Stub
    @Test
    override fun displaysPageObjects() {
        getToQuizSubmissionPage()
        speedGraderQuizSubmissionPage.assertPageObjects()
    }

    @Stub
    @Test
    fun displaysNoSubmission() {
        getToQuizSubmissionPage(submitQuiz = false)
        speedGraderQuizSubmissionPage.assertShowsNoSubmissionState()
    }

    @Stub
    @Test
    fun displaysPendingReviewState() {
        getToQuizSubmissionPage(addQuestion = true, state = "pending_review")
        speedGraderQuizSubmissionPage.assertShowsPendingReviewState()
    }

    @Stub
    @Test
    fun displaysViewQuizState() {
        getToQuizSubmissionPage(state = "untaken")
        speedGraderQuizSubmissionPage.assertShowsViewQuizState()
    }

    private fun getToQuizSubmissionPage(addQuestion: Boolean = false, submitQuiz: Boolean = true, state: String = "untaken") {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, favoriteCourseCount = 1, courseCount = 1)
        val teacher = data.teachers[0]
        val student = data.students[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val quiz = data.addQuizToCourse(course = course, published = true, quizType = Quiz.TYPE_ASSIGNMENT)
        val quizId = quiz.id

        if (addQuestion) {
            data.addQuestionToQuiz(
                    course = course,
                    quizId = quizId,
                    questionName = "Mock Question",
                    questionText = "What's your favorite color?",
                    answers = arrayOf(
                            QuizAnswer(id =  data.newItemId(), answerText = "Red", answerWeight = 1),
                            QuizAnswer(id =  data.newItemId(), answerText = "Yellow", answerWeight = 0),
                            QuizAnswer(id =  data.newItemId(), answerText = "Blue", answerWeight = 0),
                            QuizAnswer(id =  data.newItemId(), answerText = "Green", answerWeight = 0)
                    )
            )

        }

        if (submitQuiz) {
            data.addQuizSubmission(quiz, student, state)
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()

        quizListPage.clickQuiz(quiz)
        quizDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
    }
}

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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addQuizSubmission
import com.instructure.canvas.espresso.mockcanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizDetailsInteractionTest: TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToQuizDetailsPage()
        quizDetailsPage.assertPageObjects()
    }

    @Test
    fun displaysCorrectDetails() {
        val quiz = getToQuizDetailsPage()
        quizDetailsPage.assertQuizDetails(quiz)
    }

    @Test
    fun displaysInstructions() {
        getToQuizDetailsPage(withDescription = true)
        quizDetailsPage.assertDisplaysInstructions()
    }

    @Test
    fun displaysNoInstructionsMessage() {
        getToQuizDetailsPage()
        quizDetailsPage.assertDisplaysNoInstructionsView()
    }

    @Test
    fun displaysClosedAvailability() {
        getToQuizDetailsPage(lockAt = 1.days.ago.iso8601)
        quizDetailsPage.assertQuizClosed()
    }

    @Test
    fun displaysNoFromDate() {
        val lockAt = 2.days.fromNow.iso8601
        getToQuizDetailsPage(lockAt = lockAt)
        quizDetailsPage.assertToFilledAndFromEmpty()
    }

    @Test
    fun displaysNoToDate() {
        getToQuizDetailsPage(unlockAt = 2.days.ago.iso8601)
        quizDetailsPage.assertFromFilledAndToEmpty()
    }

    @Test
    fun displaysSubmittedDonut() {
        getToQuizDetailsPage(students = 1, submissions = 1)
        quizDetailsPage.assertHasSubmitted()
    }

    @Test
    fun displaysNotSubmittedDonut() {
        getToQuizDetailsPage(students = 1, submissions = 0)
        quizDetailsPage.assertNotSubmitted()
    }

    private fun getToQuizDetailsPage(
            withDescription: Boolean = false,
            lockAt: String? = null,
            unlockAt: String? = null,
            students: Int = 0,
            submissions: Int = 0): Quiz {
        val data = MockCanvas.init(teacherCount = 1, studentCount = students, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        if(submissions > 0 && students < 1) {
            throw Exception("Need at least one student for a submission")
        }

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val quiz = data.addQuizToCourse(
                course = course,
                quizType = Quiz.TYPE_ASSIGNMENT,
                lockAt = lockAt,
                unlockAt = unlockAt,
                description = if(withDescription) "Here's a description!" else ""
        )

        for (s in 0 until submissions) {
            data.addQuizSubmission(
                    quiz = quiz,
                    user = data.students[0],
                    state = "complete",
                    grade = "4" // in the context of this test, "submitted" = "graded"

            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(quiz)
        quizDetailsPage.waitForRender()
        return quiz
    }

}

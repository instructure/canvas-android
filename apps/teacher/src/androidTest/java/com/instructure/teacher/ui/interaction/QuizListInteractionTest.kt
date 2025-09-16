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
 *
 */
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizListInteractionTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToQuizzesPage()
        quizListPage.assertPageObjects()
    }

    @Test
    fun displaysNoQuizzesView() {
        getToQuizzesPage(quizCount = 0)
        quizListPage.assertDisplaysNoQuizzesView()
    }

    @Test
    fun displaysQuiz() {
        val quizzes = getToQuizzesPage()
        quizListPage.assertHasQuiz(quizzes[0])
    }

    @Test
    fun searchesQuizzes() {
        val quizzes = getToQuizzesPage(quizCount = 3)
        val searchQuiz = quizzes[2]
        quizListPage.assertQuizCount(quizzes.size)
        quizListPage.searchable.clickOnSearchButton()
        quizListPage.searchable.typeToSearchBar(searchQuiz.title!!.take(searchQuiz.title!!.length / 2))
        quizListPage.assertQuizCount(1)
        quizListPage.assertHasQuiz(searchQuiz)
    }

    private fun getToQuizzesPage(quizCount: Int = 1): List<Quiz> {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val quizzes = mutableListOf<Quiz>()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        repeat(quizCount) {
            val quiz = data.addQuizToCourse(
                    course = course,
                    quizType = Quiz.TYPE_ASSIGNMENT,
                    lockAt = 1.days.ago.iso8601,
                    unlockAt = 2.days.ago.iso8601
            )

            quizzes += quiz
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()

        return quizzes
    }
}

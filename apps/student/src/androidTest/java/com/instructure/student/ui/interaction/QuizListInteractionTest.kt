/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Quiz
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizListInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.QUIZZES, TestCategory.INTERACTION)
    fun displaysNoQuizzesView() {
        getToQuizListPage(0)
        quizListPage.assertNoQuizDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.QUIZZES, TestCategory.INTERACTION)
    fun displaysQuiz() {
        val quiz = getToQuizListPage(1)[0]
        quizListPage.assertQuizDisplayed(quiz)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.QUIZZES, TestCategory.INTERACTION)
    fun displaysQuizzes() {
        val quizzes = getToQuizListPage(5)
        quizListPage.assertQuizItemCount(quizzes.size)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.QUIZZES, TestCategory.INTERACTION)
    fun displaysQuizWithPointsIfNotRestrictQuantitativeData() {
        val quiz = getToQuizListPage(1)[0]
        quizListPage.assertPointsDisplayed("${quiz.pointsPossible} points")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.QUIZZES, TestCategory.INTERACTION)
    fun displaysQuizWithoutPointsIfRestrictQuantitativeData() {
        getToQuizListPage(1, true)
        quizListPage.assertPointsNotDisplayed()
    }

    private fun getToQuizListPage(itemCount: Int = 1, restrictQuantitativeData: Boolean = false): List<Quiz> {
        val data = MockCanvas.init(
            courseCount = 1,
            favoriteCourseCount = 1,
            studentCount = 1,
            teacherCount = 1
        )

        val course = data.courses.values.first()
        data.courseSettings[course.id] = CourseSettings(restrictQuantitativeData = restrictQuantitativeData)
        val student = data.students.first()
        val quizList = mutableListOf<Quiz>()
        data.courseQuizzes[course.id] = mutableListOf()
        repeat(itemCount) {
            val quiz = data.addQuizToCourse(course, pointsPossible = 10)
            quizList.add(quiz)
        }
        val token = data.tokenFor(student)!!

        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectQuizzes()

        return quizList
    }
}

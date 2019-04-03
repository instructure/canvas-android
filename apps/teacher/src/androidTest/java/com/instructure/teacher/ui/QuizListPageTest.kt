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
package com.instructure.teacher.ui

import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ditto.Ditto
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.seedQuizzes
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class QuizListPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToQuizzesPage()
        quizListPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysNoQuizzesView() {
        getToQuizzesPage(quizCount = 0)
        quizListPage.assertDisplaysNoQuizzesView()
    }

    @Test
    @Ditto
    fun displaysQuiz() {
        val quizzes = getToQuizzesPage()
        quizListPage.assertHasQuiz(quizzes[0])
    }

    @Test
    @Ditto
    fun searchesQuizzes() {
        val quizzes = getToQuizzesPage(quizCount = 3)
        val searchQuiz = quizzes[2]
        quizListPage.assertQuizCount(quizzes.size + 1) // +1 to account for header
        quizListPage.openSearch()
        quizListPage.enterSearchQuery(searchQuiz.title.take(searchQuiz.title.length / 2))
        quizListPage.assertQuizCount(2) // header + single search result
        quizListPage.assertHasQuiz(searchQuiz)
    }

    private fun getToQuizzesPage(quizCount: Int = 1): List<QuizApiModel> {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val quizzes = mutableListOf<QuizApiModel>()

        if (quizCount > 0) {
            quizzes += seedQuizzes(
                courseId = course.id,
                quizzes = quizCount,
                withDescription = false,
                lockAt = 1.days.ago.iso8601,
                unlockAt = 2.days.ago.iso8601,
                teacherToken = teacher.token).quizList
        }

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()

        return quizzes
    }
}

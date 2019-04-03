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

import com.instructure.dataseeding.util.*
import com.instructure.teacher.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class SpeedGraderQuizSubmissionPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToQuizSubmissionPage()
        speedGraderQuizSubmissionPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysNoSubmission() {
        getToQuizSubmissionPage(submitQuiz = false)
        speedGraderQuizSubmissionPage.assertShowsNoSubmissionState()
    }

    @Test
    @Ditto
    fun displaysPendingReviewState() {
        getToQuizSubmissionPage(addQuestion = true)
        speedGraderQuizSubmissionPage.assertShowsPendingReviewState()
    }

    @Test
    @Ditto
    fun displaysViewQuizState() {
        getToQuizSubmissionPage()
        speedGraderQuizSubmissionPage.assertShowsViewQuizState()
    }

    private fun getToQuizSubmissionPage(addQuestion: Boolean = false, submitQuiz: Boolean = true) {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = 1)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]
        val courseId = course.id

        // note that the quiz is set to unpublished if we're adding questions
        val quiz = seedQuizzes(
                courseId = courseId,
                quizzes = 1,
                withDescription = false,
                lockAt = 1.week.fromNow.iso8601,
                unlockAt = 2.days.ago.iso8601,
                published = !addQuestion,
                teacherToken = teacher.token).quizList[0]
        val quizId = quiz.id

        if (addQuestion) {
            seedQuizQuestion(
                    courseId = courseId,
                    quizId = quizId,
                    teacherToken = teacher.token
            )

            publishQuiz(courseId = courseId,
                    quizId = quizId,
                    teacherToken = teacher.token)
        }

        if (submitQuiz) {
            seedQuizSubmission(
                    courseId = courseId,
                    quizId = quizId,
                    studentToken = student.token
            )
        }

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()

        quizListPage.clickQuiz(quiz)
        quizDetailsPage.openSubmissionsPage()
        quizSubmissionListPage.clickSubmission(student)
    }
}

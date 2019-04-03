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

import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class QuizSubmissionListPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysNoSubmissionsView() {
        goToQuizSubmissionListPage(
                students = 0,
                submissions = 0
        )
        quizSubmissionListPage.assertDisplaysNoSubmissionsView()
    }

    @Test
    @Ditto
    fun filterLateSubmissions() {
        goToQuizSubmissionListPage(
                dueAt = 7.days.ago.iso8601
        )
        quizSubmissionListPage.clickFilterButton()
        quizSubmissionListPage.clickFilterSubmissions()
        quizSubmissionListPage.filterSubmittedLate()
        quizSubmissionListPage.clickDialogPositive()
        quizSubmissionListPage.assertDisplaysClearFilter()
        quizSubmissionListPage.assertFilterLabelText(R.string.submitted_late)
        quizSubmissionListPage.assertHasSubmission()
    }

    @Test
    @Ditto
    fun filterPendingReviewSubmissions() {
        goToQuizSubmissionListPage(addQuestion = true)
        quizSubmissionListPage.clickFilterButton()
        quizSubmissionListPage.clickFilterSubmissions()
        quizSubmissionListPage.filterNotGraded()
        quizSubmissionListPage.clickDialogPositive()
        quizSubmissionListPage.assertDisplaysClearFilter()
        quizSubmissionListPage.assertFilterLabelText(R.string.havent_been_graded)
        quizSubmissionListPage.assertHasSubmission()
    }

    @Test
    @Ditto
    fun displaysQuizStatusComplete() {
        goToQuizSubmissionListPage(complete = true)
        quizSubmissionListPage.assertSubmissionStatusSubmitted()
    }

    @Test
    @Ditto
    fun displaysQuizStatusMissing() {
        goToQuizSubmissionListPage(
                students = 1,
                submissions = 0,
                dueAt = 1.days.ago.iso8601
        )
        quizSubmissionListPage.assertSubmissionStatusMissing()
    }

    @Test
    @Ditto
    fun messageStudentsWho() {
        val data = goToQuizSubmissionListPage()
        val student = data.studentsList[0]
        quizSubmissionListPage.clickAddMessage()
        addMessagePage.assertPageObjects()
        addMessagePage.assertHasStudentRecipient(student)
    }

    private fun goToQuizSubmissionListPage(
            students: Int = 1,
            submissions: Int = 1,
            dueAt: String = "",
            complete: Boolean = true,
            addQuestion: Boolean = false): SeedApi.SeededDataApiModel {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = students)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val quiz = seedQuizzes(
                courseId = course.id,
                quizzes = 1,
                dueAt = dueAt,
                published = !addQuestion,
                teacherToken = teacher.token).quizList[0]

        if (addQuestion) {
            seedQuizQuestion(
                    courseId = course.id,
                    quizId = quiz.id,
                    teacherToken = teacher.token
            )

            publishQuiz(
                    courseId = course.id,
                    quizId = quiz.id,
                    teacherToken = teacher.token
            )
        }

        for (s in 0 until submissions) {
            seedQuizSubmission(course.id, quiz.id, data.studentsList[s].token, complete)
        }

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(quiz)
        quizDetailsPage.openSubmissionsPage()

        return data
    }
}

//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.CreateQuizQuestionApiModel
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.dataseeding.model.QuizSubmissionApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuizzesTest {
    private val course = CoursesApi.createCourse()
    private val teacher = UserApi.createCanvasUser()
    private val student = UserApi.createCanvasUser()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
        EnrollmentsApi.enrollUserAsStudent(course.id, student.id)
    }

    @Test
    fun createQuiz() {
        val quiz = QuizzesApi.createQuiz(
                courseId = course.id,
                withDescription = true,
                lockAt = "",
                unlockAt = "",
                dueAt = "",
                published = false,
                token = teacher.token
        )
        assertThat(quiz, instanceOf(QuizApiModel::class.java))
        assertTrue(quiz.id >= 1)
        assertTrue(quiz.title.isNotEmpty())
        assertFalse(quiz.published)
    }

    @Test
    fun createQuizQuestion() {
        val quiz = QuizzesApi.createQuiz(
                courseId = course.id,
                withDescription = true,
                lockAt = "",
                unlockAt = "",
                dueAt = "",
                published = false,
                token = teacher.token
        )
        val question = QuizzesApi.createQuizQuestion(
                courseId = course.id,
                quizId = quiz.id,
                teacherToken = teacher.token
        )
        assertThat(question, instanceOf(CreateQuizQuestionApiModel::class.java))
        assertTrue(question.id >= 1)
    }

    @Test
    fun publishQuiz() {
        var quiz = QuizzesApi.createQuiz(
                courseId = course.id,
                withDescription = true,
                lockAt = "",
                unlockAt = "",
                dueAt = "",
                published = false,
                token = teacher.token
        )

        quiz = QuizzesApi.publishQuiz(
                courseId = course.id,
                quizId = quiz.id,
                teacherToken = teacher.token,
                published = true
        )
        assertThat(quiz, instanceOf(QuizApiModel::class.java))
        assertTrue(quiz.id >= 1)
        assertTrue(quiz.title.isNotEmpty())
        assertTrue(quiz.published)
    }

    @Test
    fun createQuizSubmission() {
        val quiz = QuizzesApi.createQuiz(
                courseId = course.id,
                withDescription = true,
                lockAt = "",
                unlockAt = "",
                dueAt = "",
                published = true,
                token = teacher.token
        )

        val submission = QuizzesApi.createQuizSubmission(
                courseId = course.id,
                quizId = quiz.id,
                studentToken = student.token
        ).quizSubmissions[0]

        assertThat(submission, instanceOf(QuizSubmissionApiModel::class.java))
        assertTrue(submission.id >= 1)
        assertEquals(1, submission.attempt)
        assertTrue(submission.validationToken.isNotEmpty())
    }

    @Test
    fun completeQuizSubmission() {
        val quiz = QuizzesApi.createQuiz(
                courseId = course.id,
                withDescription = true,
                lockAt = "",
                unlockAt = "",
                dueAt = "",
                published = true,
                token = teacher.token
        )
        var submission = QuizzesApi.createQuizSubmission(
                courseId = course.id,
                quizId = quiz.id,
                studentToken = student.token
        ).quizSubmissions[0]

        submission = QuizzesApi.completeQuizSubmission(
                courseId = course.id,
                quizId = quiz.id,
                submissionId = submission.id,
                attempt = submission.attempt,
                validationToken = submission.validationToken,
                studentToken = student.token
        ).quizSubmissions[0]

        assertThat(submission, instanceOf(QuizSubmissionApiModel::class.java))
        assertEquals(1, submission.attempt)
        assertTrue(submission.validationToken.isNotEmpty())
    }

    @Test
    fun seedQuizzes() {
        for (quizCount in 0..2) {
            val quizzes = QuizzesApi.seedQuizzes(
                    QuizzesApi.CreateQuizRequest(
                            courseId = course.id,
                            withDescription = false,
                            published = false,
                            token = teacher.token
                    ),
                    quizCount
            )
            assertEquals(quizCount, quizzes.quizList.size)
        }
    }

    @Test
    fun seedQuizSubmission() {
        val quiz = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                published = true,
                token = teacher.token
        ))
        val incompleteResponse = QuizzesApi.seedQuizSubmission(
                request = QuizzesApi.CreateQuizSubmissionRequest(
                        courseId = course.id,
                        quizId = quiz.id,
                        studentToken = student.token
                ),
                complete = false
        )
        assertThat(incompleteResponse, instanceOf(QuizSubmissionApiModel::class.java))
        assertTrue(incompleteResponse.id >= 1)
        assertEquals(1, incompleteResponse.attempt)
        val submission = QuizzesApi.completeQuizSubmission(
                courseId = course.id,
                quizId = quiz.id,
                submissionId = incompleteResponse.id,
                attempt = incompleteResponse.attempt,
                validationToken = incompleteResponse.validationToken,
                studentToken = student.token
        ).quizSubmissions[0]
        assertThat(submission, instanceOf(QuizSubmissionApiModel::class.java))
        assertEquals(1, submission.attempt)
        assertTrue(submission.validationToken.isNotEmpty())
    }
}

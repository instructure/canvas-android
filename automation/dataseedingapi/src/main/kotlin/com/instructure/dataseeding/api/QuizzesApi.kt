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


package com.instructure.dataseeding.api

import com.google.gson.annotations.SerializedName
import com.instructure.dataseeding.model.*
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

object QuizzesApi {
    interface QuizzesService {
        @POST("courses/{courseId}/quizzes")
        fun createQuiz(@Path("courseId") courseId: Long, @Body createQuiz: CreateQuiz): Call<QuizApiModel>

        @POST("courses/{courseId}/quizzes/{quizId}/submissions")
        fun createQuizSubmission(@Path("courseId") courseId: Long, @Path("quizId") quizId: Long): Call<QuizSubmissionsResponse>

        @POST("courses/{courseId}/quizzes/{quizId}/submissions/{submissionId}/complete")
        fun completeQuizSubmission(@Path("courseId") courseId: Long,
                                   @Path("quizId") quizId: Long,
                                   @Path("submissionId") submissionId: Long,
                                   @Body completeQuiz: CompleteQuizRequest): Call<QuizSubmissionsResponse>

        @POST("courses/{courseId}/quizzes/{quizId}/questions")
        fun createQuizQuestion(@Path("courseId") courseId: Long,
                               @Path("quizId") quizId: Long,
                               @Body createQuestion: CreateQuizQuestion): Call<CreateQuizQuestionApiModel>

        @PUT("courses/{courseId}/quizzes/{quizId}")
        fun publishQuiz(@Path("courseId") courseId: Long,
                        @Path("quizId") quizId: Long,
                        @Body publishQuiz: PublishQuizRequest): Call<QuizApiModel>

    }

    private fun quizzesService(token: String): QuizzesService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(QuizzesService::class.java)

    data class CreateQuizRequest (
            val courseId: Long,
            val withDescription: Boolean,
            val published: Boolean,
            val token: String,
            val lockAt: String = "",
            val unlockAt: String = "",
            val dueAt: String = ""
    )

    fun createQuiz(request: CreateQuizRequest) : QuizApiModel {
        return createQuiz(
                courseId = request.courseId,
                withDescription = request.withDescription,
                lockAt = request.lockAt,
                unlockAt = request.unlockAt,
                dueAt = request.dueAt,
                published = request.published,
                token = request.token
        )
    }

    fun createQuiz(
            courseId: Long,
            token: String,
            withDescription: Boolean = true,
            lockAt: String = "",
            unlockAt: String = "",
            dueAt: String = "",
            published: Boolean = true
            ): QuizApiModel {
        val quiz = CreateQuiz(Randomizer.randomQuiz(withDescription, lockAt, unlockAt, dueAt, published))

        return quizzesService(token)
                .createQuiz(courseId, quiz)
                .execute()
                .body()!!
    }

    data class CreateQuizSubmissionRequest(
            val courseId: Long,
            val quizId: Long,
            val studentToken: String
    )

    fun createQuizSubmission(request: CreateQuizSubmissionRequest): QuizSubmissionsResponse {
        return createQuizSubmission(
                courseId = request.courseId,
                quizId = request.quizId,
                studentToken = request.studentToken
        )
    }

    fun createQuizSubmission(courseId: Long, quizId: Long, studentToken: String): QuizSubmissionsResponse =
            quizzesService(studentToken)
                .createQuizSubmission(courseId, quizId)
                .execute()
                .body()!!

    fun completeQuizSubmission(
            courseId: Long,
            quizId: Long,
            submissionId: Long,
            attempt: Long,
            validationToken: String,
            studentToken: String): QuizSubmissionsResponse {
        val request = CompleteQuizRequest(attempt, validationToken)

        return quizzesService(studentToken)
                .completeQuizSubmission(courseId, quizId, submissionId, request)
                .execute()
                .body()!!
    }

    private val defaultQuizQuestion = QuizQuestion(
                    questionName = "question name",
                    questionText = "question text",
                    questionType = "essay_question",
                    pointsPossible = 1,
                    answers = listOf()
//                            answers = listOf(QuizAnswer(
//                                    text = "text",
//                                    comments = "comments",
//                                    blankId = "",
//                                    weight = 100,
//                                    id = 1))

    )

    fun createQuizQuestion(
            courseId: Long,
            quizId: Long,
            teacherToken: String,
            quizQuestion: QuizQuestion = defaultQuizQuestion): CreateQuizQuestionApiModel {
        // Question that requires grading
        val createQuestion = CreateQuizQuestion(quizQuestion)

        return quizzesService(teacherToken)
                .createQuizQuestion(courseId, quizId, createQuestion)
                .execute()
                .body()!!
    }

    // Note that publishing an already published quiz does nothing. Must unpublish then publish.
    fun publishQuiz(courseId: Long, quizId: Long, teacherToken: String, published: Boolean = true): QuizApiModel {
        val publishQuizRequest = PublishQuizRequest(
                quiz = UpdateQuiz(published = published)
        )

        return quizzesService(teacherToken)
                .publishQuiz(courseId, quizId, publishQuizRequest)
                .execute()
                .body()!!
    }

    //
    // Seeding
    //

    fun seedQuizzes(request: CreateQuizRequest, numQuizzes: Int) : QuizListApiModel {

        val result = QuizListApiModel(
                quizList = (0 until numQuizzes).map {
                    createQuiz(request)
                }
        )

        return result
    }

    fun seedQuizSubmission(request: CreateQuizSubmissionRequest, complete: Boolean) : QuizSubmissionApiModel {
        // "you are not allowed to participate in this quiz" = make sure the quiz isn't locked

        // Pare response down to single QuizSubmissionApiModel.  This method can only create one, so we might
        // as well not complicate things by returning a QuizSubmissionResponse/List.
        var submission = createQuizSubmission(request).quizSubmissions[0]
        if(complete)
        {
            submission = completeQuizSubmission(
                    courseId = request.courseId,
                    quizId = request.quizId,
                    submissionId = submission.id,
                    attempt = submission.attempt,
                    validationToken = submission.validationToken,
                    studentToken = request.studentToken
            ).quizSubmissions[0]
        }

        return submission
    }

    // Convenience method to create and publish a quiz with questions
    fun createAndPublishQuiz(courseId: Long, teacherToken: String, questions: List<QuizQuestion>) : QuizApiModel {
        val result = createQuiz(CreateQuizRequest(
                courseId = courseId,
                withDescription = true,
                published = false, // Will publish in just a bit, after we add questions
                token = teacherToken
        ))

        for(question in questions) {
            val result = createQuizQuestion(
                    courseId = courseId,
                    quizId = result.id,
                    teacherToken = teacherToken,
                    quizQuestion = question
            )
            question.id = result.id // back-fill the question id
        }

        publishQuiz(
                courseId = courseId,
                quizId = result.id,
                teacherToken = teacherToken,
                published = true
        )

        return result
    }}

data class CreateQuiz(
        val quiz: com.instructure.dataseeding.model.CreateQuiz
)

data class CompleteQuizRequest(
        val attempt: Long,
        @SerializedName("validation_token")
        val validationToken: String
)

data class QuizSubmissionsResponse(
        @SerializedName("quiz_submissions")
        val quizSubmissions: List<QuizSubmissionApiModel>
)

data class PublishQuizRequest(
        val quiz: UpdateQuiz
)

data class CreateQuizQuestionApiModel(
        val id: Long
)



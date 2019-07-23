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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.QuizPostBodyWrapper
import com.instructure.canvasapi2.utils.weave.awaitApi
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

object QuizAPI {

    internal interface QuizInterface {
        @GET("{contextType}/{contextId}/quizzes")
        fun getFirstPageQuizzesList(@Path("contextType") contextType: String, @Path("contextId") contextId: Long): Call<List<Quiz>>

        @GET
        fun getNextPageQuizzesList(@Url nextURL: String): Call<List<Quiz>>

        @GET("{contextType}/{contextId}/quizzes/{quizId}")
        fun getDetailedQuiz(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("quizId") quizId: Long): Call<Quiz>

        @GET
        fun getDetailedQuizByUrl(@Url quizUrl: String): Call<Quiz>

        @GET("quiz_submissions/{quizSubmissionId}/questions")
        fun getFirstPageSubmissionQuestions(@Path("quizSubmissionId") quizSubmissionId: Long): Call<QuizSubmissionQuestionResponse>

        @GET
        fun getNextPageSubmissionQuestions(@Url nextURL: String): Call<QuizSubmissionQuestionResponse>

        @GET("courses/{courseId}/quizzes")
        fun getFirstPageQuizzes(@Path("courseId") contextId: Long): Call<List<Quiz>>

        @GET
        fun getNextPageQuizzes(@Url nextUrl: String): Call<List<Quiz>>

        @GET("courses/{courseId}/quizzes/{quizId}")
        fun getQuiz(@Path("courseId") courseId: Long, @Path("quizId") quizId: Long): Call<Quiz>

        @PUT("courses/{courseId}/quizzes/{quizId}")
        fun editQuiz(
                @Path("courseId") courseId: Long,
                @Path("quizId") quizId: Long,
                @Body body: QuizPostBodyWrapper): Call<Quiz>

        //FIXME: MIGRATION can be removed and replaced with other getFirstPageQuizSubmissions()
        @GET("courses/{courseId}/quizzes/{quizId}/submissions")
        fun getFirstPageQuizSubmissions(@Path("courseId") courseId: Long?, @Path("quizId") quizId: Long?): Call<QuizSubmissionResponse>

        @GET
        fun getNextPageQuizSubmissions(@Url nextUrl: String): Call<QuizSubmissionResponse>

        @POST("{contextType}/{contextId}/quizzes/{quizId}/submissions/{submissionId}/complete")
        fun submitQuiz(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Path("quizId") quizId: Long,
                @Path("submissionId") submissionId: Long,
                @Query("attempt") attempt: Int,
                @Query("validation_token") token: String): Call<QuizSubmissionResponse>

        @GET("{contextType}/{contextId}/quizzes/{quizId}/submissions")
        fun getFirstPageQuizSubmissions(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("quizId") quizId: Long): Call<QuizSubmissionResponse>

        @POST("{contextType}/{contextId}/quizzes/{quizId}/submissions/{submissionId}/events")
        fun postQuizStartedEvent(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Path("quizId") quizId: Long,
                @Path("submissionId") submissionId: Long,
                @Query("quiz_submission_events[][event_type]") sessionStartedString: String,
                @Query("quiz_submission_events[][event_data][user_agent]") userAgentString: String): Call<ResponseBody>

        @POST("{contextType}/{contextId}/quizzes/{quizId}/submissions")
        fun startQuiz(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Path("quizId") quizId: Long): Call<QuizSubmissionResponse>

        @GET("{contextType}/{contextId}/quizzes/{quizId}/submissions/{submissionId}/time")
        fun getQuizSubmissionTime(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Path("quizId") quizId: Long,
                @Path("submissionId") submissionId: Long): Call<QuizSubmissionTime>

        @POST("{contextType}/{contextId}/quizzes/{quizId}/submissions/{submissionId}/complete")
        fun postQuizSubmit(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Path("quizId") quizId: Long,
                @Path("submissionId") submissionId: Long,
                @Query("attempt") attempt: Int,
                @Query("validation_token") token: String): Call<QuizSubmissionResponse>

        @POST("quiz_submissions/{quizSubmissionId}/questions")
        fun postQuizQuestionFileUpload(
                @Path("quizSubmissionId") quizSubmissionId: Long,
                @Query("attempt") attempt: Int,
                @Query("validation_token") token: String,
                @Query("quiz_questions[][id]") questionId: Long,
                @Query("quiz_questions[][answer]") answer: String): Call<QuizSubmissionQuestionResponse>

        @POST
        fun postQuizQuestionUrl(@Url url: String): Call<QuizSubmissionQuestionResponse>

        @POST("quiz_submissions/{quizSubmissionId}/questions")
        fun postQuizQuestionEssay(
                @Path("quizSubmissionId") quizSubmissionId: Long,
                @Query("attempt") attempt: Int,
                @Query("validation_token") token: String,
                @Query("quiz_questions[][id]") questionId: Long,
                @Query(value = "quiz_questions[][answer]", encoded = true) answer: String): Call<QuizSubmissionQuestionResponse>

        @POST("quiz_submissions/{quizSubmissionId}/questions")
        fun postQuizQuestionMultiChoice(
                @Path("quizSubmissionId") quizSubmissionId: Long,
                @Query("attempt") attempt: Int,
                @Query("validation_token") token: String,
                @Query("quiz_questions[][id]") questionId: Long,
                @Query("quiz_questions[][answer]") answer: Long): Call<QuizSubmissionQuestionResponse>

        @PUT("quiz_submissions/{quizSubmissionId}/questions/{questionId}/{flag}")
        fun putFlagQuizQuestion(
                @Path("quizSubmissionId") quizSubmissionId: Long,
                @Path("questionId") questionId: Long,
                @Path("flag") flag: String,
                @Query("attempt") attempt: Int,
                @Query("validation_token") token: String): Call<ResponseBody>
    }

    fun getQuiz(courseId: Long, quizId: Long, adapter: RestBuilder, callback: StatusCallback<Quiz>, params: RestParams) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getQuiz(courseId, quizId)).enqueue(callback)
    }

    fun getFirstPageSubmissionQuestions(quizSubmissionId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<QuizSubmissionQuestionResponse>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getFirstPageSubmissionQuestions(quizSubmissionId)).enqueue(callback)
    }

    fun getNextPageSubmissionQuestions(nextUrl: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<QuizSubmissionQuestionResponse>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getNextPageSubmissionQuestions(nextUrl)).enqueue(callback)
    }

    fun getFirstPageQuizzes(contextId: Long, forceNetwork: Boolean, adapter: RestBuilder, callback: StatusCallback<List<Quiz>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(QuizInterface::class.java, params).getFirstPageQuizzes(contextId)).enqueue(callback)
    }

    fun getNextPageQuizzes(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Quiz>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(QuizInterface::class.java, params).getNextPageQuizzes(nextUrl)).enqueue(callback)
    }

    fun editQuiz(courseId: Long, assignmentId: Long, body: QuizPostBodyWrapper, adapter: RestBuilder, callback: StatusCallback<Quiz>, params: RestParams) {
        callback.addCall(adapter.buildSerializeNulls(QuizInterface::class.java, params).editQuiz(courseId, assignmentId, body)).enqueue(callback)
    }

    fun getQuizSubmissions(canvasContext: CanvasContext, quizId: Long, adapter: RestBuilder, callback: StatusCallback<QuizSubmissionResponse>, params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(QuizInterface::class.java, params).getFirstPageQuizSubmissions(canvasContext.id, quizId)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(QuizInterface::class.java, params).getNextPageQuizSubmissions(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getFirstPageQuizList(canvasContext: CanvasContext, adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<Quiz>>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getFirstPageQuizzesList(canvasContext.apiContext(), canvasContext.id)).enqueue(callback)
    }

    fun getNextPageQuizList(nextUrl: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<Quiz>>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getNextPageQuizzesList(nextUrl)).enqueue(callback)
    }

    fun getDetailedQuiz(canvasContext: CanvasContext, quizId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Quiz>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getDetailedQuiz(canvasContext.apiContext(), canvasContext.id, quizId)).enqueue(callback)
    }

    fun getDetailedQuizByUrl(quizUrl: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Quiz>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getDetailedQuizByUrl(quizUrl)).enqueue(callback)
    }

    fun submitQuiz(canvasContext: CanvasContext,
                   quizSubmission: QuizSubmission,
                   adapter: RestBuilder,
                   params: RestParams,
                   callback: StatusCallback<QuizSubmissionResponse>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).submitQuiz(
                canvasContext.apiContext(),
                canvasContext.id,
                quizSubmission.quizId,
                quizSubmission.id,
                quizSubmission.attempt,
                quizSubmission.validationToken!!)).enqueue(callback)
    }

    fun getFirstPageQuizSubmissions(canvasContext: CanvasContext, quizId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<QuizSubmissionResponse>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getFirstPageQuizSubmissions(canvasContext.apiContext(), canvasContext.id, quizId)).enqueue(callback)
    }

    fun postQuizStartedEvent(
            canvasContext: CanvasContext,
            quizId: Long,
            submissionId: Long,
            userAgent: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<ResponseBody>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params)
                .postQuizStartedEvent(canvasContext.apiContext(), canvasContext.id, quizId, submissionId, "android_session_started", userAgent)).enqueue(callback)
    }

    fun startQuiz(
            canvasContext: CanvasContext,
            quizId: Long,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).startQuiz(canvasContext.apiContext(), canvasContext.id, quizId)).enqueue(callback)
    }

    fun getQuizSubmissionTime(
            canvasContext: CanvasContext,
            quizId: Long,
            submissionId: Long,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionTime>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).getQuizSubmissionTime(canvasContext.apiContext(), canvasContext.id, quizId, submissionId)).enqueue(callback)
    }

    fun postQuizSubmit(
            canvasContext: CanvasContext,
            quizSubmission: QuizSubmission,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).postQuizSubmit(
                canvasContext.apiContext(),
                canvasContext.id,
                quizSubmission.quizId,
                quizSubmission.id,
                quizSubmission.attempt,
                quizSubmission.validationToken!!)).enqueue(callback)
    }

    fun postQuizQuestionFileUpload(
            quizSubmission: QuizSubmission,
            answer: Long,
            questionId: Long,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionQuestionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).postQuizQuestionFileUpload(
                quizSubmission.id,
                quizSubmission.attempt,
                quizSubmission.validationToken!!,
                questionId,
                if (answer == -1L) "" else answer.toString()
        )).enqueue(callback)
    }


    fun postQuizQuestionMatching(
            quizSubmission: QuizSubmission,
            questionId: Long,
            answers: HashMap<Long, Int>,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionQuestionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).postQuizQuestionUrl(
                buildMatchingUrl(quizSubmission.id,
                        quizSubmission.attempt,
                        quizSubmission.validationToken!!,
                        questionId,
                        answers))).enqueue(callback)
    }

    fun postQuizQuestionMultipleDropdown(
            quizSubmission: QuizSubmission,
            questionId: Long,
            answers: HashMap<String, Long>,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionQuestionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).postQuizQuestionUrl(
                buildMultipleDropdownUrl(
                        quizSubmission.id,
                        quizSubmission.attempt,
                        quizSubmission.validationToken!!,
                        questionId,
                        answers))).enqueue(callback)
    }

    fun postQuizQuestionMultiAnswers(
            quizSubmission: QuizSubmission,
            questionId: Long,
            answers: ArrayList<Long>,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionQuestionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).postQuizQuestionUrl(
                buildMultiAnswerUrl(
                        quizSubmission.id,
                        quizSubmission.attempt,
                        quizSubmission.validationToken!!,
                        questionId,
                        answers))).enqueue(callback)
    }

    fun postQuizQuestionEssay(
            quizSubmission: QuizSubmission,
            questionId: Long,
            answer: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionQuestionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).postQuizQuestionEssay(
                quizSubmission.id,
                quizSubmission.attempt,
                quizSubmission.validationToken!!,
                questionId,
                answer)).enqueue(callback)
    }

    fun postQuizQuestionMultiChoice(
            submissionId: Long,
            attempts: Int,
            questionId: Long,
            answer: Long,
            token: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<QuizSubmissionQuestionResponse>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).postQuizQuestionMultiChoice(
                submissionId,
                attempts,
                token,
                questionId,
                answer)).enqueue(callback)
    }

    fun putFlagQuizQuestion(
            quizSubmission: QuizSubmission,
            questionId: Long,
            flagQuestion: Boolean,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<ResponseBody>) {

        callback.addCall(adapter.build(QuizInterface::class.java, params).putFlagQuizQuestion(
                quizSubmission.id,
                questionId,
                if (flagQuestion) "flag" else "unflag",
                quizSubmission.attempt,
                quizSubmission.validationToken!!)).enqueue(callback)
    }

    private fun buildMultiAnswerUrl(quizSubmissionId: Long, attempt: Int, validationToken: String, questionId: Long, answers: ArrayList<Long>): String {
        // Build the entire relative URL because Retrofit 2 forcefully escapes @Path params. It will end up looking like:
        // quiz_submissions/{submission_id}/questions?attempt={attempt}&validation_token={validation_token}&quiz_questions[][id]={question_id}&quiz_questions[][answer][]={answer_id}...
        val url = "quiz_submissions/$quizSubmissionId/questions?attempt=$attempt&validation_token=$validationToken&quiz_questions[][id]=$questionId"
        val builder = StringBuilder(url)
        for (answer in answers) {
            builder.append("&quiz_questions[][answer][]=$answer")
        }

        return builder.toString()
    }

    private fun buildMatchingUrl(quizSubmissionId: Long, attempt: Int, validationToken: String, questionId: Long, answers: HashMap<Long, Int>): String {
        // Build the entire relative URL because Retrofit 2 forcefully escapes @Path params. It will end up looking like:
        // quiz_submissions/{submission_id}/questions?attempt={attempt}&validation_token={validation_token}&quiz_questions[][id]={question_id}&quiz_questions[][answer][][answer_id]={answer_id}&quiz_questions[][answer][][match_id]={match_id}...
        val url = "quiz_submissions/$quizSubmissionId/questions?attempt=$attempt&validation_token=$validationToken&quiz_questions[][id]=$questionId"
        val builder = StringBuilder(url)
        // Loop through the HashMap that contains the list of answers and their matches that the user selected
        for ((key, value) in answers) {
            builder.append("&quiz_questions[][answer][][answer_id]=$key&quiz_questions[][answer][][match_id]=$value")
        }

        return builder.toString()
    }

    private fun buildMultipleDropdownUrl(quizSubmissionId: Long, attempt: Int, validationToken: String, questionId: Long, answers: HashMap<String, Long>): String {
        // Build the entire relative URL because Retrofit 2 forcefully escapes @Path params. It will end up looking like:
        // quiz_submissions/{submission_id}/questions?attempt={attempt}&validation_token={validation_token}&quiz_questions[][id]={question_id}&quiz_questions[][answer][{answerKey}]={answerValue}...
        val url = "quiz_submissions/$quizSubmissionId/questions?attempt=$attempt&validation_token=$validationToken&quiz_questions[][id]=$questionId"
        val builder = StringBuilder(url)
        // Loop through the HashMap that contains the list of answers and their matches that the user selected
        for ((key, value) in answers) {
            builder.append("&quiz_questions[][answer][$key]=$value")
        }

        return builder.toString()
    }
}

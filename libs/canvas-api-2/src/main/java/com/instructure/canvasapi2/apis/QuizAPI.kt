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
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.*
import java.util.*

object QuizAPI {

    interface QuizInterface {
        @GET("{contextType}/{contextId}/all_quizzes")
        fun getFirstPageQuizzesList(@Path("contextType") contextType: String, @Path("contextId") contextId: Long): Call<List<Quiz>>

        @GET("{contextType}/{contextId}/all_quizzes")
        suspend fun getFirstPageQuizzesList(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Tag restParams: RestParams): DataResult<List<Quiz>>

        @GET
        fun getNextPageQuizzesList(@Url nextURL: String): Call<List<Quiz>>

        @GET
        suspend fun getNextPageQuizzesList(@Url nextURL: String, @Tag restParams: RestParams): DataResult<List<Quiz>>

        @GET("{contextType}/{contextId}/quizzes/{quizId}")
        fun getDetailedQuiz(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("quizId") quizId: Long): Call<Quiz>

        @GET
        fun getDetailedQuizByUrl(@Url quizUrl: String): Call<Quiz>

        @GET
        suspend fun getDetailedQuizByUrl(@Url quizUrl: String, @Tag params: RestParams): DataResult<Quiz>

        @GET("courses/{courseId}/all_quizzes")
        fun getFirstPageQuizzes(@Path("courseId") contextId: Long): Call<List<Quiz>>

        @GET
        fun getNextPageQuizzes(@Url nextUrl: String): Call<List<Quiz>>

        @GET("courses/{courseId}/quizzes/{quizId}")
        fun getQuiz(@Path("courseId") courseId: Long, @Path("quizId") quizId: Long): Call<Quiz>

        @GET("courses/{courseId}/quizzes/{quizId}")
        suspend fun getQuiz(@Path("courseId") courseId: Long, @Path("quizId") quizId: Long, @Tag restParams: RestParams): DataResult<Quiz>

        @PUT("courses/{courseId}/quizzes/{quizId}")
        fun editQuiz(
                @Path("courseId") courseId: Long,
                @Path("quizId") quizId: Long,
                @Body body: QuizPostBodyWrapper): Call<Quiz>

        @GET("{contextType}/{contextId}/quizzes/{quizId}/submissions")
        fun getFirstPageQuizSubmissions(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("quizId") quizId: Long): Call<QuizSubmissionResponse>
    }

    fun getQuiz(courseId: Long, quizId: Long, adapter: RestBuilder, callback: StatusCallback<Quiz>, params: RestParams) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getQuiz(courseId, quizId)).enqueue(callback)
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

    fun getFirstPageQuizSubmissions(canvasContext: CanvasContext, quizId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<QuizSubmissionResponse>) {
        callback.addCall(adapter.build(QuizInterface::class.java, params).getFirstPageQuizSubmissions(canvasContext.apiContext(), canvasContext.id, quizId)).enqueue(callback)
    }
}

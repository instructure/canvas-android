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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.QuizPostBody
import com.instructure.canvasapi2.models.postmodels.QuizPostBodyWrapper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.canvasapi2.utils.weave.awaitApi
import okhttp3.ResponseBody
import java.util.*

object QuizManager {

    @JvmStatic
    fun getAllQuizzes(courseId: Long, forceNetwork: Boolean, callback: StatusCallback<List<Quiz>>) {
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Quiz>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Quiz>>, nextUrl: String, isCached: Boolean) {
                QuizAPI.getNextPageQuizzes(forceNetwork, nextUrl, adapter, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        QuizAPI.getFirstPageQuizzes(courseId, forceNetwork, adapter, depaginatedCallback)
    }

    @JvmStatic
    fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean, callback: StatusCallback<Quiz>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        QuizAPI.getQuiz(courseId, quizId, adapter, callback, params)
    }

    fun getQuizAsync(courseId: Long, quizId: Long, forceNetwork: Boolean) = apiAsync<Quiz> {
        getQuiz(courseId, quizId, forceNetwork, it)
    }

    @JvmStatic
    fun editQuiz(courseId: Long, quizId: Long, body: QuizPostBody, callback: StatusCallback<Quiz>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        val bodyWrapper = QuizPostBodyWrapper()

        bodyWrapper.quiz = body
        QuizAPI.editQuiz(courseId, quizId, bodyWrapper, adapter, callback, params)
    }

    @JvmStatic
    fun getQuizSubmissions(
        canvasContext: CanvasContext,
        quizId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getQuizSubmissions(canvasContext, quizId, adapter, callback, params)
    }

    @JvmStatic
    fun getFirstPageQuizList(
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<List<Quiz>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getFirstPageQuizList(canvasContext, adapter, params, callback)
    }

    @JvmStatic
    fun getNextPageQuizList(nextPage: String, forceNetwork: Boolean, callback: StatusCallback<List<Quiz>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getNextPageQuizList(nextPage, adapter, params, callback)
    }

    @JvmStatic
    fun getDetailedQuiz(
        canvasContext: CanvasContext,
        quizId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<Quiz>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getDetailedQuiz(canvasContext, quizId, adapter, params, callback)
    }

    @JvmStatic
    fun getDetailedQuizByUrl(quizUrl: String, forceNetwork: Boolean, callback: StatusCallback<Quiz>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getDetailedQuizByUrl(quizUrl, adapter, params, callback)
    }

    @JvmStatic
    fun submitQuiz(
        canvasContext: CanvasContext,
        quizSubmission: QuizSubmission,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.submitQuiz(canvasContext, quizSubmission, adapter, params, callback)
    }

    @JvmStatic
    fun getFirstPageQuizSubmissions(
        canvasContext: CanvasContext,
        quizId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getFirstPageQuizSubmissions(canvasContext, quizId, adapter, params, callback)
    }

    @JvmStatic
    fun postQuizStartedEvent(
        canvasContext: CanvasContext,
        quizId: Long,
        submissionId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<ResponseBody>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizStartedEvent(canvasContext, quizId, submissionId, ApiPrefs.userAgent, adapter, params, callback)
    }

    @JvmStatic
    fun startQuiz(
        canvasContext: CanvasContext,
        quizId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.startQuiz(canvasContext, quizId, adapter, params, callback)
    }

    @JvmStatic
    fun getQuizSubmissionTime(
        canvasContext: CanvasContext,
        quizSubmission: QuizSubmission,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionTime>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getQuizSubmissionTime(
            canvasContext,
            quizSubmission.quizId,
            quizSubmission.id,
            adapter,
            params,
            callback
        )
    }

    @JvmStatic
    fun postQuizSubmit(
        canvasContext: CanvasContext,
        quizSubmission: QuizSubmission,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizSubmit(canvasContext, quizSubmission, adapter, params, callback)
    }

    @JvmStatic
    fun getFirstPageSubmissionQuestions(
        quizSubmissionId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getFirstPageSubmissionQuestions(quizSubmissionId, adapter, params, callback)
    }

    @JvmStatic
    fun getNextPageSubmissionQuestions(
        nextUrl: String,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getNextPageSubmissionQuestions(nextUrl, adapter, params, callback)
    }

    @JvmStatic
    fun postQuizQuestionFileUpload(
        quizSubmission: QuizSubmission,
        answer: Long,
        questionId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizQuestionFileUpload(quizSubmission, answer, questionId, adapter, params, callback)
    }

    @JvmStatic
    fun postQuizQuestionMatching(
        quizSubmission: QuizSubmission,
        questionId: Long,
        answers: HashMap<Long, Int>,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizQuestionMatching(quizSubmission, questionId, answers, adapter, params, callback)
    }

    @JvmStatic
    fun postQuizQuestionMultipleDropdown(
        quizSubmission: QuizSubmission,
        questionId: Long,
        answers: HashMap<String, Long>,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizQuestionMultipleDropdown(quizSubmission, questionId, answers, adapter, params, callback)
    }

    @JvmStatic
    fun postQuizQuestionMultiAnswer(
        quizSubmission: QuizSubmission,
        questionId: Long,
        answers: ArrayList<Long>,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizQuestionMultiAnswers(quizSubmission, questionId, answers, adapter, params, callback)
    }

    @JvmStatic
    fun postQuizQuestionEssay(
        quizSubmission: QuizSubmission,
        answer: String,
        questionId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizQuestionEssay(quizSubmission, questionId, answer, adapter, params, callback)
    }

    @JvmStatic
    fun postQuizQuestionMultiChoice(
        quizSubmission: QuizSubmission,
        answer: Long,
        questionId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<QuizSubmissionQuestionResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.postQuizQuestionMultiChoice(
            quizSubmission.id,
            quizSubmission.attempt,
            questionId,
            answer,
            quizSubmission.validationToken!!,
            adapter,
            params,
            callback
        )
    }

    @JvmStatic
    fun putFlagQuizQuestion(
        quizSubmission: QuizSubmission,
        questionId: Long,
        forceNetwork: Boolean,
        flagQuestion: Boolean,
        callback: StatusCallback<ResponseBody>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.putFlagQuizQuestion(quizSubmission, questionId, flagQuestion, adapter, params, callback)
    }

}

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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizSubmissionResponse
import com.instructure.canvasapi2.models.postmodels.QuizPostBody
import com.instructure.canvasapi2.models.postmodels.QuizPostBodyWrapper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync

object QuizManager {

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

    fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean, callback: StatusCallback<Quiz>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        QuizAPI.getQuiz(courseId, quizId, adapter, callback, params)
    }

    fun getQuizAsync(courseId: Long, quizId: Long, forceNetwork: Boolean) = apiAsync<Quiz> {
        getQuiz(courseId, quizId, forceNetwork, it)
    }

    fun editQuiz(courseId: Long, quizId: Long, body: QuizPostBody, callback: StatusCallback<Quiz>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        val bodyWrapper = QuizPostBodyWrapper()

        bodyWrapper.quiz = body
        QuizAPI.editQuiz(courseId, quizId, bodyWrapper, adapter, callback, params)
    }

    fun getFirstPageQuizList(
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<List<Quiz>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getFirstPageQuizList(canvasContext, adapter, params, callback)
    }

    fun getNextPageQuizList(nextPage: String, forceNetwork: Boolean, callback: StatusCallback<List<Quiz>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getNextPageQuizList(nextPage, adapter, params, callback)
    }

    fun getDetailedQuiz(
        canvasContext: CanvasContext,
        quizId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<Quiz>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork, domain = ApiPrefs.overrideDomains[canvasContext.id])

        QuizAPI.getDetailedQuiz(canvasContext, quizId, adapter, params, callback)
    }

    fun getDetailedQuizByUrl(quizUrl: String, forceNetwork: Boolean, callback: StatusCallback<Quiz>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        QuizAPI.getDetailedQuizByUrl(quizUrl, adapter, params, callback)
    }

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

}

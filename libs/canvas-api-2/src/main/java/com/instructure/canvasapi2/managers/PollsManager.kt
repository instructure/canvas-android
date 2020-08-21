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
import com.instructure.canvasapi2.apis.PollSubmissionAPI
import com.instructure.canvasapi2.apis.PollsAPI
import com.instructure.canvasapi2.apis.PollsChoiceAPI
import com.instructure.canvasapi2.apis.PollsSessionAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.PollChoiceResponse
import com.instructure.canvasapi2.models.PollResponse
import com.instructure.canvasapi2.models.PollSessionResponse
import com.instructure.canvasapi2.models.PollSubmissionResponse

import okhttp3.ResponseBody

object PollsManager {

    fun getFirstPagePolls(callback: StatusCallback<PollResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsAPI.getFirstPagePolls(adapter, params, callback)
    }

    fun getNextPagePolls(nextUrl: String, callback: StatusCallback<PollResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsAPI.getNextPagePolls(nextUrl, adapter, params, callback)
    }

    fun createPollSession(
        pollId: Long,
        courseId: Long,
        sectionId: Long,
        callback: StatusCallback<PollSessionResponse>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.createPollSession(pollId, courseId, sectionId, adapter, params, callback)
    }

    fun openPollSession(pollId: Long, sectionId: Long, callback: StatusCallback<ResponseBody>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.openPollSession(pollId, sectionId, adapter, params, callback)
    }

    fun getFirstPagePollSessions(pollId: Long, callback: StatusCallback<PollSessionResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.getFirstPagePollSessions(pollId, adapter, params, callback)
    }

    fun getNextPagePollSessions(nextUrl: String, callback: StatusCallback<PollSessionResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.getNextPagePollSessions(nextUrl, adapter, params, callback)
    }

    fun updatePoll(pollId: Long, title: String, callback: StatusCallback<PollResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsAPI.updatePoll(pollId, title, adapter, params, callback)
    }

    fun deletePollChoice(
        pollId: Long,
        pollChoiceId: Long,
        callback: StatusCallback<ResponseBody>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsChoiceAPI.deletePollChoice(pollId, pollChoiceId, adapter, params, callback)
    }

    fun deletePollSession(
        pollId: Long,
        pollSessionId: Long,
        callback: StatusCallback<ResponseBody>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.deletePollSession(pollId, pollSessionId, adapter, params, callback)
    }

    fun createPoll(title: String, callback: StatusCallback<PollResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsAPI.createPoll(title, adapter, params, callback)
    }

    fun createPollChoice(
        pollId: Long,
        text: String,
        isCorrect: Boolean,
        position: Int,
        callback: StatusCallback<PollChoiceResponse>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsChoiceAPI.createPollChoice(pollId, text, isCorrect, position, adapter, params, callback)
    }

    fun updatePollChoice(
        pollId: Long,
        pollChoiceId: Long,
        text: String,
        isCorrect: Boolean,
        position: Int,
        callback: StatusCallback<PollChoiceResponse>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsChoiceAPI.updatePollChoice(pollId, pollChoiceId, text, isCorrect, position, adapter, params, callback)
    }

    fun getClosedSessions(callback: StatusCallback<PollSessionResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.getClosedSessions(adapter, params, callback)
    }

    fun getOpenSessions(callback: StatusCallback<PollSessionResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.getOpenSessions(adapter, params, callback)
    }

    fun getSinglePoll(pollId: Long, callback: StatusCallback<PollResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsAPI.getSinglePoll(pollId, adapter, params, callback)
    }

    fun getSinglePollSession(
        pollId: Long,
        pollSessionId: Long,
        callback: StatusCallback<PollSessionResponse>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.getSinglePollSession(pollId, pollSessionId, adapter, params, callback)
    }

    fun closePollSession(
        pollId: Long,
        pollSessionId: Long,
        callback: StatusCallback<ResponseBody>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.closePollSession(pollId, pollSessionId, adapter, params, callback)
    }

    fun updatePollSession(
        pollId: Long,
        pollSessionId: Long,
        courseId: Long,
        sectionId: Long,
        hasPublicResults: Boolean,
        callback: StatusCallback<PollSessionResponse>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsSessionAPI.updatePollSession(
            pollId,
            pollSessionId,
            courseId,
            sectionId,
            hasPublicResults,
            adapter,
            params,
            callback
        )
    }

    fun getFirstPagePollChoices(pollId: Long, callback: StatusCallback<PollChoiceResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsChoiceAPI.getFirstPagePollChoices(pollId, adapter, params, callback)
    }

    fun getNextPagePollChoices(nextUrl: String, callback: StatusCallback<PollChoiceResponse>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsChoiceAPI.getNextPagePollChoices(nextUrl, adapter, params, callback)
    }

    fun deletePoll(pollId: Long, callback: StatusCallback<ResponseBody>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollsAPI.deletePoll(pollId, adapter, params, callback)
    }

    fun createPollSubmission(
        pollId: Long,
        pollSessionId: Long,
        pollChoiceId: Long,
        callback: StatusCallback<PollSubmissionResponse>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        PollSubmissionAPI.createPollSubmission(pollId, pollSessionId, pollChoiceId, adapter, params, callback)
    }

}

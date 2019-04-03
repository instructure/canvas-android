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
import com.instructure.canvasapi2.models.PollSubmissionResponse

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object PollSubmissionAPI {

    internal interface PollSubmissionInterface {

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}/poll_submissions/{pollSubmissionId}")
        fun getPollSubmission(
                @Path("pollId") pollId: Long,
                @Path("pollSessionId") pollSessionId: Long,
                @Path("pollSubmissionId") pollSubmissionId: Long): Call<PollSubmissionResponse>

        @POST("polls/{pollId}/poll_sessions/{pollSessionId}/poll_submissions")
        fun createPollSubmission(
                @Path("pollId") pollId: Long,
                @Path("pollSessionId") pollSessionId: Long,
                @Query("poll_submissions[][poll_choice_id]") pollChoiceId: Long,
                @Body body: String): Call<PollSubmissionResponse>
    }

    fun getPollSubmission(pollId: Long, pollSessionId: Long, pollSubmissionId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSubmissionResponse>) {
        callback.addCall(adapter.build(PollSubmissionInterface::class.java, params).getPollSubmission(pollId, pollSessionId, pollSubmissionId)).enqueue(callback)
    }

    fun createPollSubmission(pollId: Long, pollSessionId: Long, pollChoiceId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSubmissionResponse>) {
        callback.addCall(adapter.build(PollSubmissionInterface::class.java, params).createPollSubmission(pollId, pollSessionId, pollChoiceId, "")).enqueue(callback)
    }
}

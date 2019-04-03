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
import com.instructure.canvasapi2.models.PollSessionResponse

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

object PollsSessionAPI {

    internal interface PollSessionInterface {

        @GET("poll_sessions/opened")
        fun getOpenSessions(): Call<PollSessionResponse>

        @GET("poll_sessions/closed")
        fun getClosedSessions(): Call<PollSessionResponse>

        @GET("polls/{pollId}/poll_sessions")
        fun getFirstPagePollSessionsList(@Path("pollId") pollId: Long): Call<PollSessionResponse>

        @GET("{next}")
        fun getNextPagePollSessionsList(@Path(value = "next", encoded = false) nextURL: String): Call<PollSessionResponse>

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}")
        fun getSinglePollSession(@Path("pollId") pollId: Long, @Path("pollSessionId") poll_sessionId: Long): Call<PollSessionResponse>

        @POST("polls/{pollId}/poll_sessions")
        fun createPollSession(
                @Path("pollId") pollId: Long,
                @Query("poll_sessions[][course_id]") courseId: Long,
                @Query("poll_sessions[][course_section_id]") courseSectionId: Long,
                @Body body: String): Call<PollSessionResponse>

        @PUT("polls/{pollId}/poll_sessions/{pollSessionId}")
        fun updatePollSession(
                @Path("pollId") pollId: Long,
                @Path("pollSessionId") pollSessionId: Long,
                @Query("poll_sessions[][course_id]") courseId: Long,
                @Query("poll_sessions[][course_section_id]") courseSectionId: Long,
                @Query("poll_sessions[][has_public_results]") hasPublicResults: Boolean,
                @Body body: String): Call<PollSessionResponse>

        @DELETE("polls/{pollId}/poll_sessions/{pollSessionId}")
        fun deletePollSession(
                @Path("pollId") pollId: Long,
                @Path("pollSessionId") pollSessionId: Long): Call<ResponseBody>

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}/open")
        fun openPollSession(@Path("pollId") pollId: Long, @Path("pollSessionId") pollSessionId: Long): Call<ResponseBody>

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}/close")
        fun closePollSession(@Path("pollId") pollId: Long, @Path("pollSessionId") pollSessionId: Long): Call<ResponseBody>
    }

    fun getFirstPagePollSessions(pollId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSessionResponse>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).getFirstPagePollSessionsList(pollId)).enqueue(callback)
    }

    fun getNextPagePollSessions(nextURL: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSessionResponse>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).getNextPagePollSessionsList(nextURL)).enqueue(callback)
    }

    fun getSinglePollSession(pollId: Long, pollSessionId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSessionResponse>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).getSinglePollSession(pollId, pollSessionId)).enqueue(callback)
    }

    fun createPollSession(pollId: Long, courseId: Long, courseSectionId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSessionResponse>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).createPollSession(pollId, courseId, courseSectionId, "")).enqueue(callback)
    }

    fun updatePollSession(pollId: Long, pollSessionId: Long, courseId: Long, courseSectionId: Long, hasPublicResults: Boolean, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSessionResponse>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).updatePollSession(pollId, pollSessionId, courseId, courseSectionId, hasPublicResults, "")).enqueue(callback)
    }

    fun deletePollSession(pollId: Long, pollSessionId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).deletePollSession(pollId, pollSessionId)).enqueue(callback)
    }

    fun openPollSession(pollId: Long, pollSessionId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).openPollSession(pollId, pollSessionId)).enqueue(callback)
    }

    fun closePollSession(pollId: Long, pollSessionId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).closePollSession(pollId, pollSessionId)).enqueue(callback)
    }

    fun getOpenSessions(adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSessionResponse>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).getOpenSessions()).enqueue(callback)
    }

    fun getClosedSessions(adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollSessionResponse>) {
        callback.addCall(adapter.build(PollSessionInterface::class.java, params).getClosedSessions()).enqueue(callback)
    }
}

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
import com.instructure.canvasapi2.models.PollResponse

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

object PollsAPI {

    internal interface PollsInterface {

        @GET("polls")
        fun getPollsList(): Call<PollResponse>

        @GET("{next}")
        fun next(@Path(value = "next", encoded = false) nextURL: String): Call<PollResponse>

        @GET("polls/{pollId}")
        fun getSinglePoll(@Path("pollId") pollId: Long): Call<PollResponse>

        @POST("polls")
        fun createPoll(@Query("polls[][question]") pollTitle: String, @Body body: String): Call<PollResponse>

        @PUT("polls/{pollId}")
        fun updatePoll(@Path("pollId") pollId: Long, @Query("polls[][question]") pollTitle: String, @Body body: String): Call<PollResponse>

        @DELETE("polls/{pollId}")
        fun deletePoll(@Path("pollId") pollId: Long): Call<ResponseBody>
    }

    fun getFirstPagePolls(adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollResponse>) {
        callback.addCall(adapter.build(PollsInterface::class.java, params).getPollsList()).enqueue(callback)
    }

    fun getNextPagePolls(nextURL: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollResponse>) {
        callback.addCall(adapter.build(PollsInterface::class.java, params).next(nextURL)).enqueue(callback)
    }

    fun getSinglePoll(pollId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollResponse>) {
        callback.addCall(adapter.build(PollsInterface::class.java, params).getSinglePoll(pollId)).enqueue(callback)
    }

    fun createPoll(title: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollResponse>) {
        callback.addCall(adapter.build(PollsInterface::class.java, params).createPoll(title, "")).enqueue(callback)
    }

    fun updatePoll(pollId: Long, title: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollResponse>) {
        callback.addCall(adapter.build(PollsInterface::class.java, params).updatePoll(pollId, title, "")).enqueue(callback)
    }

    fun deletePoll(pollId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(PollsInterface::class.java, params).deletePoll(pollId)).enqueue(callback)
    }
}

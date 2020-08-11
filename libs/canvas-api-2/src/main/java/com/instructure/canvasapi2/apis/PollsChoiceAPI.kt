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
import com.instructure.canvasapi2.models.PollChoiceResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object PollsChoiceAPI {

    internal interface PollChoiceInterface {
        @GET("polls/{pollId}/poll_choices")
        fun getFirstPagePollChoicesList(@Path("pollId") pollId: Long): Call<PollChoiceResponse>

        @GET("{next}")
        fun getNextPagePollChoicesList(@Path(value = "next") nextURL: String?): Call<PollChoiceResponse>

        @POST("polls/{pollId}/poll_choices")
        fun createPollChoice(
            @Path("pollId") pollId: Long,
            @Query("poll_choices[][text]") pollChoiceText: String?,
            @Query("poll_choices[][is_correct]") isCorrect: Boolean,
            @Query("poll_choices[][position]") position: Int,
            @Body body: String?
        ): Call<PollChoiceResponse>

        @PUT("polls/{pollId}/poll_choices/{poll_choice_id}")
        fun updatePollChoice(
            @Path("pollId") pollId: Long,
            @Path("poll_choice_id") poll_choiceId: Long,
            @Query("poll_choices[][text]") pollChoiceText: String?,
            @Query("poll_choices[][is_correct]") isCorrect: Boolean,
            @Query("poll_choices[][position]") position: Int, @Body body: String?
        ): Call<PollChoiceResponse>

        @DELETE("polls/{pollId}/poll_choices/{poll_choice_id}")
        fun deletePollChoice(
            @Path("pollId") pollId: Long,
            @Path("poll_choice_id") poll_choiceId: Long
        ): Call<ResponseBody>
    }

    fun getFirstPagePollChoices(pollId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollChoiceResponse>) {
        callback.addCall(adapter.build(PollChoiceInterface::class.java, params).getFirstPagePollChoicesList(pollId)).enqueue(callback)
    }

    fun getNextPagePollChoices(nextURL: String?, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollChoiceResponse>) {
        callback.addCall(adapter.build(PollChoiceInterface::class.java, params).getNextPagePollChoicesList(nextURL)).enqueue(callback)
    }

    fun createPollChoice(pollId: Long, text: String?, isCorrect: Boolean, position: Int, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollChoiceResponse>) {
        callback.addCall(adapter.build(PollChoiceInterface::class.java, params).createPollChoice(pollId, text, isCorrect, position, "")).enqueue(callback)
    }

    fun updatePollChoice(pollId: Long, pollChoiceId: Long, text: String?, isCorrect: Boolean, position: Int, adapter: RestBuilder, params: RestParams, callback: StatusCallback<PollChoiceResponse>) {
        callback.addCall(adapter.build(PollChoiceInterface::class.java, params).updatePollChoice(pollId, pollChoiceId, text, isCorrect, position, "")).enqueue(callback)
    }

    fun deletePollChoice(pollId: Long, pollChoiceId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(PollChoiceInterface::class.java, params).deletePollChoice(pollId, pollChoiceId)).enqueue(callback)
    }
}

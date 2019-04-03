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
package com.instructure.canvasapi2.apis;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.PollChoiceResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class PollsChoiceAPI {

    interface PollChoiceInterface {
        @GET("polls/{pollId}/poll_choices")
        Call<PollChoiceResponse> getFirstPagePollChoicesList(
                @Path("pollId") long pollId);

        @GET("{next}")
        Call<PollChoiceResponse> getNextPagePollChoicesList(
                @Path(value = "next") String nextURL);

        @POST("polls/{pollId}/poll_choices")
        Call<PollChoiceResponse> createPollChoice(
                @Path("pollId") long pollId,
                @Query("poll_choices[][text]") String pollChoiceText,
                @Query("poll_choices[][is_correct]") boolean isCorrect,
                @Query("poll_choices[][position]") int position,
                @Body String body);

        @PUT("polls/{pollId}/poll_choices/{poll_choice_id}")
        Call<PollChoiceResponse> updatePollChoice(
                @Path("pollId") long pollId,
                @Path("poll_choice_id") long poll_choiceId,
                @Query("poll_choices[][text]") String pollChoiceText,
                @Query("poll_choices[][is_correct]") boolean isCorrect,
                @Query("poll_choices[][position]") int position, @Body String body);

        @DELETE("polls/{pollId}/poll_choices/{poll_choice_id}")
        Call<ResponseBody> deletePollChoice(@Path("pollId") long pollId, @Path("poll_choice_id") long poll_choiceId);
    }

    public static void getFirstPagePollChoices(long pollId, RestBuilder adapter, RestParams params, StatusCallback<PollChoiceResponse> callback) {
        callback.addCall(adapter.build(PollChoiceInterface.class, params).getFirstPagePollChoicesList(pollId)).enqueue(callback);
    }

    public static void getNextPagePollChoices(String nextURL, RestBuilder adapter, RestParams params, StatusCallback<PollChoiceResponse> callback){
        callback.addCall(adapter.build(PollChoiceInterface.class, params).getNextPagePollChoicesList(nextURL)).enqueue(callback);
    }

    public static void createPollChoice(long pollId, String text, boolean isCorrect, int position, RestBuilder adapter, RestParams params, StatusCallback<PollChoiceResponse> callback) {
        callback.addCall(adapter.build(PollChoiceInterface.class, params).createPollChoice(pollId, text, isCorrect, position, "")).enqueue(callback);
    }

    public static void updatePollChoice(long pollId, long pollChoiceId, String text, boolean isCorrect, int position, RestBuilder adapter, RestParams params, StatusCallback<PollChoiceResponse> callback) {
        callback.addCall(adapter.build(PollChoiceInterface.class, params).updatePollChoice(pollId, pollChoiceId, text, isCorrect, position, "")).enqueue(callback);
    }

    public static void deletePollChoice(long pollId, long pollChoiceId, RestBuilder adapter, RestParams params, StatusCallback<ResponseBody> callback) {
        callback.addCall(adapter.build(PollChoiceInterface.class, params).deletePollChoice(pollId, pollChoiceId)).enqueue(callback);
    }
}
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

package com.instructure.canvasapi.api;

import com.instructure.canvasapi.model.PollChoiceResponse;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;


public class PollChoiceAPI extends BuildInterfaceAPI {

    interface PollChoiceInterface {
        @GET("/polls/{pollid}/poll_choices")
        void getFirstPagePollChoicesList(@Path("pollid") long poll_id, Callback<PollChoiceResponse> callback);

        @GET("/{next}")
        void getNextPagePollChoicesList(@Path(value = "next", encode = false) String nextURL, Callback<PollChoiceResponse> callback);

        @GET("/polls/{pollid}/poll_choices/{poll_choice_id}")
        void getSinglePollChoice(@Path("pollid") long poll_id, @Path("poll_choice_id") long poll_choice_id, Callback<PollChoiceResponse> callback);

        @POST("/polls/{pollid}/poll_choices")
        void createPollChoice(@Path("pollid") long poll_id, @Query("poll_choices[][text]") String pollChoiceText, @Query("poll_choices[][is_correct]") boolean isCorrect, @Query("poll_choices[][position]") int position, @Body String body, Callback<PollChoiceResponse> callback);

        @PUT("/polls/{pollid}/poll_choices/{poll_choice_id}")
        void updatePollChoice(@Path("pollid") long poll_id, @Path("poll_choice_id") long poll_choice_id, @Query("poll_choices[][text]") String pollChoiceText, @Query("poll_choices[][is_correct]") boolean isCorrect, @Query("poll_choices[][position]") int position, @Body String body, Callback<PollChoiceResponse> callback);

        @DELETE("/polls/{pollid}/poll_choices/{poll_choice_id}")
        void deletePollChoice(@Path("pollid") long poll_id, @Path("poll_choice_id") long poll_choice_id, Callback<Response> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPagePollChoices(long poll_id, CanvasCallback<PollChoiceResponse> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(PollChoiceInterface.class, callback).getFirstPagePollChoicesList(poll_id, callback);
        buildInterface(PollChoiceInterface.class, callback).getFirstPagePollChoicesList(poll_id, callback);
    }

    public static void getNextPagePollChoices(String nextURL, CanvasCallback<PollChoiceResponse> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(PollChoiceInterface.class, callback, false).getNextPagePollChoicesList(nextURL, callback);
        buildInterface(PollChoiceInterface.class, callback, false).getNextPagePollChoicesList(nextURL, callback);
    }

    public static void getSinglePollChoice(long poll_id, long poll_choice_id, CanvasCallback<PollChoiceResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_choice_id)) { return; }

        buildCacheInterface(PollChoiceInterface.class, callback).getSinglePollChoice(poll_id, poll_choice_id, callback);
        buildInterface(PollChoiceInterface.class, callback).getSinglePollChoice(poll_id, poll_choice_id, callback);
    }

    public static void createPollChoice(long poll_id, String text, boolean is_correct, int position, CanvasCallback<PollChoiceResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, text, is_correct)) { return; }

        buildInterface(PollChoiceInterface.class, callback).createPollChoice(poll_id, text, is_correct, position, "", callback);
    }

    public static void updatePollChoice(long poll_id, long poll_choice_id, String text, boolean is_correct, int position, CanvasCallback<PollChoiceResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_choice_id, text, is_correct)) { return; }

        buildInterface(PollChoiceInterface.class, callback).updatePollChoice(poll_id, poll_choice_id, text, is_correct, position, "", callback);
    }

    public static void deletePollChoice(long poll_id, long poll_choice_id, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_choice_id)) { return; }

        buildInterface(PollChoiceInterface.class, callback).deletePollChoice(poll_id, poll_choice_id, callback);
    }
}

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

import com.instructure.canvasapi.model.PollResponse;
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


public class PollAPI extends BuildInterfaceAPI {

    interface PollInterface {
        @GET("/polls")
        void getFirstPagePollsList(Callback<PollResponse> callback);

        @GET("/{next}")
        void getNextPagePollsList(@Path(value = "next", encode = false) String nextURL, Callback<PollResponse> callback);

        @GET("/polls/{pollid}")
        void getSinglePoll(@Path("pollid") long poll_id, Callback<PollResponse> callback);

        @POST("/polls")
        void createPoll(@Query("polls[][question]") String pollTitle, @Body String body, Callback<PollResponse> callback);

        @PUT("/polls/{pollid}")
        void updatePoll(@Path("pollid") long poll_id, @Query("polls[][question]") String pollTitle, @Body String body, Callback<PollResponse> callback);

        @DELETE("/polls/{pollid}")
        void deletePoll(@Path("pollid") long poll_id, Callback<Response> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPagePoll(CanvasCallback<PollResponse> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(PollInterface.class, callback).getFirstPagePollsList(callback);
        buildInterface(PollInterface.class, callback).getFirstPagePollsList(callback);
    }

    public static void getNextPagePoll(String nextURL, CanvasCallback<PollResponse> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(PollInterface.class, callback, false).getNextPagePollsList(nextURL, callback);
        buildInterface(PollInterface.class, callback, false).getNextPagePollsList(nextURL, callback);
    }

    public static void getSinglePoll(long poll_id, CanvasCallback<PollResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id)) { return; }

        buildCacheInterface(PollInterface.class, callback).getSinglePoll(poll_id, callback);
        buildInterface(PollInterface.class, callback).getSinglePoll(poll_id, callback);
    }

    public static void createPoll(String title, CanvasCallback<PollResponse> callback) {
        if (APIHelpers.paramIsNull(callback, title)) { return; }

        buildInterface(PollInterface.class, callback).createPoll(title, "", callback);
    }

    public static void updatePoll(long poll_id, String title, CanvasCallback<PollResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, title)) { return; }

        buildInterface(PollInterface.class, callback).updatePoll(poll_id, title, "", callback);
    }

    public static void deletePoll(long poll_id, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id)) { return; }

        buildInterface(PollInterface.class, callback).deletePoll(poll_id, callback);
    }
}

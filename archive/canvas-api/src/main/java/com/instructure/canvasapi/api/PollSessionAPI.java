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

import com.instructure.canvasapi.model.PollSessionResponse;
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


public class PollSessionAPI extends BuildInterfaceAPI {

    interface PollSessionInterface {
        @GET("/polls/{pollid}/poll_sessions")
        void getFirstPagePollSessionsList(@Path("pollid") long poll_id, Callback<PollSessionResponse> callback);

        @GET("/{next}")
        void getNextPagePollSessionsList(@Path(value = "next", encode = false) String nextURL, Callback<PollSessionResponse> callback);

        @GET("/polls/{pollid}/poll_sessions/{poll_session_id}")
        void getSinglePollSession(@Path("pollid") long poll_id, @Path("poll_session_id") long poll_session_id, Callback<PollSessionResponse> callback);

        @POST("/polls/{pollid}/poll_sessions")
        void createPollSession(@Path("pollid") long poll_id, @Query("poll_sessions[][course_id]") long course_id, @Query("poll_sessions[][course_section_id]") long course_section_id, @Body String body, Callback<PollSessionResponse> callback);

        @PUT("/polls/{pollid}/poll_sessions/{poll_session_id}")
        void updatePollSession(@Path("pollid") long poll_id, @Path("poll_session_id") long poll_session_id,  @Query("poll_sessions[][course_id]") long course_id, @Query("poll_sessions[][course_section_id]") long course_section_id, @Query("poll_sessions[][has_public_results]") boolean has_public_results, @Body String body, Callback<PollSessionResponse> callback);

        @DELETE("/polls/{pollid}/poll_sessions/{poll_session_id}")
        void deletePollSession(@Path("pollid") long poll_id, @Path("poll_session_id") long poll_session_id, Callback<Response> callback);

        @GET("/polls/{pollid}/poll_sessions/{poll_session_id}/open")
        void openPollSession(@Path("pollid") long poll_id, @Path("poll_session_id") long poll_session_id, Callback<Response> callback);

        @GET("/polls/{pollid}/poll_sessions/{poll_session_id}/close")
        void closePollSession(@Path("pollid") long poll_id, @Path("poll_session_id") long poll_session_id, Callback<Response> callback);

        @GET("/poll_sessions/opened")
        void getOpenSessions(Callback<PollSessionResponse> callback);

        @GET("/poll_sessions/closed")
        void getClosedSessions(Callback<PollSessionResponse> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPagePollSessions(long poll_id, CanvasCallback<PollSessionResponse> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(PollSessionInterface.class, callback).getFirstPagePollSessionsList(poll_id, callback);
        buildInterface(PollSessionInterface.class, callback).getFirstPagePollSessionsList(poll_id, callback);
    }

    public static void getNextPagePollSessions(String nextURL, CanvasCallback<PollSessionResponse> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(PollSessionInterface.class, callback, false).getNextPagePollSessionsList(nextURL, callback);
        buildInterface(PollSessionInterface.class, callback, false).getNextPagePollSessionsList(nextURL, callback);
    }

    public static void getSinglePollSession(long poll_id, long poll_session_id, CanvasCallback<PollSessionResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_session_id)) { return; }

        buildCacheInterface(PollSessionInterface.class, callback).getSinglePollSession(poll_id, poll_session_id, callback);
        buildInterface(PollSessionInterface.class, callback).getSinglePollSession(poll_id, poll_session_id, callback);
    }

    public static void createPollSession(long poll_id, long course_id, long course_section_id, CanvasCallback<PollSessionResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, course_id, course_section_id)) { return; }

        buildInterface(PollSessionInterface.class, callback).createPollSession(poll_id, course_id, course_section_id, "", callback);
    }

    public static void updatePollSession(long poll_id, long poll_session_id, long course_id, long course_section_id, boolean has_public_results, CanvasCallback<PollSessionResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_session_id, course_id, course_section_id, has_public_results)) { return; }

        buildInterface(PollSessionInterface.class, callback).updatePollSession(poll_id, poll_session_id, course_id, course_section_id, has_public_results, "", callback);
    }

    public static void deletePollSession(long poll_id, long poll_session_id, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_session_id)) { return; }

        buildInterface(PollSessionInterface.class, callback).deletePollSession(poll_id, poll_session_id, callback);
    }

    public static void openPollSession(long poll_id, long poll_session_id, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_session_id)) { return; }

        buildInterface(PollSessionInterface.class, callback).openPollSession(poll_id, poll_session_id, callback);
    }

    public static void closePollSession(long poll_id, long poll_session_id, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_session_id)) { return; }

        buildInterface(PollSessionInterface.class, callback).closePollSession(poll_id, poll_session_id, callback);
    }

    public static void getOpenSessions(CanvasCallback<PollSessionResponse> callback) {
        buildInterface(PollSessionInterface.class, callback).getOpenSessions(callback);
    }

    public static void getClosedSessions(CanvasCallback<PollSessionResponse> callback) {
        buildInterface(PollSessionInterface.class, callback).getClosedSessions(callback);
    }
}

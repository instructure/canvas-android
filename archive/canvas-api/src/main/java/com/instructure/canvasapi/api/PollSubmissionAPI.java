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

import com.instructure.canvasapi.model.PollSubmissionResponse;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public class PollSubmissionAPI extends BuildInterfaceAPI {

    interface PollSubmissionInterface {
        @GET("/polls/{pollid}/poll_sessions/{poll_session_id}/poll_submissions/{poll_submission_id}")
        void getPollSubmission(@Path("pollid") long poll_id, @Path("poll_session_id") long poll_session_id, @Path("poll_submission_id") long poll_submission_id, Callback<PollSubmissionResponse> callback);

        @POST("/polls/{pollid}/poll_sessions/{poll_session_id}/poll_submissions/")
        void createPollSubmission(@Path("pollid") long poll_id, @Path("poll_session_id") long poll_session_id, @Query("poll_submissions[][poll_choice_id]") long poll_choice_id, @Body String body, Callback<PollSubmissionResponse> callback);

    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getPollSubmission(long poll_id, long poll_session_id, long poll_submission_id, CanvasCallback<PollSubmissionResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_session_id, poll_submission_id)) { return; }

        buildCacheInterface(PollSubmissionInterface.class, callback).getPollSubmission(poll_id, poll_session_id, poll_submission_id, callback);
        buildInterface(PollSubmissionInterface.class, callback).getPollSubmission(poll_id, poll_session_id, poll_submission_id, callback);
    }

    public static void createPollSubmission(long poll_id, long poll_session_id, long poll_choice_id, CanvasCallback<PollSubmissionResponse> callback) {
        if (APIHelpers.paramIsNull(callback, poll_id, poll_session_id, poll_choice_id)) { return; }

        buildInterface(PollSubmissionInterface.class, callback).createPollSubmission(poll_id, poll_session_id, poll_choice_id, "", callback);
    }

}

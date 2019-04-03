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

import com.instructure.canvasapi.model.ErrorReportResult;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;


public class ErrorReportAPI {

    private static final String DEFAULT_DOMAIN = "https://canvas.instructure.com";
    public interface ErrorReportInterface {
        @POST("/error_reports.json")
        void postErrorReport(@Query("error[subject]") String subject, @Query("error[url]") String url, @Query("error[email]") String email, @Query("error[comments]") String comments, @Query("error[user_perceived_severity") String userPerceivedSeverity, @Body String body, CanvasCallback<ErrorReportResult> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // Build Interface Helpers
    /////////////////////////////////////////////////////////////////////////

    private static ErrorReportInterface buildInterface(CanvasCallback<?> callback) {
        //we don't want to use the normal buildAdapter method because the user might not always be logged in
        //when they use this method (like when they are on the login page) and the normal buildAdapter method prepends a
        // /api/v1 and requires a token.
        RestAdapter restAdapter = CanvasRestAdapter.buildTokenRestAdapter(callback.getContext());
        return restAdapter.create(ErrorReportInterface.class);
    }

    /**
     * Used when we don't want to use the user's domain
     * @param callback
     * @return
     */
    private static ErrorReportInterface buildGenericInterface(CanvasCallback<?> callback) {
        //we don't want to use the normal buildAdapter method because the user might not always be logged in
        //when they use this method (like when they are on the login page) and the normal buildAdapter method prepends a
        // /api/v1 and requires a token.
        RestAdapter restAdapter = CanvasRestAdapter.getGenericHostAdapter(DEFAULT_DOMAIN);
        return restAdapter.create(ErrorReportInterface.class);
    }

    public static void postErrorReport(String subject, String url, String email, String comments, String userPerceivedSeverity, CanvasCallback<ErrorReportResult> callback) {
        if(APIHelpers.paramIsNull(callback, subject, url, email, comments, userPerceivedSeverity)) return;

        buildInterface(callback).postErrorReport(subject, url, email, comments, userPerceivedSeverity, "", callback);
    }

    public static void postGenericErrorReport(String subject, String url, String email, String comments, String userPerceivedSeverity, CanvasCallback<ErrorReportResult> callback) {
        if(APIHelpers.paramIsNull(callback, subject, url, email, comments, userPerceivedSeverity)) return;

        buildGenericInterface(callback).postErrorReport(subject, url, email, comments, userPerceivedSeverity, "", callback);
    }
}

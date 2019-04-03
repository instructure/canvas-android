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

import com.instructure.canvasapi.model.OAuthToken;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.EncodedQuery;
import retrofit.http.POST;
import retrofit.http.Query;


public class OAuthAPI {


    interface OAuthInterface {
        @DELETE("/login/oauth2/token")
        void deleteToken(Callback<Response> callback);

        @POST("/login/oauth2/token")
        void getToken(@Query("client_id") String clientId, @Query("client_secret") String clientSecret, @Query("code") String oAuthRequest, @Query(value = "redirect_uri", encodeValue = true) String redirectURI, @Body String body, CanvasCallback<OAuthToken>canvasCallback);

    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void deleteToken(CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        RestAdapter restAdapter = CanvasRestAdapter.buildTokenRestAdapter(callback.getContext());
        OAuthInterface oAuthInterface = restAdapter.create(OAuthInterface.class);
        oAuthInterface.deleteToken(callback);
    }

    public static void deleteToken(String token, String protocol, String domain, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        RestAdapter restAdapter = CanvasRestAdapter.buildTokenRestAdapter(token, protocol, domain);
        OAuthInterface oAuthInterface = restAdapter.create(OAuthInterface.class);
        oAuthInterface.deleteToken(callback);
    }

    public static void getToken(String clientId, String clientSecret, String oAuthRequest, CanvasCallback<OAuthToken> callback) {

        if (APIHelpers.paramIsNull(callback,clientId,clientSecret,oAuthRequest)) { return; }

        RestAdapter restAdapter = CanvasRestAdapter.buildTokenRestAdapter(callback.getContext());
        OAuthInterface oAuthInterface = restAdapter.create(OAuthInterface.class);
        oAuthInterface.getToken(clientId, clientSecret, oAuthRequest, "urn:ietf:wg:oauth:2.0:oob", "", callback);
    }
}

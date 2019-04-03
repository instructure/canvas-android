/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.api;

import android.content.Context;
import android.util.Log;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.RetrofitCounter;
import com.instructure.loginapi.login.model.DomainVerificationResult;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.EncodedQuery;
import retrofit.http.GET;
import retrofit.http.Query;

public class MobileVerifyAPI {

    private static RestAdapter getAuthenticationRestAdapter(final Context context){

        final String userAgent = APIHelpers.getUserAgent(context);

        RetrofitCounter.increment();

        return new RestAdapter.Builder()
                .setEndpoint("https://canvas.instructure.com/api/v1") // The base API endpoint.
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        if (!userAgent.equals("")) {
                            requestFacade.addHeader("User-Agent", APIHelpers.getUserAgent(context));
                        }
                    }
                })
                .build();
    }

    interface OAuthInterface {
        @GET("/mobile_verify.json")
        void mobileVerify(@EncodedQuery("domain") String domain, @Query("user_agent") String userAgent, CanvasCallback<DomainVerificationResult> callback);
    }

    public static void mobileVerify(String domain, CanvasCallback<DomainVerificationResult> callback) {
        if (APIHelpers.paramIsNull(callback, domain)) { return; }

        final String userAgent = APIHelpers.getUserAgent(callback.getContext());
        if(userAgent.equals("")){
            Log.d(APIHelpers.LOG_TAG, "User agent must be set for this API to work correctly!");
            return;
        }

        OAuthInterface oAuthInterface = getAuthenticationRestAdapter(callback.getContext()).create(OAuthInterface.class);
        oAuthInterface.mobileVerify(domain,userAgent, callback);
    }
}

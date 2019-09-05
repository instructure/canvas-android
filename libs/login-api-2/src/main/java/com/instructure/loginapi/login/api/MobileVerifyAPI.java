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
package com.instructure.loginapi.login.api;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.loginapi.login.model.DomainVerificationResult;
import com.instructure.canvasapi2.utils.CanvasAuthenticator;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MobileVerifyAPI {

    private static Retrofit getAuthenticationRetrofit() {

        final String userAgent = ApiPrefs.getUserAgent();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        if (!userAgent.equals("")) {
                            Request request = chain.request().newBuilder()
                                    .header("User-Agent", userAgent)
                                    .cacheControl(CacheControl.FORCE_NETWORK)
                                    .build();
                            return chain.proceed(request);
                        } else {
                            return chain.proceed(chain.request());
                        }
                    }
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://canvas.instructure.com/api/v1/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    interface OAuthInterface {
        @GET("mobile_verify.json")
        Call<DomainVerificationResult> mobileVerify (@Query(value = "domain", encoded = false) String domain, @Query("user_agent") String userAgent);
    }

    public static void mobileVerify(String domain, StatusCallback<DomainVerificationResult> callback) {
        if (APIHelper.INSTANCE.paramIsNull(callback, domain)) {
            return;
        }

        final String userAgent = ApiPrefs.getUserAgent();
        if (userAgent.equals("")) {
            Logger.d("User agent must be set for this API to work correctly!");
            return;
        }

        OAuthInterface oAuthInterface = getAuthenticationRetrofit().create(OAuthInterface.class);
        oAuthInterface.mobileVerify(domain, userAgent).enqueue(callback);
    }
}

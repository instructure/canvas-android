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

package com.instructure.loginapi.login.api.zendesk.utilities;

import android.content.Context;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.instructure.canvasapi.utilities.RetrofitCounter;
import com.instructure.loginapi.login.BuildConfig;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class ZendeskRestAdapter {

    public static final String apiDomain = BuildConfig.ZENDESK_DOMAIN;

    /**
     * Returns a RestAdapter Instance that points at :domain/api/v2
     *
     * @param  callback A Canvas Callback
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(ZendeskCallback callback) {
        return buildAdapter(callback.getContext());
    }

    /**
     * Returns a RestAdapter Instance
     *
     * @param  context An Android context.
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(final Context context) {

        if(context == null ){
            return null;
        }

        RetrofitCounter.increment();

        GsonConverter gsonConverter = new GsonConverter(getGSONParser());

        //Sets the auth token, user agent, and handles masquerading.
        return new RestAdapter.Builder()
                .setEndpoint(apiDomain +"/api/v2/") // The base API endpoint.
                .setRequestInterceptor(new ZendeskRequestInterceptor())
                .setConverter(gsonConverter)
                .build();
    }

    /**
     * Gets our custom GSON parser.
     *
     * @return Our custom GSON parser with custom deserializers.
     */
    public static Gson getGSONParser(){
        GsonBuilder b = new GsonBuilder();
        return b.create();
    }

    /**
     * Class that's used as to inject the user agent, token, and handles masquerading.
     */
    public static class ZendeskRequestInterceptor implements RequestInterceptor {

        private String encodeCredentialsForBasicAuthorization() {
            return "Basic " + Base64.encodeToString(BuildConfig.ZENDESK_CREDENTIALS.getBytes(), Base64.NO_WRAP);
        }

        @Override
        public void intercept(RequestFacade requestFacade) {
            //Authenticate with token
            requestFacade.addHeader("Authorization", encodeCredentialsForBasicAuthorization());
            requestFacade.addHeader("Content-Type", "application/json");
        }
    }
}

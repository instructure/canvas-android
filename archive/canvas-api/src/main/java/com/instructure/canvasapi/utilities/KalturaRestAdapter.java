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

package com.instructure.canvasapi.utilities;

import android.content.Context;
import android.util.Log;


import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import retrofit.RestAdapter;

public class KalturaRestAdapter {
    /**
     * Returns a RestAdapter Instance that points at :domain/api_v3
     *
     * @param  callback A Canvas Callback
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(CanvasCallback callback) {
        callback.setFinished(false);
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

        String domain = APIHelpers.getFullKalturaDomain(context);

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (domain == null || domain.equals("")) {
            Log.d(APIHelpers.LOG_TAG, "The KalturaRestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new RestAdapter.Builder().setEndpoint("http://invalid.domain.com").build();
        }


        //Sets the auth token, user agent, and handles masquerading.
        return new RestAdapter.Builder()
                .setEndpoint(domain + "/api_v3/") // The base API endpoint.
                .setConverter(new SimpleXmlConverter())
                .build();


    }


    /**
     * Sets up the KalturaRestAdapter.
     *
     * Short hand for setdomain and setToken.
     *
     * Clears out any old data before setting the new data.
     *
     * @param context An Android context.
     * @param ks_token An kaltura Token
     * @param domain The domain for the signed in user.
     *
     * @return Whether or not the instance was setup. Only returns false if the data is empty or invalid.
     */
    public static boolean setupInstance(Context context, String ks_token, String domain){
        if (ks_token == null ||
                ks_token.equals("") ||
                domain == null) {

            return false;
        }

        String protocol = "https";
        if(domain.startsWith("http://")) {
            protocol = "http";
        }
        boolean kalturaDomainSet = APIHelpers.setKalturaDomain(context, domain);
        boolean tokenSet = APIHelpers.setKalturaToken(context, ks_token);
        boolean protocolSet = APIHelpers.setProtocol(protocol, context);
        return (kalturaDomainSet && tokenSet && protocolSet);
    }
}

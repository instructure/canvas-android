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

import com.instructure.canvasapi.model.Recipient;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import retrofit.Callback;
import retrofit.http.EncodedQuery;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;


public class RecipientAPI extends BuildInterfaceAPI {

    interface RecipientsInterface {
        @GET("/search/recipients?synthetic_contexts=1")
        void getFirstPageRecipientsList(@Query("search") String searchTerm, @EncodedQuery("context")String context, Callback<Recipient[]> callback);

        @GET("/search/recipients?synthetic_contexts=1")
        void getFirstPageRecipientsListNoContext(@Query("search") String searchTerm, Callback<Recipient[]> callback);

        @GET("/{next}")
        void getNextPageRecipientsList(@Path(value = "next", encode = false) String nextURL, Callback<Recipient[]> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPageRecipients(String search, String context, CanvasCallback<Recipient[]> callback) {
        if (APIHelpers.paramIsNull(callback, search, context)) { return; }

        if(context.trim().equals("")){
            buildCacheInterface(RecipientsInterface.class, callback).getFirstPageRecipientsListNoContext(search,callback);
            buildInterface(RecipientsInterface.class, callback).getFirstPageRecipientsListNoContext(search,callback);
        } else {
            buildCacheInterface(RecipientsInterface.class, callback).getFirstPageRecipientsList(search,context,callback);
            buildInterface(RecipientsInterface.class, callback).getFirstPageRecipientsList(search,context,callback);
        }
    }

    public static void getNextPageRecipients(String nextURL, CanvasCallback<Recipient[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(RecipientsInterface.class, callback, false).getNextPageRecipientsList(nextURL,callback);
        buildInterface(RecipientsInterface.class, callback, false).getNextPageRecipientsList(nextURL,callback);
    }
}

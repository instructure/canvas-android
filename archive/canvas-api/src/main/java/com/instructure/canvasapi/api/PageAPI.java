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

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Page;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import retrofit.Callback;
import retrofit.http.Path;
import retrofit.http.GET;


public class PageAPI extends BuildInterfaceAPI {

    interface PagesInterface {
        @GET("/{context_id}/pages?sort=title&order=asc")
        void getFirstPagePagesList(@Path("context_id") long context_id, Callback<Page[]> callback);

        @GET("/{next}")
        void getNextPagePagesList(@Path(value = "next", encode = false) String nextURL, Callback<Page[]> callback);

        @GET("/{context_id}/pages/{pageid}")
        void getDetailedPage(@Path("context_id") long context_id, @Path("pageid") String page_id, Callback<Page> callback);

        @GET("/{context_id}/front_page")
        void getFrontPage(@Path("context_id") long context_id, Callback<Page> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPagePages(CanvasContext canvasContext, CanvasCallback<Page[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(PagesInterface.class, callback, canvasContext).getFirstPagePagesList(canvasContext.getId(), callback);
        buildInterface(PagesInterface.class, callback, canvasContext).getFirstPagePagesList(canvasContext.getId(), callback);
    }

    public static void getNextPagePages(String nextURL, CanvasCallback<Page[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(PagesInterface.class, callback, false).getNextPagePagesList(nextURL, callback);
        buildInterface(PagesInterface.class, callback, false).getNextPagePagesList(nextURL, callback);
    }

    public static void getDetailedPage(CanvasContext canvasContext, String page_id, CanvasCallback<Page> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(PagesInterface.class, callback, canvasContext).getDetailedPage(canvasContext.getId(), page_id, callback);
        buildInterface(PagesInterface.class, callback, canvasContext).getDetailedPage(canvasContext.getId(), page_id, callback);
    }

    public static void getFrontPage(CanvasContext canvasContext, CanvasCallback<Page> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(PagesInterface.class, callback, canvasContext).getFrontPage(canvasContext.getId(), callback);
        buildInterface(PagesInterface.class, callback, canvasContext).getFrontPage(canvasContext.getId(), callback);
    }
}

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

import android.content.Context;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.HiddenStreamItem;
import com.instructure.canvasapi.model.StreamItem;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.DELETE;
import retrofit.http.Path;
import retrofit.http.GET;
import retrofit.http.Query;


public class StreamAPI extends BuildInterfaceAPI {

    interface StreamInterface {
        @GET("/users/self/activity_stream")
        void getUserStream(Callback<StreamItem[]> callback);

        @DELETE("/users/self/activity_stream/{streamID}")
        void hideStreamItem(@Path("streamID")long streamID, Callback<HiddenStreamItem> callback);

        @GET("/{context_id}/activity_stream")
        void getContextStream(@Path("context_id") long context_id, Callback<StreamItem[]> callback);

        @GET("/{next}")
        void getNextPageStream(@Path(value = "next", encode = false) String nextURL, Callback<StreamItem[]> callback);

        /////////////////////////////////////////////////////////////////////////////
        // Synchronous
        /////////////////////////////////////////////////////////////////////////////

        @GET("/users/self/activity_stream")
        StreamItem[] getUserStreamSynchronous(@Query("per_page") int number);
    }


    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPageUserStream(CanvasCallback<StreamItem[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(StreamInterface.class, callback, null).getUserStream(callback);
        buildInterface(StreamInterface.class, callback, null).getUserStream(callback);
    }

    public static void getFirstPageCourseStream(CanvasContext canvasContext, CanvasCallback<StreamItem[]> callback) {
        if (APIHelpers.paramIsNull(callback ,canvasContext)) { return; }

        buildCacheInterface(StreamInterface.class, callback, canvasContext).getContextStream(canvasContext.getId(), callback);
        buildInterface(StreamInterface.class, callback, canvasContext).getContextStream(canvasContext.getId(), callback);
    }

    public static void getNextPageStream(String nextUrl, CanvasCallback<StreamItem[]> callback) {
        if (APIHelpers.paramIsNull(callback, nextUrl)) { return; }

        callback.setIsNextPage(true);

        buildCacheInterface(StreamInterface.class, callback, false).getNextPageStream(nextUrl, callback);
        buildInterface(StreamInterface.class, callback, false).getNextPageStream(nextUrl, callback);
    }

    public static void hideStreamItem(long streamId, final CanvasCallback<HiddenStreamItem> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }
        buildInterface(StreamInterface.class, callback, null).hideStreamItem(streamId,callback);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Synchronous
    //
    // If Retrofit is unable to parse (no network for example) Synchronous calls
    // will throw a nullPointer exception. All synchronous calls need to be in a
    // try catch block.
    /////////////////////////////////////////////////////////////////////////////

    public static StreamItem[] getUserStreamSynchronous(Context context, int numberToReturn) {
        try {
            RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context);

            return restAdapter.create(StreamInterface.class).getUserStreamSynchronous(numberToReturn);
        } catch (Exception E){
            return null;
        }
    }
}

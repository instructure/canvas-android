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
import com.instructure.canvasapi.model.DiscussionTopicHeader;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Path;
import retrofit.http.GET;

public class AnnouncementAPI extends BuildInterfaceAPI {

    interface AnnouncementsInterface {
        @GET("/{context_id}/discussion_topics?only_announcements=1")
        void getFirstPageAnnouncementsList(@Path("context_id") long context_id, Callback<DiscussionTopicHeader[]> callback);

        @GET("/{next}")
        void getNextPageAnnouncementsList(@Path(value = "next", encode = false) String nextURL, Callback<DiscussionTopicHeader[]> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPageAnnouncements(CanvasContext canvasContext, CanvasCallback<DiscussionTopicHeader[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(AnnouncementsInterface.class, callback, canvasContext).getFirstPageAnnouncementsList(canvasContext.getId(), callback);
        buildInterface(AnnouncementsInterface.class, callback, canvasContext).getFirstPageAnnouncementsList(canvasContext.getId(), callback);
    }

    public static void getNextPageAnnouncements(String nextURL, CanvasCallback<DiscussionTopicHeader[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(AnnouncementsInterface.class, callback, false).getNextPageAnnouncementsList(nextURL, callback);
        buildInterface(AnnouncementsInterface.class, callback, false).getNextPageAnnouncementsList(nextURL, callback);
    }
}

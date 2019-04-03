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

package com.instructure.canvasapi.api.compatibility_synchronous;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

public class VimeoSynchronousAPI {
    /**
     * getVimeoThumbnail is a method used to get a thumbnail url for a given vimeo video id.
     * @param vimeoId
     * @param context
     * @return
     */
    public static String getVimeoThumbnail(String vimeoId, Context context) {
        try {
            String url = "http://vimeo.com/api/v2/video/" + vimeoId + ".json";

            String response = HttpHelpers.externalHttpGet(context, url).responseBody;
            JSONArray a = new JSONArray(response);
            JSONObject json = a.getJSONObject(0);
            if (json.has("thumbnail_small"))
                return json.getString("thumbnail_small");
            else
                return json.getString("thumbnail");
        } catch (Exception E) {
            return "default_video_poster.png";
        }
    }
}

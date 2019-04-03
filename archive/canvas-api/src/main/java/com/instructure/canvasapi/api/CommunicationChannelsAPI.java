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
import com.instructure.canvasapi.model.CommunicationChannel;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;

public class CommunicationChannelsAPI extends BuildInterfaceAPI {

    public interface CommunicationChannelInterface {

        @GET("/users/{user_id}/communication_channels")
        void getCommunicationChannels(@Path("user_id") long userId, CanvasCallback<CommunicationChannel[]> callback);
    }
    public static void getCommunicationChannels(final long userId, final CanvasCallback<CommunicationChannel[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(CommunicationChannelInterface.class, callback, null).getCommunicationChannels(userId, callback);
        buildInterface(CommunicationChannelInterface.class, callback, null).getCommunicationChannels(userId, callback);
    }
}

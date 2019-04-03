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

import com.instructure.canvasapi.utilities.APIHelpers;

import retrofit.Callback;
import retrofit.Profiler;
import retrofit.client.Response;
import retrofit.http.GET;


public class PingAPI extends BuildInterfaceAPI {

    interface PingInterface {
        @GET("/ping")
        void getPing(Callback<Response> callback);
    }

    public static void getPing(String url, Profiler profiler, Callback<Response> callback) {
        if (APIHelpers.paramIsNull(profiler)) { return; }

        buildPingInterface(PingInterface.class, url, profiler).getPing(callback);
    }
}

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

import com.instructure.canvasapi.model.LaunchDefinition;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import java.util.List;

import retrofit.http.GET;

import static com.instructure.canvasapi.api.BuildInterfaceAPI.buildCacheInterface;
import static com.instructure.canvasapi.api.BuildInterfaceAPI.buildInterface;

public class LaunchDefinitionsAPI {

    interface LaunchDefinitionsInterface {
        @GET("/accounts/self/lti_apps/launch_definitions?placements[]=global_navigation")
        void getLaunchDefinitions(CanvasCallback<List<LaunchDefinition>> callback);
    }

    public static void getLaunchDefinitions(final CanvasCallback<List<LaunchDefinition>> callback) {
        if (APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(LaunchDefinitionsInterface.class, callback).getLaunchDefinitions(callback);
        buildInterface(LaunchDefinitionsInterface.class, callback).getLaunchDefinitions(callback);
    }
}

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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

object LaunchDefinitionsAPI {

    interface LaunchDefinitionsInterface {
        @GET("accounts/self/lti_apps/launch_definitions?placements[]=global_navigation")
        fun getLaunchDefinitions(): Call<List<LaunchDefinition>?>

        @GET("accounts/self/lti_apps/launch_definitions?placements[]=global_navigation")
        suspend fun getLaunchDefinitions(@Tag params: RestParams): DataResult<List<LaunchDefinition>?>

        @GET("accounts/self/external_tools/sessionless_launch")
        suspend fun getLtiFromAuthenticationUrl(@Query("url") url: String, @Tag restParams: RestParams): DataResult<LTITool>

        @GET("courses/{courseId}/lti_apps/launch_definitions?placements[]=course_navigation")
        fun getLaunchDefinitionsForCourse(@Path("courseId") courseId: Long): Call<List<LaunchDefinition>>
    }

    fun getLaunchDefinitions(adapter: RestBuilder, callback: StatusCallback<List<LaunchDefinition>?>, params: RestParams) {
        callback.addCall(adapter.build(LaunchDefinitionsInterface::class.java, params).getLaunchDefinitions()).enqueue(callback)
    }

    fun getLaunchDefinitionsForCourse(courseId: Long, adapter: RestBuilder, callback: StatusCallback<List<LaunchDefinition>>, params: RestParams) {
        callback.addCall(adapter.build(LaunchDefinitionsInterface::class.java, params).getLaunchDefinitionsForCourse(courseId)).enqueue(callback)
    }
}

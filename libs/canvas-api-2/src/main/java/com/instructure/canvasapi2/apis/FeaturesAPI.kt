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
import com.instructure.canvasapi2.models.EnvironmentSettings
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Tag

object FeaturesAPI {

    interface FeaturesInterface {
        @GET("courses/{courseId}/features/enabled")
        fun getEnabledFeaturesForCourse(@Path("courseId") contextId: Long): Call<List<String>>

        @GET("courses/{courseId}/features/enabled")
        suspend fun getEnabledFeaturesForCourse(@Path("courseId") contextId: Long, @Tag params: RestParams): DataResult<List<String>>

        @GET("features/environment")
        fun getEnvironmentFeatureFlags(): Call<Map<String, Boolean>>

        @GET("features/environment")
        suspend fun getEnvironmentFeatureFlags(@Tag restParams: RestParams): DataResult<Map<String, Boolean>>

        @GET("settings/environment")
        suspend fun getAccountSettingsFeatures(@Tag restParams: RestParams): DataResult<EnvironmentSettings>
    }

    fun getEnabledFeaturesForCourse(adapter: RestBuilder, courseId: Long, callback: StatusCallback<List<String>>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) {
            return
        }

        callback.addCall(adapter.build(FeaturesInterface::class.java, params).getEnabledFeaturesForCourse(courseId)).enqueue(callback)
    }

    fun getEnvironmentFeatureFlags(adapter: RestBuilder, callback: StatusCallback<Map<String, Boolean>>, params: RestParams) {
        callback.addCall(adapter.build(FeaturesInterface::class.java, params).getEnvironmentFeatureFlags()).enqueue(callback)
    }
}

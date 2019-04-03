//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package api.bitrise.private

import api.bitrise.private.CookieRetrofit.privateCookieRetrofit
import retrofit2.Call
import retrofit2.http.*

object RollingBuilds {
    interface ApiService {
        @GET("app/{appSlug}/rolling_builds_toggle")
        fun getConfig(@Path("appSlug") appSlug: String): Call<RollingBuildsConfig>

        @PATCH("app/{appSlug}/rolling_builds_toggle")
        fun setConfig(@Path("appSlug") appSlug: String,
                      @Body body: RollingBuildsPatch): Call<RollingBuildsConfig>

        @DELETE("app/{appSlug}/addon/addons-rolling-build")
        fun disable(@Path("appSlug") appSlug: String): Call<Status>

        @POST("app/{appSlug}/addon/addons-rolling-build")
        fun enable(@Path("appSlug") appSlug: String): Call<Status>
    }

    private val apiService: ApiService by lazy {
        privateCookieRetrofit.create(ApiService::class.java)
    }

    fun getConfig(appSlug: String): RollingBuildsConfig {
        return apiService.getConfig(appSlug).execute().body() ?: throw RuntimeException("getConfig failed")
    }

    fun setConfigPR(appSlug: String, enabled: Boolean): RollingBuildsConfig {
        val config = RollingBuildsPatch("pr", enabled)
        return apiService.setConfig(appSlug, config).execute().body() ?: throw RuntimeException("setConfigPR failed")
    }

    fun setConfigPush(appSlug: String, enabled: Boolean): RollingBuildsConfig {
        val config = RollingBuildsPatch("push", enabled)
        return apiService.setConfig(appSlug, config).execute().body() ?: throw RuntimeException("setConfigPush failed")
    }

    fun setConfigRunning(appSlug: String, enabled: Boolean): RollingBuildsConfig {
        val config = RollingBuildsPatch("running", enabled)
        return apiService.setConfig(appSlug, config).execute().body() ?: throw RuntimeException("setConfigRunning failed")
    }

    fun disable(appSlug: String): Status {
        return apiService.disable(appSlug).execute().body() ?: Status(null, null)
    }

    fun enable(appSlug: String): Status {
        return apiService.enable(appSlug).execute().body() ?: Status(null, null)
    }
}

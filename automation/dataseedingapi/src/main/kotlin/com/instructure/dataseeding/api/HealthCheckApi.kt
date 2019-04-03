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


package com.instructure.dataseeding.api

import com.instructure.dataseeding.util.CanvasRestAdapter
import retrofit2.Call
import retrofit2.http.GET

object HealthCheckApi {

    // Health check returns 'canvas ok' not JSON.
    // https://mobileqa.test.instructure.com/health_check
    interface HealthCheckService {
        @GET("/health_check")
        fun healthCheck(): Call<String>
    }

    private val healthCheckService: HealthCheckService by lazy {
        CanvasRestAdapter.noAuthRetrofit.create(HealthCheckService::class.java)
    }

    fun healthCheck(): Boolean {
        var result = false

        for (i in 1..3) {
            val response = healthCheckService.healthCheck().execute().body() ?: ""
            result = response.startsWith("canvas ok")

            if (result) break
        }

        return result
    }
}

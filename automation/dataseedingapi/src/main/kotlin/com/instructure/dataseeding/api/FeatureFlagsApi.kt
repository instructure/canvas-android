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

import com.instructure.dataseeding.model.FeatureFlagApiModel
import com.instructure.dataseeding.util.CanvasRestAdapter
import retrofit2.Call
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

object FeatureFlagsApi {
    interface FeatureFlagsService {
        @PUT("accounts/self/features/flags/{feature}")
        fun setAccountFeatureFlag(@Path("feature") feature: String, @Query("state") state: String): Call<FeatureFlagApiModel>
    }

    private val accountFeatureFlagsService: FeatureFlagsService by lazy {
        CanvasRestAdapter.adminRetrofit.create(FeatureFlagsService::class.java)
    }

    fun setAccountFeatureFlag(feature: String, state: String): FeatureFlagApiModel {
        return accountFeatureFlagsService.setAccountFeatureFlag(feature, state).execute().body()!!
    }
}

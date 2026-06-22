/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */


package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.FeatureFlagApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

object FeatureFlagsApi {
    interface FeatureFlagsService {

        @PUT("accounts/{accountId}/features/flags/{feature}")
        fun setAccountFeatureFlag(
            @Path("accountId") accountId: String,
            @Path("feature") feature: String,
            @Query("state") state: String
        ): Call<FeatureFlagApiModel>
    }

    private val adminFeatureFlagsService: FeatureFlagsService by lazy {
        CanvasNetworkAdapter.adminRetrofit.create(FeatureFlagsService::class.java)
    }

    /**
     * Sets a feature flag on the given account. [state] is one of Canvas' feature flag states:
     * "off", "allowed", or "on". Defaults to the admin's root account ("self").
     */
    fun setAccountFeatureFlag(feature: String, state: String, accountId: String = "self"): FeatureFlagApiModel {
        return adminFeatureFlagsService
            .setAccountFeatureFlag(accountId, feature, state)
            .execute()
            .body()!!
    }
}

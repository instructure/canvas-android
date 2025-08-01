/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ExperienceSummary
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Tag

interface ExperienceAPI {

    @GET("career/experience_summary")
    suspend fun getExperienceSummary(@Tag params: RestParams): DataResult<ExperienceSummary>

    @POST("career/switch_experience")
    suspend fun switchExperience(@Tag params: RestParams, @Field("experience") experience: String): DataResult<ExperienceSummary>
}
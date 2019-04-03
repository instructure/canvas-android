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

import com.instructure.dataseeding.model.CreateSection
import com.instructure.dataseeding.model.CreateSectionWrapper
import com.instructure.dataseeding.model.SectionApiModel
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object SectionsApi {
    interface SectionsService {
        @POST("courses/{courseId}/sections")
        fun createSection(@Path("courseId") courseId: Long, @Body createSection: CreateSectionWrapper): Call<SectionApiModel>
    }

    private val sectionsService: SectionsService by lazy {
        CanvasRestAdapter.adminRetrofit.create(SectionsService::class.java)
    }

    fun createSection(courseId: Long): SectionApiModel {
        val section = CreateSectionWrapper(CreateSection(name = Randomizer.randomSectionName()))
        return sectionsService
                .createSection(courseId, section)
                .execute()
                .body()!!
    }
}

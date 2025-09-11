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
import com.instructure.dataseeding.model.EnrollmentApiModel
import com.instructure.dataseeding.model.SectionApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object SectionsApi {
    interface SectionsService {

        @GET("courses/{courseId}/sections")
        fun getSections(@Path("courseId") courseId: Long): Call<List<SectionApiModel>>

        @POST("courses/{courseId}/sections")
        fun createSection(@Path("courseId") courseId: Long, @Body createSection: CreateSectionWrapper): Call<SectionApiModel>

        @POST("sections/{sectionId}/enrollments")
        fun enrollUserToSection(@Path("sectionId") sectionId: Long, @Query("enrollment[user_id]") userId: Long, @Query("enrollment[type]") type: String, @Query("enrollment[enrollment_state]") enrollmentState: String): Call<EnrollmentApiModel>
    }

    private val sectionsService: SectionsService by lazy {
        CanvasNetworkAdapter.adminRetrofit.create(SectionsService::class.java)
    }

    fun getSections(courseId: Long): List<SectionApiModel> {
        return sectionsService
            .getSections(courseId = courseId)
            .execute()
            .body()!!
    }

    fun createSection(courseId: Long, sectionName: String = Randomizer.randomSectionName()): SectionApiModel {
        val section = CreateSectionWrapper(CreateSection(name = sectionName))
        return sectionsService
                .createSection(courseId, section)
                .execute()
                .body()!!
    }

    fun enrollUserToSection(sectionId: Long, userId: Long, enrollmentType: String = "StudentEnrollment", enrollmentState: String = "active"): EnrollmentApiModel {
        return sectionsService
            .enrollUserToSection(sectionId, userId, enrollmentType, enrollmentState)
            .execute()
            .body()!!
    }
}

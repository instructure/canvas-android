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

import com.instructure.dataseeding.model.CreateEnrollmentTerm
import com.instructure.dataseeding.model.CreateEnrollmentTermWrapper
import com.instructure.dataseeding.model.EnrollmentTermApiModel
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

object EnrollmentTermsApi {
    interface EnrollmentTermsService {

        @POST("accounts/self/terms")
        fun createEnrollmentTerm(@Body createEnrollmentTermWrapper: CreateEnrollmentTermWrapper): Call<EnrollmentTermApiModel>
    }

    private val adminEnrollmentTermsService: EnrollmentTermsService by lazy {
        CanvasRestAdapter.adminRetrofit.create(EnrollmentTermsService::class.java)
    }

    fun createEnrollmentTerm(): EnrollmentTermApiModel {
        val enrollmentTerm = CreateEnrollmentTermWrapper(CreateEnrollmentTerm(Randomizer.randomEnrollmentTitle()))
        return adminEnrollmentTermsService
                .createEnrollmentTerm(enrollmentTerm)
                .execute()
                .body()!!
    }
}

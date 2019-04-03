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

import com.instructure.dataseeding.model.*
import com.instructure.dataseeding.util.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

object GradingPeriodsApi {
    interface GradingPeriodsService {

        @POST("accounts/self/grading_period_sets")
        fun createGradingPeriodSet(@Body createGradingPeriod: CreateGradingPeriodSetWrapper): Call<GradingPeriodSetApiModelWrapper>

        @PATCH("grading_period_sets/{gradingPeriodSetId}/grading_periods/batch_update")
        fun createGradingPeriod(@Path("gradingPeriodSetId") gradingPeriodSetId: Long, @Body createGradingPeriod: CreateGradingPeriodWrapper): Call<GradingPeriods>
    }

    private val adminGradingPeriodsService: GradingPeriodsService by lazy {
        CanvasRestAdapter.adminRetrofit.create(GradingPeriodsService::class.java)
    }

    fun createGradingPeriodSet(enrollmentTermId: Long): GradingPeriodSetApiModelWrapper {
        val createGradingPeriodSet = CreateGradingPeriodSetWrapper(
                CreateGradingPeriodSet(Randomizer.randomGradingPeriodSetTitle()), listOf(enrollmentTermId))

        return adminGradingPeriodsService
                .createGradingPeriodSet(createGradingPeriodSet)
                .execute()
                .body()!!
    }

    fun createGradingPeriod(gradingPeriodSetId: Long): GradingPeriods {
        val createGradingPeriod = CreateGradingPeriod(
                Randomizer.randomGradingPeriodName(),
                1.week.ago.iso8601,
                1.week.fromNow.iso8601,
                1.week.fromNow.iso8601
        )

        val createGradingPeriodWrapper = CreateGradingPeriodWrapper(listOf(createGradingPeriod))

        return adminGradingPeriodsService
                .createGradingPeriod(gradingPeriodSetId, createGradingPeriodWrapper)
                .execute()
                .body()!!
    }
}

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


package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class CreateGradingPeriodSet(
        val title: String
)

data class GradingPeriodSetApiModel(
        val id: Long
)

data class GradingPeriodSetApiModelWrapper(
        @SerializedName("grading_period_set")
        val gradingPeriodSet: GradingPeriodSetApiModel
)

data class CreateGradingPeriodSetWrapper(
        @SerializedName("grading_period_set")
        val gradingPeriodSet: CreateGradingPeriodSet,
        @SerializedName("enrollment_term_ids")
        val enrollmentTermIds: List<Long>
)

data class CreateGradingPeriodWrapper(
        @SerializedName("grading_periods")
        val gradingPeriods: List<CreateGradingPeriod>
)

data class CreateGradingPeriod(
        val title: String,
        @SerializedName("start_date")
        val startDate: String,
        @SerializedName("end_date")
        val endDate: String,
        @SerializedName("close_date")
        val closeDate: String
)

data class GradingPeriodApiModel(
        val id: Long
)

data class GradingPeriods(
        @SerializedName("grading_periods")
        val gradingPeriods: List<GradingPeriodApiModel>
)

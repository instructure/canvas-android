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
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.*

object LatePolicyApi {
    interface LatePolicyService {

        @POST("courses/{courseId}/late_policy")
        fun createLatePolicy(
                @Path("courseId") courseId: Long,
                @Body latePolicy: LatePolicyWrapper): Call<LatePolicyWrapper>
    }

    private fun latePolicyService(token: String): LatePolicyService = CanvasNetworkAdapter.retrofitWithToken(token).create(LatePolicyService::class.java)

    fun createLatePolicy(courseId: Long,
                         latePolicy: LatePolicy,
                         teacherToken: String): LatePolicyWrapper {

        val wrapper = LatePolicyWrapper(
                LatePolicy(
                        latePolicy.missingSubmissionDeductionEnabled,
                        latePolicy.missingSubmissionDeduction,
                        latePolicy.lateSubmissionDeductionEnabled,
                        latePolicy.lateSubmissionDeduction,
                        latePolicy.lateSubmissionInterval,
                        latePolicy.lateSubmissionMinimumPercentEnabled,
                        latePolicy.lateSubmissionMinimumPercent
                )
        )
        return latePolicyService(teacherToken)
                .createLatePolicy(courseId, wrapper)
                .execute()
                .body()!!
    }
}

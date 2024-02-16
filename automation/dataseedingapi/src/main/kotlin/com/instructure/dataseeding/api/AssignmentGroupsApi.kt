//
// Copyright (C) 2022-present Instructure, Inc.
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

import com.instructure.dataseeding.model.AssignmentGroupApiModel
import com.instructure.dataseeding.model.CreateAssignmentGroup
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object AssignmentGroupsApi {
    interface AssignmentGroupsService {
        @POST("courses/{courseId}/assignment_groups")
        fun createAssignmentGroup(@Path("courseId") courseId: Long, @Body createAssignmentGroup: CreateAssignmentGroup): Call<AssignmentGroupApiModel>
    }

    private fun assignmentGroupsService(token: String): AssignmentGroupsService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(AssignmentGroupsService::class.java)

    fun createAssignmentGroup(token: String, courseId: Long, name: String, position: Int? = null, groupWeight: Int? = null, sisSourceId: Long? = null): AssignmentGroupApiModel {
        val assignmentGroup = CreateAssignmentGroup(name, position, groupWeight, sisSourceId)
        return assignmentGroupsService(token)
                .createAssignmentGroup(courseId, assignmentGroup)
                .execute()
                .body()!!
    }
}

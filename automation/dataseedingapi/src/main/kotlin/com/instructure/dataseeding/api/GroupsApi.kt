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
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * This API is not using the `object` singleton pattern because that causes
 * the JUnit tests to fail when running the test suite.
 *
 * See test/java/com/instructure/dataseeding/soseedy/GroupsTest
 */
object GroupsApi {
    interface GroupsService {
        @POST("courses/{courseId}/group_categories")
        fun createCourseGroupCategory(@Path("courseId") courseId: Long, @Body createGroupCategory: CreateGroupCategory): Call<GroupCategoryApiModel>

        @POST("group_categories/{groupCategory}/groups")
        fun createGroup(@Path("groupCategory") groupCategoryId: Long, @Body createGroup: CreateGroup): Call<GroupApiModel>

        @POST("groups/{groupId}/memberships")
        fun createGroupMembership(@Path("groupId") groupId: Long, @Body createGroupMembership: CreateGroupMembership): Call<GroupMembershipApiModel>
    }

    private fun groupsService(token: String): GroupsService
            = CanvasRestAdapter.retrofitWithToken(token).create(GroupsService::class.java)

    fun createCourseGroupCategory(courseId: Long, teacherToken: String): GroupCategoryApiModel {
        val groupCategory = CreateGroupCategory(Randomizer.randomCourseGroupCategoryName())

        return groupsService(teacherToken)
                .createCourseGroupCategory(courseId, groupCategory)
                .execute()
                .body()!!
    }

    fun createGroup(groupCategoryId: Long, teacherToken: String): GroupApiModel
            = groupsService(teacherToken)
            .createGroup(groupCategoryId, Randomizer.randomGroup())
            .execute()
            .body()!!

    fun createGroupMembership(groupId: Long, userId: Long, teacherToken: String): GroupMembershipApiModel {
        val groupMembership = CreateGroupMembership(userId)

        return groupsService(teacherToken)
                .createGroupMembership(groupId, groupMembership)
                .execute()
                .body()!!
    }
}

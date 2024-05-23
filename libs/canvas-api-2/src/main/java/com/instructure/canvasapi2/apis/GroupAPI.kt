/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.apis


import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url
import java.io.IOException

object GroupAPI {

    interface GroupInterface {

        @GET("users/self/favorites/groups")
        fun getFirstPageFavoriteGroups(): Call<List<Group>>

        @GET("users/self/groups?include[]=favorites&include[]=can_access")
        fun getFirstPageGroups(): Call<List<Group>>

        @GET("users/self/groups?include[]=favorites&include[]=can_access")
        suspend fun getFirstPageGroups(@Tag params: RestParams): DataResult<List<Group>>

        @GET
        fun getNextPageGroups(@Url nextUrl: String): Call<List<Group>>

        @GET
        suspend fun getNextPageGroups(@Url nextUrl: String, @Tag params: RestParams): DataResult<List<Group>>

        @GET("groups/{groupId}?include[]=permissions&include[]=favorites")
        fun getDetailedGroup(@Path("groupId") groupId: Long): Call<Group>

        @GET("groups/{groupId}?include[]=permissions&include[]=favorites")
        suspend fun getDetailedGroup(@Path("groupId") groupId: Long, @Tag params: RestParams): DataResult<Group>

        @POST("users/self/favorites/groups/{groupId}")
        fun addGroupToFavorites(@Path("groupId") groupId: Long): Call<Favorite>

        @DELETE("users/self/favorites/groups/{groupId}")
        fun removeGroupFromFavorites(@Path("groupId") groupId: Long): Call<Favorite>

        @GET("users/self/groups")
        fun getGroupsSynchronous(@Query("page") page: Int): Call<List<Group>>

        @GET("courses/{courseId}/groups")
        fun getFirstPageCourseGroups(@Path("courseId") courseId: Long): Call<List<Group>>

        @GET("groups/{groupId}/permissions")
        fun getGroupPermissions(@Path("groupId") groupId: Long, @Query("permissions[]") requestedPermissions: List<String>): Call<CanvasContextPermission>

        @GET("groups/{groupId}/permissions")
        suspend fun getGroupPermissions(@Path("groupId") groupId: Long, @Query("permissions[]") requestedPermissions: List<String>, @Tag params: RestParams): DataResult<CanvasContextPermission>
    }

    fun getFirstPageGroups(adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).getFirstPageGroups()).enqueue(callback)
    }

    fun getFavoriteGroups(adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).getFirstPageFavoriteGroups()).enqueue(callback)
    }

    @Throws(IOException::class)
    fun getFavoriteGroupsSynchronously(adapter: RestBuilder, params: RestParams): Response<List<Group>> {
        return adapter.build(GroupInterface::class.java, params).getFirstPageFavoriteGroups().execute()
    }

    @Throws(IOException::class)
    fun getGroupsSynchronously(adapter: RestBuilder, params: RestParams): Response<List<Group>> {
        return adapter.build(GroupInterface::class.java, params).getGroupsSynchronous(1).execute()
    }

    @Throws(IOException::class)
    fun getNextPageGroupsSynchronously(url: String, adapter: RestBuilder, params: RestParams): Response<List<Group>> {
        return adapter.build(GroupInterface::class.java, params).getNextPageGroups(url).execute()
    }

    fun getNextPageGroups(nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).getNextPageGroups(nextUrl)).enqueue(callback)
    }

    fun getDetailedGroup(adapter: RestBuilder, callback: StatusCallback<Group>, params: RestParams, groupId: Long) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).getDetailedGroup(groupId)).enqueue(callback)
    }

    fun addGroupToFavorites(adapter: RestBuilder, callback: StatusCallback<Favorite>, params: RestParams, groupId: Long) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).addGroupToFavorites(groupId)).enqueue(callback)
    }

    fun removeGroupFromFavorites(adapter: RestBuilder, callback: StatusCallback<Favorite>, params: RestParams, groupId: Long) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).removeGroupFromFavorites(groupId)).enqueue(callback)
    }

    fun getGroupsForCourse(adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams, courseId: Long) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).getFirstPageCourseGroups(courseId)).enqueue(callback)
    }

    fun getGroupPermissions(groupId: Long, requestedPermissions: List<String>, adapter: RestBuilder, callback: StatusCallback<CanvasContextPermission>, params: RestParams) {
        callback.addCall(adapter.build(GroupInterface::class.java, params).getGroupPermissions(groupId, requestedPermissions)).enqueue(callback)
    }
}

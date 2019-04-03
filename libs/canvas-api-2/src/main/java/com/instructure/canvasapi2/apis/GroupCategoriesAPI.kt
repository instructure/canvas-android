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
import com.instructure.canvasapi2.models.Group

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

object GroupCategoriesAPI {

    internal interface GroupCategoriesInterface {

        @GET("group_categories/{groupCategoryId}/groups")
        fun getFirstPageGroupsFromCategory(@Path("groupCategoryId") groupCategoryId: Long): Call<List<Group>>

        @GET
        fun getNextPageGroups(@Url nextUrl: String): Call<List<Group>>
    }

    fun getFirstPageGroupsInCategory(categoryId: Long, adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams) {
        callback.addCall(adapter.build(GroupCategoriesInterface::class.java, params).getFirstPageGroupsFromCategory(categoryId)).enqueue(callback)
    }

    fun getNextPageGroups(nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams) {
        callback.addCall(adapter.build(GroupCategoriesInterface::class.java, params).getNextPageGroups(nextUrl)).enqueue(callback)
    }
}

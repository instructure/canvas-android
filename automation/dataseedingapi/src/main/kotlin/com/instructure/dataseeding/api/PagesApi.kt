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

import com.instructure.dataseeding.model.CreatePage
import com.instructure.dataseeding.model.CreatePageWrapper
import com.instructure.dataseeding.model.PageApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object PagesApi {
    interface PagesService {
        @POST("courses/{courseId}/pages")
        fun createCoursePage(@Path("courseId") courseId: Long, @Body createCoursePage: CreatePageWrapper): Call<PageApiModel>
    }

    private fun pagesService(token: String): PagesService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(PagesService::class.java)

    fun createCoursePage(
            courseId: Long,
            token: String,
            published: Boolean = true,
            frontPage: Boolean = false,
            body: String = Randomizer.randomPageBody(),
            editingRoles: String? = null,
            pageTitle: String? = null
            ): PageApiModel {
        val page = CreatePageWrapper(CreatePage(pageTitle ?: Randomizer.randomPageTitle(), body, published, frontPage, editingRoles))

        return pagesService(token)
                .createCoursePage(courseId, page)
                .execute()
                .body()!!
    }
}

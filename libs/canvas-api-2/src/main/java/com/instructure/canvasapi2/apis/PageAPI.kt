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
 */

package com.instructure.canvasapi2.apis

import androidx.annotation.NonNull
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.postmodels.PagePostBodyWrapper
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.DataResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

object PageAPI {

    interface PagesInterface {
        @GET("{contextId}/pages?sort=title&order=asc")
        fun getFirstPagePages(
                @Path("contextId") contextId: Long): Call<List<Page>>

        @GET("{contextType}/{contextId}/pages?sort=title&order=asc")
        suspend fun getFirstPagePages(
            @Path("contextId") contextId: Long, @Path("contextType") contextType: String, @Tag params: RestParams
        ): DataResult<List<Page>>

        @GET("{contextType}/{contextId}/pages?sort=title&order=asc&include[]=body")
        suspend fun getFirstPagePagesWithBody(
            @Path("contextId") contextId: Long, @Path("contextType") contextType: String, @Tag params: RestParams
        ): DataResult<List<Page>>

        @GET
        fun getNextPagePagesList(
            @Url nextURL: String
        ): Call<List<Page>>

        @GET
        suspend fun getNextPagePagesList(
            @Url nextURL: String, @Tag params: RestParams
        ): DataResult<List<Page>>

        @GET("{contextId}/pages/{pageId}")
        fun getDetailedPage(
                @Path("contextId") contextId: Long,
                @Path("pageId") pageId: String): Call<Page>

        @GET("courses/{contextId}/pages/{pageId}")
        suspend fun getDetailedPage(@Path("contextId") contextId: Long,
                                           @Path("pageId") pageId: String,
                                           @Tag params: RestParams): DataResult<Page>

        @GET("{contextType}/{contextId}/pages/{pageId}")
        suspend fun getDetailedPage(
            @Path("contextType") contextType: String,
            @Path("contextId") contextId: Long,
            @Path("pageId") pageId: String,
            @Tag params: RestParams
        ): DataResult<Page>

        @GET("{contextId}/front_page")
        fun getFrontPage(
                @Path("contextId") contextId: Long): Call<Page>

        @GET("{contextType}/{contextId}/front_page")
        suspend fun getFrontPage(
            @Path("contextType") contextType: String,
            @Path("contextId") contextId: Long,
            @Tag params: RestParams
        ): DataResult<Page>

        @PUT("{contextId}/pages/{pageUrl}")
        fun editPage(
                @Path("contextId") contextId: Long,
                @Path("pageUrl") pageUrl: String,
                @Body body: PagePostBodyWrapper): Call<Page>

        @Multipart
        @POST("{contextId}/pages")
        fun createPage(
                @Path("contextId") contextId: Long,
                @Part("wiki_page[body]") body: RequestBody,
                @Part("wiki_page[title]") title: RequestBody,
                @Part("wiki_page[editing_roles]") editingRoles: RequestBody,
                @Part("wiki_page[front_page]") isFrontPage: Boolean,
                @Part("wiki_page[published]") isPublished: Boolean): Call<Page>

        @DELETE("{contextId}/pages/{pageUrl}")
        fun deletePage(
                @Path("contextId") contextId: Long,
                @Path("pageUrl") pageUrl: String): Call<Page>
    }

    fun getFirstPagePages(@NonNull adapter: RestBuilder, @NonNull params: RestParams, canvasContext: CanvasContext, callback: StatusCallback<List<Page>>) {
        callback.addCall(adapter.build(PagesInterface::class.java, params)
                .getFirstPagePages(canvasContext.id)).enqueue(callback)
    }

    fun getNextPagePages(@NonNull adapter: RestBuilder, @NonNull params: RestParams, nextURL: String, callback: StatusCallback<List<Page>>) {
        callback.addCall(adapter.build(PagesInterface::class.java, params)
                .getNextPagePagesList(nextURL)).enqueue(callback)
    }

    fun getDetailedPage(@NonNull adapter: RestBuilder, @NonNull params: RestParams, canvasContext: CanvasContext, pageId: String, callback: StatusCallback<Page>) {
        callback.addCall(adapter.build(PagesInterface::class.java, params)
                .getDetailedPage(canvasContext.id, pageId)).enqueue(callback)
    }

    fun getFrontPage(@NonNull adapter: RestBuilder, @NonNull params: RestParams, canvasContext: CanvasContext, callback: StatusCallback<Page>) {
        callback.addCall(adapter.build(PagesInterface::class.java, params)
                .getFrontPage(canvasContext.id)).enqueue(callback)
    }

    fun editPage(@NonNull adapter: RestBuilder, @NonNull params: RestParams, canvasContext: CanvasContext, pageUrl: String, pagePostBody: PagePostBodyWrapper, callback: StatusCallback<Page>) {
        callback.addCall(adapter.build(PagesInterface::class.java, params).editPage(canvasContext.id, pageUrl, pagePostBody)).enqueue(callback)
    }

    fun createPage(adapter: RestBuilder,
                         params: RestParams,
                         canvasContext: CanvasContext,
                         newPage: Page,
                         callback: StatusCallback<Page>) {
        callback.addCall(adapter.build(PagesInterface::class.java, params)
                .createPage(
                        canvasContext.id,
                        APIHelper.makeRequestBody(newPage.body),
                        APIHelper.makeRequestBody(newPage.title),
                        APIHelper.makeRequestBody(newPage.editingRoles),
                        newPage.frontPage,
                        newPage.published
                )).enqueue(callback)
    }

    fun deletePage(@NonNull adapter: RestBuilder, @NonNull params: RestParams, canvasContext: CanvasContext, pageUrl: String, callback: StatusCallback<Page>) {
        callback.addCall(adapter.build(PagesInterface::class.java, params).deletePage(canvasContext.id, pageUrl)).enqueue(callback)
    }
}

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
import com.instructure.canvasapi2.models.Bookmark
import retrofit2.Call
import retrofit2.http.*

object BookmarkAPI {

    internal interface BookmarkInterface {

        @GET("users/self/bookmarks")
        fun getBookmarks(): Call<List<Bookmark>>

        @POST("users/self/bookmarks")
        fun createBookmark(
                @Query("name") name: String,
                @Query(value = "url", encoded = true) url: String,
                @Query("position") position: Int,
                @Body body: String): Call<Bookmark>

        @PUT("users/self/bookmarks/{id}")
        fun updateBookmark(@Path("id") bookmarkId: Long,
                @Query("name") name: String,
                @Query(value = "url", encoded = false) url: String,
                @Query("position") position: Int,
                @Body body: String): Call<Bookmark>

        @DELETE("users/self/bookmarks/{id}")
        fun deleteBookmark(@Path("id") bookmarkId: Long): Call<Bookmark>
    }

    fun getBookmarks(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<Bookmark>>) {
        callback.addCall(adapter.build(BookmarkInterface::class.java, params).getBookmarks()).enqueue(callback)
    }

    fun createBookmark(bookmark: Bookmark, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Bookmark>) {
        callback.addCall(adapter.build(BookmarkInterface::class.java, params).createBookmark(bookmark.name!!, bookmark.url!!, bookmark.position, "")).enqueue(callback)
    }

    fun deleteBookmark(bookmarkId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Bookmark>) {
        callback.addCall(adapter.build(BookmarkInterface::class.java, params).deleteBookmark(bookmarkId)).enqueue(callback)
    }

    fun updateBookmark(bookmark: Bookmark, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Bookmark>) {
        callback.addCall(adapter.build(BookmarkInterface::class.java, params).updateBookmark(bookmark.id, bookmark.name!!, bookmark.url!!, bookmark.position, "")).enqueue(callback)
    }

}

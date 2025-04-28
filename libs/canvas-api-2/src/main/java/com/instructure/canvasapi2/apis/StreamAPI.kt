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

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.HiddenStreamItem
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url

object StreamAPI {

    interface StreamInterface {
        @GET("users/self/activity_stream?only_active_courses=true")
        fun getUserStream(): Call<List<StreamItem>>

        @GET("users/self/activity_stream?only_active_courses=true")
        suspend fun getUserStream(@Tag params: RestParams): DataResult<List<StreamItem>>

        @GET("{contextId}/activity_stream?only_active_courses=true")
        fun getContextStream(@Path("contextId") contextId: Long): Call<List<StreamItem>>

        @GET
        fun getNextPageStream(@Url nextURL: String): Call<List<StreamItem>>

        @DELETE("users/self/activity_stream/{streamId}")
        fun hideStreamItem(@Path("streamId") streamId: Long): Call<HiddenStreamItem>

        @GET("users/self/activity_stream?only_active_courses=true")
        fun getUserStreamCustomCount(@Query("per_page") number: Int): Call<List<StreamItem>>
    }

    fun getUserStream(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<StreamItem>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(StreamInterface::class.java, params).getUserStream()).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(StreamInterface::class.java, params).getNextPageStream(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getCourseStream(canvasContext: CanvasContext, adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<StreamItem>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(StreamInterface::class.java, params).getContextStream(canvasContext.id)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(StreamInterface::class.java, params).getNextPageStream(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun hideStreamItem(streamId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<HiddenStreamItem>) {
        callback.addCall(adapter.build(StreamInterface::class.java, params).hideStreamItem(streamId)).enqueue(callback)
    }

    fun getUserStreamSynchronous(numberToReturn: Int, adapter: RestBuilder, params: RestParams): List<StreamItem>? {
        return try {
            adapter.build(StreamInterface::class.java, params).getUserStreamCustomCount(numberToReturn).execute().body()
        } catch (E: Exception) {
            null
        }
    }
}

package com.instructure.canvasapi2.apis

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

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CourseNickname
import com.instructure.canvasapi2.utils.DataResult

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

object CourseNicknameAPI {

    interface NicknameInterface {

        @get:GET("users/self/course_nicknames/")
        val allNicknames: Call<List<CourseNickname>>

        @GET("users/self/course_nicknames/{course_id}")
        fun getNickname(@Path("course_id") courseId: Long): Call<CourseNickname>

        @PUT("users/self/course_nicknames/{course_id}")
        fun setNickname(@Path("course_id") courseId: Long, @Query("nickname") nickname: String): Call<CourseNickname>

        @PUT("users/self/course_nicknames/{course_id}")
        suspend fun setNickname(
            @Path("course_id") courseId: Long,
            @Query("nickname") nickname: String,
            @Tag restParams: RestParams
        ): DataResult<CourseNickname>

        @DELETE("users/self/course_nicknames/{course_id}")
        fun deleteNickname(@Path("course_id") courseId: Long): Call<CourseNickname>

        @DELETE("users/self/course_nicknames/{course_id}")
        suspend fun deleteNickname(@Path("course_id") courseId: Long, @Tag restParams: RestParams): DataResult<CourseNickname>

        @DELETE("users/self/course_nicknames/")
        fun deleteAllNicknames(): Call<CourseNickname>
    }

    fun getAllNicknames(adapter: RestBuilder, callback: StatusCallback<List<CourseNickname>>, params: RestParams) {
        callback.addCall(adapter.build(NicknameInterface::class.java, params).allNicknames).enqueue(callback)
    }

    fun getNickname(courseId: Long, adapter: RestBuilder, callback: StatusCallback<CourseNickname>, params: RestParams) {
        callback.addCall(adapter.build(NicknameInterface::class.java, params).getNickname(courseId)).enqueue(callback)
    }

    fun setNickname(courseId: Long, nickname: String, adapter: RestBuilder, callback: StatusCallback<CourseNickname>, params: RestParams) {
        // Reduces the nickname to only 60 max chars per the api docs.
        val shortenedNickname = nickname.substring(0, Math.min(nickname.length, 60))

        callback.addCall(adapter.build(NicknameInterface::class.java, params).setNickname(courseId, shortenedNickname)).enqueue(callback)
    }

    fun deleteNickname(courseId: Long, adapter: RestBuilder, callback: StatusCallback<CourseNickname>, params: RestParams) {
        callback.addCall(adapter.build(NicknameInterface::class.java, params).deleteNickname(courseId)).enqueue(callback)
    }

    fun deleteAllNicknames(adapter: RestBuilder, callback: StatusCallback<CourseNickname>, params: RestParams) {
        callback.addCall(adapter.build(NicknameInterface::class.java, params).deleteAllNicknames()).enqueue(callback)
    }
}

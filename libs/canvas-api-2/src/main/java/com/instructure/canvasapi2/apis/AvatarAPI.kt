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
import com.instructure.canvasapi2.models.Avatar
import com.instructure.canvasapi2.models.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

object AvatarAPI {

    internal interface AvatarsInterface {
        @PUT("users/self")
        fun updateAvatar(@Query("user[avatar][url]") avatarUrl: String): Call<User>

        @PUT("users/self")
        fun updateAvatarWithToken(@Query("user[avatar][token]") avatarToken: String): Call<User>
    }

    fun updateAvatar(adapter: RestBuilder, params: RestParams, avatarUrl: String, callback: StatusCallback<User>) {
        callback.addCall(adapter.build(AvatarsInterface::class.java, params).updateAvatar(avatarUrl)).enqueue(callback)
    }

    fun updateAvatarWithToken(adapter: RestBuilder, params: RestParams, avatarToken: String, callback: StatusCallback<User>) {
        callback.addCall(adapter.build(AvatarsInterface::class.java, params).updateAvatarWithToken(avatarToken)).enqueue(callback)
    }
}

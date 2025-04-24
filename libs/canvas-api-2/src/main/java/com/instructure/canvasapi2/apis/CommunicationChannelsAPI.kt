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
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.utils.DataResult
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

object CommunicationChannelsAPI {

    interface CommunicationChannelInterface {
        @GET("users/{userId}/communication_channels")
        fun getCommunicationChannels(@Path("userId") userId: Long): Call<List<CommunicationChannel>>

        @GET("users/{userId}/communication_channels")
        suspend fun getCommunicationChannels(@Path("userId") userId: Long, @Tag params: RestParams): DataResult<List<CommunicationChannel>>

        @POST("users/self/communication_channels?communication_channel[type]=push")
        fun addPushCommunicationChannel(@Query("communication_channel[token]") registrationId: String): Call<ResponseBody>

        @POST("users/self/communication_channels?communication_channel[type]=push")
        suspend fun addPushCommunicationChannel(@Query("communication_channel[token]") registrationId: String, @Tag params: RestParams): DataResult<ResponseBody>

        @DELETE("users/self/communication_channels/push")
        fun deletePushCommunicationChannel(@Query("push_token") registrationId: String): Call<ResponseBody>

        @DELETE("users/self/communication_channels/push")
        suspend fun deletePushCommunicationChannel(@Query("push_token") registrationId: String, @Tag params: RestParams): DataResult<ResponseBody>
    }

    fun getCommunicationChannels(
            userId: Long,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<List<CommunicationChannel>>) {
        callback.addCall(adapter.build(CommunicationChannelInterface::class.java, params).getCommunicationChannels(userId)).enqueue(callback)
    }

    fun addNewPushCommunicationChannelSynchronous(registrationId: String, adapter: RestBuilder, params: RestParams): Response<ResponseBody>? {
        return try {
            adapter.build(CommunicationChannelInterface::class.java, params).addPushCommunicationChannel(registrationId).execute()
        } catch (e: Exception) {
            null
        }

    }

    fun deletePushCommunicationChannelSynchronous(registrationId: String) {
        try {
            RestBuilder().build(CommunicationChannelInterface::class.java, RestParams())
                .deletePushCommunicationChannel(registrationId)
                .execute()
        } catch (e: Exception) {
            // Don't crash, just catch
        }
    }

    fun addNewPushCommunicationChannel(
            registrationId: String,
            callback: StatusCallback<ResponseBody>,
            adapter: RestBuilder,
            params: RestParams) {
        callback.addCall(adapter.build(CommunicationChannelInterface::class.java, params).addPushCommunicationChannel(registrationId)).enqueue(callback)
    }
}

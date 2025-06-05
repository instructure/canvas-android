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
import com.instructure.canvasapi2.models.NotificationPreferenceResponse
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Tag

object NotificationPreferencesAPI {

    interface NotificationPreferencesInterface {
        @GET("users/{userId}/communication_channels/{communicationChannelId}/notification_preferences")
        fun getNotificationPreferences(
                @Path("userId") userId: Long,
                @Path("communicationChannelId") communicationChannelId: Long
        ): Call<NotificationPreferenceResponse>

        @GET("users/{userId}/communication_channels/{communicationChannelId}/notification_preferences")
        suspend fun getNotificationPreferences(
            @Path("userId") userId: Long,
            @Path("communicationChannelId") communicationChannelId: Long,
            @Tag params: RestParams
        ): DataResult<NotificationPreferenceResponse>

        @PUT("users/self/communication_channels/{communicationChannelId}/notification_preferences")
        fun updateMultipleNotificationPreferences(
                @Path("communicationChannelId") communicationChannelId: Long,
                @QueryMap notifications: Map<String, String>
        ): Call<NotificationPreferenceResponse>

        @PUT("users/self/communication_channels/{communicationChannelId}/notification_preferences")
        suspend fun updateMultipleNotificationPreferences(
            @Path("communicationChannelId") communicationChannelId: Long,
            @QueryMap notifications: Map<String, String>,
            @Tag params: RestParams
        ): DataResult<NotificationPreferenceResponse>

        @PUT("users/self/communication_channels/{communicationChannelId}/notification_preferences/{category}")
        fun updatePreferenceCategory(
                @Path("category") categoryName: String,
                @Path("communicationChannelId") communicationChannelId: Long,
                @Query("notification_preferences[frequency]") frequency: String
        ): Call<NotificationPreferenceResponse>

        @PUT("users/self/communication_channels/{communicationChannelId}/notification_preferences/{category}")
        suspend fun updatePreferenceCategory(
            @Path("category") categoryName: String,
            @Path("communicationChannelId") communicationChannelId: Long,
            @Query("notification_preferences[frequency]") frequency: String,
            @Tag params: RestParams
        ): DataResult<NotificationPreferenceResponse>
    }

    fun getNotificationPreferences(
            userId: Long,
            communicationChannelId: Long,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<NotificationPreferenceResponse>) {
        val call = adapter
                .build(NotificationPreferencesInterface::class.java, params)
                .getNotificationPreferences(userId, communicationChannelId)
        callback.addCall(call).enqueue(callback)
    }

    fun updateMultipleNotificationPreferences(
            communicationChannelId: Long,
            notifications: List<String>,
            frequency: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<NotificationPreferenceResponse>) {
        val prefMap = notifications.associate { "notification_preferences[$it][frequency]" to frequency }
        val call = adapter
                .build(NotificationPreferencesInterface::class.java, params)
                .updateMultipleNotificationPreferences(communicationChannelId, prefMap)
        callback.addCall(call).enqueue(callback)
    }

    fun updatePreferenceCategory(
            categoryName: String,
            communicationChannelId: Long,
            frequency: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<NotificationPreferenceResponse>) {
        val call = adapter
                .build(NotificationPreferencesInterface::class.java, params)
                .updatePreferenceCategory(categoryName, communicationChannelId, frequency)
        callback.addCall(call).enqueue(callback)
    }
}

/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.CommunicationChannelsAPI
import com.instructure.canvasapi2.apis.NotificationPreferencesAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.models.NotificationPreference
import javax.inject.Inject

class NotificationPreferencesNetworkDataSource @Inject constructor(
    private val notificationsApi: NotificationPreferencesAPI.NotificationPreferencesInterface,
    private val communicationChannelsApi: CommunicationChannelsAPI.CommunicationChannelInterface,
) {
    suspend fun getNotificationChannels(userId: Long): List<CommunicationChannel> {
        val restParams = RestParams(isForceReadFromNetwork = true)
        return communicationChannelsApi.getCommunicationChannels(userId, restParams).dataOrThrow
    }

    suspend fun getNotificationPreferences(userId: Long, communicationChannelId: Long): List<NotificationPreference> {
        val restParams = RestParams(isForceReadFromNetwork = true)
        return notificationsApi.getNotificationPreferences(userId, communicationChannelId, restParams)
            .dataOrThrow
            .notificationPreferences
    }

    suspend fun updatePreferenceCategory(category: String, channelId: Long, frequency: String) {
        val restParams = RestParams(isForceReadFromNetwork = true)
        notificationsApi.updatePreferenceCategory(category, channelId, frequency, restParams).dataOrThrow
    }
}

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
package com.instructure.canvasapi2.managers

import androidx.annotation.StringRes
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.NotificationPreferencesAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.NotificationPreferenceResponse
import com.instructure.canvasapi2.utils.weave.apiAsync

class NotificationPreferencesManager(private val notificationPreferencesApi: NotificationPreferencesAPI) {

    fun getNotificationPreferences(
        userId: Long,
        commChannelId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<NotificationPreferenceResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        notificationPreferencesApi.getNotificationPreferences(
            userId,
            commChannelId,
            adapter,
            params,
            callback
        )
    }

    fun getNotificationPreferencesAsync(
            userId: Long,
            commChannelId: Long,
            forceNetwork: Boolean
    ) = apiAsync<NotificationPreferenceResponse> { getNotificationPreferences(userId, commChannelId, forceNetwork, it) }

    fun updatePreferenceCategory(
        categoryName: String,
        channelId: Long,
        frequency: String,
        callback: StatusCallback<NotificationPreferenceResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        notificationPreferencesApi.updatePreferenceCategory(
            categoryName,
            channelId,
            frequency,
            adapter,
            params,
            callback
        )
    }

    fun updatePreferenceCategoryAsync(
            categoryName: String,
            channelId: Long,
            frequency: String
    ) = apiAsync<NotificationPreferenceResponse> { updatePreferenceCategory(categoryName, channelId, frequency, it) }
}

enum class NotificationPreferencesFrequency(val apiString: String, @StringRes val stringRes: Int) {
    IMMEDIATELY("immediately", R.string.emailNotificationsImmediately),
    DAILY("daily", R.string.emailNotificationsDaily),
    WEEKLY("weekly", R.string.emailNotificationsWeekly),
    NEVER("never", R.string.emailNotificationsNever);

    companion object {
        fun fromApiString(apiString: String) = values().find { apiString == it.apiString } ?: IMMEDIATELY
    }
}

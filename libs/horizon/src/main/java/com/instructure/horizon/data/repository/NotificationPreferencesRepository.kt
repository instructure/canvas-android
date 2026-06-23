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
package com.instructure.horizon.data.repository

import com.instructure.horizon.data.datasource.NotificationPreferencesNetworkDataSource
import com.instructure.horizon.model.AccountNotificationCategory
import com.instructure.horizon.model.AccountNotificationFrequency
import com.instructure.horizon.model.AccountNotificationPreference
import com.instructure.horizon.model.AccountNotificationType
import com.instructure.horizon.model.HorizonAccountNotificationChannel
import javax.inject.Inject

class NotificationPreferencesRepository @Inject constructor(
    private val networkDataSource: NotificationPreferencesNetworkDataSource,
) {
    suspend fun getNotificationPreferences(userId: Long): List<AccountNotificationPreference> {
        val communicationChannels = networkDataSource.getNotificationChannels(userId)
        return communicationChannels.flatMap { channel ->
            val notificationPreferences = networkDataSource.getNotificationPreferences(userId, channel.id)

            notificationPreferences.mapNotNull { notificationPreference ->
                val type = channel.type
                val category = AccountNotificationCategory.fromApiString(notificationPreference.category)
                if (type != null && category != null) {
                    AccountNotificationPreference(
                        channel = HorizonAccountNotificationChannel(
                            id = channel.id,
                            type = AccountNotificationType.fromApiString(type),
                        ),
                        category = category,
                        frequency = AccountNotificationFrequency.fromApiString(notificationPreference.frequency),
                    )
                } else {
                    null
                }
            }
        }
    }

    suspend fun updateNotificationPreference(
        category: AccountNotificationCategory,
        channelId: Long,
        frequency: AccountNotificationFrequency,
    ) {
        networkDataSource.updatePreferenceCategory(category.apiString, channelId, frequency.apiString)
    }
}

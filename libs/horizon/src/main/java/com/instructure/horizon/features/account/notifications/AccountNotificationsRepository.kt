/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.account.notifications

import com.instructure.canvasapi2.apis.CommunicationChannelsAPI
import com.instructure.canvasapi2.apis.NotificationPreferencesAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.models.NotificationPreference
import javax.inject.Inject

class AccountNotificationsRepository @Inject constructor(
    private val notificationsApi: NotificationPreferencesAPI.NotificationPreferencesInterface,
    private val communicationChannelsApi: CommunicationChannelsAPI.CommunicationChannelInterface
) {
    suspend fun updateNotificationPreference(
        category: AccountNotificationCategory,
        channelId: Long,
        frequency: AccountNotificationFrequency
    ) {
        val restParams = RestParams(isForceReadFromNetwork = true)
        notificationsApi.updatePreferenceCategory(
            category.apiString,
            channelId,
            frequency.apiString,
            restParams
        ).dataOrThrow
    }

    suspend fun registerPushNotification() {

    }

    suspend fun unregisterPushNotification() {

    }

    suspend fun getNotificationPreferences(userId: Long): List<AccountNotificationPreference> {
        val communicationChannels = getNotificationChannels(userId)
        val settings = communicationChannels.flatMap { channel ->
            val notificationPreferences = getNotificationPreferences(userId, channel.id)

            notificationPreferences.mapNotNull { notificationPreference ->
                val type = channel.type
                val category = AccountNotificationCategory.fromApiString(notificationPreference.category)
                if (type != null && category != null) {
                    AccountNotificationPreference(
                        channelId = channel.id,
                        type = AccountNotificationType.fromApiString(type),
                        category = category,
                        frequency = AccountNotificationFrequency.fromApiString(notificationPreference.frequency)
                    )
                } else {
                    null
                }
            }
        }.distinctBy { Pair(it.channelId, it.category) }

        return settings
    }

    private suspend fun getNotificationChannels(userId: Long): List<CommunicationChannel> {
        val restParams = RestParams(isForceReadFromNetwork = true)
        return communicationChannelsApi.getCommunicationChannels(userId, restParams)
            .dataOrThrow
    }
    private suspend fun getNotificationPreferences(userId: Long, communicationChannelId: Long): List<NotificationPreference> {
        val restParams = RestParams(isForceReadFromNetwork = true)
        return notificationsApi.getNotificationPreferences(userId, communicationChannelId, restParams)
            .dataOrThrow
            .notificationPreferences
    }
}

data class AccountNotificationPreference(
    val channelId: Long,
    val type: AccountNotificationType,
    val category: AccountNotificationCategory,
    val frequency: AccountNotificationFrequency
)

enum class AccountNotificationCategory(val apiString: String) {
    ACCOUNT_NOTIFICATION("account_notification"),
    ANNOUNCEMENT("announcement"),
    CONVERSATION_MESSAGE("conversation_message"),
    DUE_DATE("due_date"),
    GRADING("grading");

    companion object {
        fun fromApiString(apiString: String): AccountNotificationCategory? {
            return values().firstOrNull { it.apiString == apiString }
        }
    }
}

enum class AccountNotificationFrequency(val apiString: String) {
    IMMEDIATELY("immediately"),
    NEVER("never");

    companion object {
        fun fromApiString(apiString: String): AccountNotificationFrequency {
            return values().firstOrNull { it.apiString == apiString } ?: NEVER
        }
    }
}
enum class AccountNotificationType(val apiString: String) {
    EMAIL("email"),
    PUSH("push");

    companion object {
        fun fromApiString(apiString: String): AccountNotificationType {
            return values().firstOrNull { it.apiString == apiString } ?: EMAIL
        }
    }
}
/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.pandautils.models

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.PandaPrefs
import java.util.*

data class PushNotification(
    val from: String = "",
    val htmlUrl: String = "",
    val alert: String = "",
    val collapseKey: String = "",
    val userId: String = "",
    val notificationId: Int = 0
) {

    companion object {
        const val GROUP_ID = 344555

        const val HTML_URL: String = "html_url" // Used to pass around destination in intents

        private const val ALERT: String = "alert"
        private const val COLLAPSE_KEY: String = "collapse_key"
        private const val USER_ID: String = "user_id"

        private const val PUSH_NOTIFICATIONS_KEY = "pandaNotificationsKey"

        @Deprecated("Starting with MBL-12556, notifications require an id to separate out instead of being a single inbox notification")
        private const val OLD_PUSH_NOTIFICATIONS_KEY = "pushNotificationsKey"

        fun fromData(from: String, data: Map<String, String>?): PushNotification {
            val notificationId = (getPushNotifications().lastOrNull()?.notificationId ?: 0) + 1

            return PushNotification(
                from = from,
                htmlUrl = data?.get(HTML_URL) ?: "",
                alert = data?.get(ALERT) ?: "",
                collapseKey = data?.get(COLLAPSE_KEY) ?: "",
                userId = data?.get(USER_ID) ?: "",
                notificationId = notificationId

            )
        }

        fun store(push: PushNotification) {
            val pushes = getPushNotifications()
            pushes.add(push)

            PandaPrefs.putString(PUSH_NOTIFICATIONS_KEY, Gson().toJson(pushes))
        }

        fun remove(intent: Intent) {
            val notificationId = intent.getIntExtra(PushExternalReceiver.ID_PUSH_NOTIFICATION, GROUP_ID)
            if (notificationId == GROUP_ID) {
                PandaPrefs.remove(PUSH_NOTIFICATIONS_KEY)
                return
            }

            val pushes = getPushNotifications()

            // Remove only the notification matching the ID
            pushes.removeAll { push -> push.notificationId == notificationId }
            PandaPrefs.putString(PUSH_NOTIFICATIONS_KEY, Gson().toJson(pushes))

            // If we only have no notifications left, we don't need a group summary anymore
            if (pushes.size == 0) {
                (ContextKeeper.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .cancel(GROUP_ID)
            }
        }

        fun clearPushHistory() {
            val nm = (ContextKeeper.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

            // Cancel the group summary
            nm.cancel(GROUP_ID)

            // Cancel all other pushes
            val pushes = getPushNotifications()
            pushes.forEach { push ->
                nm.cancel(push.notificationId)
            }

            // Delete the stored notifications
            PandaPrefs.remove(PUSH_NOTIFICATIONS_KEY)
        }

        /**
         * Helper function to retrieve all stored push notifications.
         *
         * This will migrate over old pushes so that each stored push will show as its own notification.
         */
        fun getAllStoredPushes(): ArrayList<PushNotification> {
            val pushes = ArrayList<PushNotification>()

            val notifications = getPushNotifications()
            val oldNotifications = getPushNotifications(OLD_PUSH_NOTIFICATIONS_KEY)

            // Migrate old notifications to new notifications
            var id = notifications.lastOrNull()?.notificationId ?: 0
            oldNotifications.forEach { push ->
                pushes.add(push.copy(notificationId = ++id))
            }

            pushes.addAll(notifications)

            // Update stored pushes if we migrated, removing the old notifications
            if (oldNotifications.isNotEmpty()) {
                PandaPrefs.putString(PUSH_NOTIFICATIONS_KEY, Gson().toJson(pushes))
                PandaPrefs.remove(OLD_PUSH_NOTIFICATIONS_KEY)
            }

            return pushes
        }

        /**
         * A helper function introduced in MBL-12556 to help retrieve notifications. This supports the old key that is
         * for the single notification (OLD_PUSH_NOTIFICATIONS_KEY that is now deprecated), as well as the
         * PUSH_NOTIFICATIONS_KEY that should be used since separating out pushes into their own notifications.
         */
        private fun getPushNotifications(key: String = PUSH_NOTIFICATIONS_KEY): ArrayList<PushNotification> {
            val json = PandaPrefs.getString(key, "")
            if (!json.isNullOrBlank()) {
                val type = object : TypeToken<List<PushNotification>>() {}.type
                return Gson().fromJson(json, type) ?: ArrayList()
            }

            // Return an empty list if nothing was stored before
            return ArrayList()
        }
    }
}

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

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.utils.PandaPrefs
import java.util.*

data class PushNotification(
    var htmlUrl: String = "",
    var from: String = "",
    var alert: String = "",
    var collapseKey: String = "",
    var userId: String = ""
) {
    /**
     * Basic idea is that we STORE push notification info based on the incoming PUSH data.
     * But we RETRIEVE push notification info based on the current users data.
     *
     * If we store it correctly then it can be retrieved correctly.
     *
     * The HTML url has the domain as part of the url and the users id, the users id also has the shard so we
     * have to strip off the shard so we have the actual user id. With the domain and user id we can make
     * a key to store push notifications with.
     */
    fun PushNotification(
        html_url: String,
        from: String,
        alert: String,
        collapse_key: String,
        user_id: String
    ): PushNotification {
        this.htmlUrl = html_url
        this.from = from
        this.alert = alert
        this.collapseKey = collapse_key
        this.userId = user_id

        return this
    }

    companion object {
        const val HTML_URL: String = "html_url"
        const val FROM: String = "from"
        const val ALERT: String = "alert"
        const val COLLAPSE_KEY: String = "collapse_key"
        const val USER_ID: String = "user_id"
        const val NOTIFY_ID = 555443

        private const val PUSH_NOTIFICATIONS_KEY = "pushNotificationsKey"

        fun store(push: PushNotification) {
            val pushes = getPushNotifications(PUSH_NOTIFICATIONS_KEY)
            pushes.add(push)

            PandaPrefs.putString(PUSH_NOTIFICATIONS_KEY, Gson().toJson(pushes))
        }

        fun clearPushHistory() {
            // Delete the old notifications keyed off of user/domain (deprecated)
            getPushStoreKey()?.let { key -> PandaPrefs.remove(key) }

            // Delete the new notifications (no longer filtering)
            PandaPrefs.remove(PUSH_NOTIFICATIONS_KEY)
        }

        fun getAllStoredPushes(): ArrayList<PushNotification> {
            val pushes = ArrayList<PushNotification>()

            // Combine both the old and new notifications to display to the user
            pushes.addAll(getPushNotifications(getPushStoreKey()))
            pushes.addAll(getPushNotifications(PUSH_NOTIFICATIONS_KEY))

            return pushes
        }

        /**
         * A helper function introduced in MBL-13725 to help retrieve notifications. This supports the old keys that are
         * unique per user id and domain (that is now deprecated), as well as the single key PUSH_NOTIFICATIONS_KEY that
         * should be used since filtering is no longer happening.
         */
        private fun getPushNotifications(key: String?): ArrayList<PushNotification> {
            if (!key.isNullOrBlank()) {
                val json = PandaPrefs.getString(key, "")
                if (!json.isNullOrBlank()) {
                    val type = object : TypeToken<List<PushNotification>>() {}.type
                    return Gson().fromJson(json, type) ?: ArrayList()
                }
            }

            return ArrayList()
        }

        private fun getPushStorePrefix(): String? {
            val user = ApiPrefs.user
            var domain = ApiPrefs.domain

            if (user == null || TextUtils.isEmpty(domain)) {
                return null
            }

            if (BuildConfig.IS_DEBUG) {
                domain = domain.replaceFirst(".beta".toRegex(), "")
            }

            return user.id.toString() + "___" + domain + "___"
        }

        // TODO: Remove this in a future release from the version that this was deprecated (MBL-13725) and clean up some
        //  comments in this file (and inline getPushNotifications into getAllStoredPushes)
        @Deprecated("Push notifications are no longer filtered by user/domain, going forward should just use PUSH_NOTIFICATIONS_KEY")
        private fun getPushStoreKey(): String? {
            val prefix = getPushStorePrefix() ?: return null
            return prefix + PUSH_NOTIFICATIONS_KEY
        }
    }
}

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

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.utils.PandaPrefs
import java.net.MalformedURLException
import java.net.URL
import java.util.*

data class PushNotification(

    var htmlUrl: String  = "",
    var from: String  = "",
    var alert: String  = "",
    var collapseKey: String  = "",
    var userId: String  = "") {
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
    fun PushNotification(html_url: String, from: String, alert: String, collapse_key: String, user_id: String): PushNotification {
        this.htmlUrl = html_url
        this.from = from
        this.alert = alert
        this.collapseKey = collapse_key
        this.userId = user_id

        return this
    }

    companion object {
        val HTML_URL: String = "html_url"
        val FROM: String = "from"
        val ALERT: String = "alert"
        val COLLAPSE_KEY: String = "collapse_key"
        val USER_ID: String = "user_id"


        private val PUSH_NOTIFICATIONS_KEY = "pushNotificationsKey"




        fun store(push: PushNotification): Boolean {
            val pushes = getStoredPushes()
            pushes.add(push)
            val json = Gson().toJson(pushes)
            val key = getPushStoreKey(push)
            if (!key.isNullOrBlank()) {
                PandaPrefs.putString(key!!, json)
                return true
            }
            return false
        }

        fun clearPushHistory() {
            val key = getPushStoreKey()
            if (key != null) PandaPrefs.remove(key)
        }

        fun getStoredPushes(): ArrayList<PushNotification> {
            val key = getPushStoreKey()
            if (!key.isNullOrEmpty()) {
                val json = PandaPrefs.getString(key!!, "")
                if (!TextUtils.isEmpty(json)) {
                    val type = object : TypeToken<List<PushNotification>>() {

                    }.type
                    var pushes: ArrayList<PushNotification>? = Gson().fromJson(json, type)
                    if (pushes == null) {
                        pushes = ArrayList()
                    }
                    return pushes
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

        private fun getPushStorePrefix(push: PushNotification): String? {
            val userId = getUserIdFromPush(push)
            var domain: String
            try {
                val url = URL(push.htmlUrl)
                domain = url.host
            } catch (e: MalformedURLException) {
                domain = ""
            }

            return if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(domain)) {
                null
            } else userId + "___" + domain + "___"
        }


        private fun getPushStoreKey(): String? {
            val prefix = getPushStorePrefix() ?: return null
            return prefix + PUSH_NOTIFICATIONS_KEY
        }

        private fun getPushStoreKey(push: PushNotification): String? {
            val prefix = getPushStorePrefix(push) ?: return null
            return prefix + PUSH_NOTIFICATIONS_KEY
        }

        /**
         * To get the user id when the shard is added we have to mod the number by 10 trillion.
         * @param push The notification which should always have a valid user id with the shard.
         * @return the users id after removal of the shard id
         */
        private fun getUserIdFromPush(push: PushNotification): String = try {
                val temp = 1000000000000L
                val userId = java.lang.Long.parseLong(push.userId)
                val remainder = userId % temp
                remainder.toString()
            } catch (e: Exception) {
                ""
            }

        /**
         * Removes the shard from a user id. It is expcted that the user id passed in has a mixed in.
         * @param userIdWithShard
         * @return
         */
        fun getUserIdFromPush(userIdWithShard: String): String = try {
                val temp = 1000000000000L
                val userId = java.lang.Long.parseLong(userIdWithShard)
                val remainder = userId % temp
                remainder.toString()
            } catch (e: Exception) {
                ""
            }
    }
}

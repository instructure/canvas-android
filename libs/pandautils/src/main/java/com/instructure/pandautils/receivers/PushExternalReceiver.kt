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

package com.instructure.pandautils.receivers

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.ColorRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.R
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.services.PushNotificationRegistrationService

abstract class PushExternalReceiver : FirebaseMessagingService() {
    abstract fun getAppColor(): Int
    abstract fun getAppName(context: Context): String
    abstract fun getStartingActivityClass(): Class<out Activity>

    override fun onNewToken(token: String?) {
        if (token == null) return

        PushNotificationRegistrationService.scheduleJob(applicationContext, ApiPrefs.isMasquerading)
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        Logger.d("PushExternalReceiver onReceive()")
        val data = message?.data ?: return

        val from = message.from ?: ""
        val alert = getMessage(data)
        val userId = getUserId(data)
        val htmlUrl = getHtmlUrl(data)
        val collapseKey = getCollapseKey(data)

        val push = PushNotification(htmlUrl, from, alert, collapseKey, userId)
        PushNotification.store(push)
        postNotification(this, data, getAppName(this), getStartingActivityClass(), getAppColor())
    }

    companion object {

        const val NEW_PUSH_NOTIFICATION = "newPushNotification"
        private const val CHANNEL_PUSH_GENERAL = "generalNotifications"

        fun postNotification(context: Context, data: Map<String, String>?, appName: String, startingActivity: Class<out Activity>, @ColorRes appColor: Int) {
            val loginId = ApiPrefs.user?.loginId

            if (loginId == null) {
                Logger.e("PushExternalReceiver: User loginId was null - Can't create notification channel, nor post to a notification channel with a null user loginId")
                return
            }

            // Only the first few lines get shown in the notification, and taking all could result in a crash (given an EXTREMELY large amount)
            val pushes = PushNotification.getAllStoredPushes().takeLast(10)

            if (pushes.isEmpty() && data == null) {
                // Nothing to post, situation would occur from the BootReceiver
                Logger.e("PushExternalReceiver: No notifications to show")
                return
            }

            val contentIntent = Intent(context, startingActivity)
            contentIntent.putExtra(NEW_PUSH_NOTIFICATION, true)
            data?.forEach { contentIntent.putExtra(it.key, it.value) }

            val deleteIntent = Intent(context, PushDeleteReceiver::class.java)

            val contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val inboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle(context.getString(R.string.notificationPrimaryInboxTitle))
            for (push in pushes) {
                inboxStyle.addLine(push.alert)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_PUSH_GENERAL)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setColor(ContextCompat.getColor(context, appColor))
                .setContentTitle(appName)
                .setContentText(getMessage(data))
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setAutoCancel(true)
                .setStyle(inboxStyle)
                .build()

            createNotificationChannel(context, CHANNEL_PUSH_GENERAL, loginId, nm)

            nm.notify(PushNotification.NOTIFY_ID, notification)
        }

        private fun createNotificationChannel(context: Context, channelId: String, userEmail: String, nm: NotificationManager) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            // Prevents recreation of notification channel if it exists.
            val channelList = nm.notificationChannels
            for (channel in channelList) {
                if (channelId == channel.id) {
                    return
                }
            }

            val name = context.getString(R.string.notificationChannelNamePrimary)
            val description = context.getString(R.string.notificationChannelDescriptionPrimary)

            // Create a group for the user, this enables support for multiple users
            nm.createNotificationChannelGroup(NotificationChannelGroup(userEmail, name))

            // Create the channel and add the group
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.group = userEmail

            // Create the channel
            nm.createNotificationChannel(channel)
        }

        private fun getMessage(data: Map<String, String>?) = data?.get(PushNotification.ALERT) ?: ""

        private fun getUserId(data: Map<String, String>?) = data?.get(PushNotification.USER_ID) ?: ""

        private fun getHtmlUrl(data: Map<String, String>?) = data?.get(PushNotification.HTML_URL) ?: ""

        private fun getCollapseKey(data: Map<String, String>?) = data?.get(PushNotification.COLLAPSE_KEY) ?: ""

        fun postStoredNotifications(context: Context, appName: String, startingActivity: Class<out Activity>, @ColorRes appColor: Int) {
            postNotification(context, null, appName, startingActivity, appColor)
        }
    }
}

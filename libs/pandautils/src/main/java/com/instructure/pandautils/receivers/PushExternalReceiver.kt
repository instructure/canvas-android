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
import com.instructure.pandautils.services.PushNotificationRegistrationWorker

abstract class PushExternalReceiver : FirebaseMessagingService() {
    abstract fun getAppColor(): Int
    abstract fun getAppName(context: Context): String
    abstract fun getStartingActivityClass(): Class<out Activity>

    override fun onNewToken(token: String) {
        PushNotificationRegistrationWorker.scheduleJob(applicationContext, ApiPrefs.isMasquerading)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Logger.d("PushExternalReceiver onReceive()")
        val data = message.data

        val from = message.from ?: ""

        val push = PushNotification.fromData(from, data)
        PushNotification.store(push)
        postNotifications(this, getStartingActivityClass(), getAppName(this), getAppColor(), push)
    }

    companion object {

        const val NEW_PUSH_NOTIFICATION = "newPushNotification"
        const val ID_PUSH_NOTIFICATION = "idPushNotification"
        private const val CHANNEL_PUSH_GENERAL = "generalNotifications"

        private fun postNotifications(
            context: Context,
            startingActivity: Class<out Activity>,
            appName: String,
            @ColorRes appColor: Int,
            vararg notifications: PushNotification
        ) {
            val loginId = ApiPrefs.user?.loginId

            if (loginId == null) {
                Logger.e("PushExternalReceiver: User loginId was null - Can't create notification channel, nor post to a notification channel with a null user loginId")
                return
            }

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(context, CHANNEL_PUSH_GENERAL, loginId, nm)

            notifications.forEach { push ->
                nm.notify(
                    push.notificationId,
                    createNotification(context, startingActivity, appName, appColor, CHANNEL_PUSH_GENERAL, push)
                )
            }

            // If the passed notifications is greater than 1, we're showing from boot so we don't need to retrieve
            // all stored notifications
            val allPushes =
                    if (notifications.size > 1) notifications.toList() else PushNotification.getAllStoredPushes()
            if (allPushes.size > 1) {
                nm.notify(PushNotification.GROUP_ID, createGroup(context, appColor, CHANNEL_PUSH_GENERAL))
            }
        }

        private fun createContentIntent(
            context: Context,
            startingActivity: Class<out Activity>,
            notification: PushNotification
        ): PendingIntent? {
            val contentIntent = Intent(context, startingActivity)
            contentIntent.putExtra(NEW_PUSH_NOTIFICATION, true)
            contentIntent.putExtra(ID_PUSH_NOTIFICATION, notification.notificationId)
            contentIntent.putExtra(PushNotification.HTML_URL, notification.htmlUrl)
            return PendingIntent.getActivity(
                context,
                notification.notificationId,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        private fun createDeleteIntent(context: Context, notificationId: Int): PendingIntent? {
            val deleteIntent = Intent(context, PushDeleteReceiver::class.java)
            deleteIntent.putExtra(ID_PUSH_NOTIFICATION, notificationId)
            return PendingIntent.getBroadcast(context, notificationId, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun createGroup(context: Context, @ColorRes appColor: Int, channelId: String): Notification? {
            return NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setColor(ContextCompat.getColor(context, appColor))
                .setDeleteIntent(createDeleteIntent(context, PushNotification.GROUP_ID))
                .setAutoCancel(true)
                .setGroup(channelId)
                .setGroupSummary(true)
                .build()
        }

        private fun createNotification(
            context: Context,
            startingActivity: Class<out Activity>,
            appName: String,
            @ColorRes appColor: Int,
            channelId: String,
            notification: PushNotification
        ): Notification {
            // Use the app name if we're marshmallow or lower, as it won't display the app name for us. On later versions,
            // we'll still want a title, but it will already have our app name so instead we'll use this notification title
            val title = context.getString(R.string.notificationPrimaryInboxTitle)
            return NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(notification.alert)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setColor(ContextCompat.getColor(context, appColor))
                .setContentIntent(createContentIntent(context, startingActivity, notification))
                .setDeleteIntent(createDeleteIntent(context, notification.notificationId))
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.alert))
                .setGroup(channelId)
                .build()
        }

        private fun createNotificationChannel(
            context: Context,
            channelId: String,
            loginId: String,
            nm: NotificationManager
        ) {
            val name = context.getString(R.string.notificationChannelNamePrimary)
            val description = context.getString(R.string.notificationChannelDescriptionPrimary)

            // Create a group for the user, this enables support for multiple users
            nm.createNotificationChannelGroup(NotificationChannelGroup(loginId, name)) // This name is the subtitle

            // Create the channel and add the group
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance) // This name is the actual title
            channel.description = description
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.group = loginId

            // Create the channel
            nm.createNotificationChannel(channel)
        }

        fun postStoredNotifications(
            context: Context,
            appName: String,
            startingActivity: Class<out Activity>,
            @ColorRes appColor: Int
        ) {
            val pushes = PushNotification.getAllStoredPushes()
            if (pushes.isEmpty()) {
                // Nothing to post, situation would occur from the BootReceiver
                Logger.v("PushExternalReceiver: No notifications to show")
                return
            }

            postNotifications(context, startingActivity, appName, appColor, *pushes.toTypedArray())
        }
    }
}

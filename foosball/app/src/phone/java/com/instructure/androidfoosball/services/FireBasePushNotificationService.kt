/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat
import android.util.Log

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.activities.SignInActivity


class FireBasePushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        Log.d("abcde", "From: " + remoteMessage!!.from)
        //Get the channel the users subscribed too
        val pushId = remoteMessage.from.replace("/topics/", "")
        //Unsubscribe
        FirebaseMessaging.getInstance().unsubscribeFromTopic(pushId)

        val notificationIntent = Intent(applicationContext, SignInActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val intent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)

        //post notification
        val builder = NotificationCompat.Builder(applicationContext)
        builder.setSmallIcon(R.drawable.ic_status_notification)
        builder.setContentTitle(getString(R.string.app_name))
        builder.setContentIntent(intent)
        builder.setAutoCancel(true)
        val tableName = remoteMessage.data["message"]
        val contentText = String.format(getString(R.string.push_text), tableName)
        builder.setContentText(contentText)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())

        if (remoteMessage.data.size > 0) {
            Log.d("abcde", "Message data payload: " + remoteMessage.data)
        }

        if (remoteMessage.notification != null) {
            Log.d("abcde", "Message Notification Body: " + remoteMessage.notification.body)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d("abcde", "Push Message Deleted")
    }

    override fun onMessageSent(s: String?) {
        super.onMessageSent(s)
        Log.d("abcde", "Push Message Sent: " + s!!)
    }

    override fun onSendError(s: String?, e: Exception?) {
        super.onSendError(s, e)
        Log.d("abcde", "Push Message Error: " + s!!)
    }

    companion object {

        private val NOTIFICATION_ID = 444930
    }
}

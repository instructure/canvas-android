/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.features.assignments.details.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.receivers.alarm.AlarmReceiver
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.pandautils.utils.Const
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity

class StudentAlarmReceiverNotificationHandler: AlarmReceiverNotificationHandler {
    override fun showNotification(context: Context, assignmentId: Long, assignmentPath: String, assignmentName: String, dueIn: String) {
        val intent = Intent(context, NavigationActivity.startActivityClass).apply {
            putExtra(Const.LOCAL_NOTIFICATION, true)
            putExtra(PushNotification.HTML_URL, assignmentPath)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, AlarmReceiver.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.reminderNotificationTitle))
            .setContentText(context.getString(R.string.reminderNotificationDescription, dueIn, assignmentName))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(assignmentId.toInt(), builder.build())
    }

    override fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            AlarmReceiver.CHANNEL_ID,
            context.getString(R.string.reminderNotificationChannelName),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminderNotificationChannelDescription)
        }

        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
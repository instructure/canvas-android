/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.utils.Const
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.util.goAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDao: ReminderDao

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val assignmentId = intent.getLongExtra(ASSIGNMENT_ID, 0L)
            val assignmentPath = intent.getStringExtra(ASSIGNMENT_PATH) ?: return
            val assignmentName = intent.getStringExtra(ASSIGNMENT_NAME) ?: return
            val dueIn = intent.getStringExtra(DUE_IN) ?: return

            createNotificationChannel(context)
            showNotification(context, assignmentId, assignmentPath, assignmentName, dueIn)
            goAsync {
                reminderDao.deletePastReminders(System.currentTimeMillis())
            }
        }
    }

    private fun showNotification(context: Context, assignmentId: Long, assignmentPath: String, assignmentName: String, dueIn: String) {
        val intent = Intent(context, NavigationActivity.startActivityClass).apply {
            putExtra(Const.LOCAL_NOTIFICATION, true)
            putExtra(PushNotification.HTML_URL, assignmentPath)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.reminderNotificationTitle))
            .setContentText(context.getString(R.string.reminderNotificationDescription, dueIn, assignmentName))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(assignmentId.toInt(), builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminderNotificationChannelName),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminderNotificationChannelDescription)
        }

        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "REMINDERS_CHANNEL_ID"
        const val ASSIGNMENT_ID = "ASSIGNMENT_ID"
        const val ASSIGNMENT_PATH = "ASSIGNMENT_PATH"
        const val ASSIGNMENT_NAME = "ASSIGNMENT_NAME"
        const val DUE_IN = "DUE_IN"
    }
}
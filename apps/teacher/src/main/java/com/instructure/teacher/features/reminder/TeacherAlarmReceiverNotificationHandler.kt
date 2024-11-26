package com.instructure.teacher.features.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.instructure.pandautils.receivers.alarm.AlarmReceiver
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.teacher.R
import com.instructure.teacher.activities.RouteValidatorActivity

class TeacherAlarmReceiverNotificationHandler: AlarmReceiverNotificationHandler {
    override fun showNotification(context: Context, contentId: Long, htmlPath: String, title: String, message: String) {
        val intent = RouteValidatorActivity.createIntent(context, htmlPath.toUri())

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, AlarmReceiver.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(contentId.toInt(), builder.build())
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
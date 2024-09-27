package com.instructure.parentapp.features.assignment.details.receiver

import android.content.Context
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler

class ParentAlarmReceiverNotificationHandler: AlarmReceiverNotificationHandler {
    override fun showNotification(context: Context, assignmentId: Long, assignmentPath: String, assignmentName: String, dueIn: String) = Unit

    override fun createNotificationChannel(context: Context) = Unit
}
package com.instructure.student.features.assignments.details.receiver

import android.content.Context
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler

class StudentAlarmReceiverNotificationHandler: AlarmReceiverNotificationHandler {
    override fun showNotification(context: Context, assignmentId: Long, assignmentPath: String, assignmentName: String, dueIn: String) = Unit

    override fun createNotificationChannel(context: Context) = Unit
}
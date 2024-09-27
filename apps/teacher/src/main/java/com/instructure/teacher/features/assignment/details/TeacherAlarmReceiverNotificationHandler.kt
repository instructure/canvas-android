package com.instructure.teacher.features.assignment.details

import android.content.Context
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler

class TeacherAlarmReceiverNotificationHandler: AlarmReceiverNotificationHandler {
    override fun showNotification(context: Context, assignmentId: Long, assignmentPath: String, assignmentName: String, dueIn: String) = Unit

    override fun createNotificationChannel(context: Context) = Unit
}
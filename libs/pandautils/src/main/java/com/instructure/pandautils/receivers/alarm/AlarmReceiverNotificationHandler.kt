package com.instructure.pandautils.receivers.alarm

import android.content.Context

interface AlarmReceiverNotificationHandler {
    fun showNotification(context: Context, assignmentId: Long, assignmentPath: String, assignmentName: String, dueIn: String)

    fun createNotificationChannel(context: Context)
}
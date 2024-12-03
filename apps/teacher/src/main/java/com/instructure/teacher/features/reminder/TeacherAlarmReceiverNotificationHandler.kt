package com.instructure.teacher.features.reminder

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.teacher.activities.RouteValidatorActivity

class TeacherAlarmReceiverNotificationHandler: AlarmReceiverNotificationHandler() {
    override fun getIntent(context: Context, htmlPath: String): Intent {
        return RouteValidatorActivity.createIntent(context, htmlPath.toUri())
    }
}
package com.instructure.parentapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.goAsync
import com.instructure.parentapp.R
import com.instructure.parentapp.features.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InitializeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action || Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
            // Restores stored push notifications upon boot
            PushExternalReceiver.postStoredNotifications(context, context.getString(R.string.app_name), LoginActivity::class.java, R.color.login_teacherAppTheme)

            goAsync {
                alarmScheduler.scheduleAllAlarmsForCurrentUser()
            }
        }
    }
}
/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.teacher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.goAsync
import com.instructure.teacher.R
import com.instructure.teacher.activities.LoginActivity
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
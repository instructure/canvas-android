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

package com.instructure.pandautils.features.assignments.details.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.receivers.alarm.AlarmReceiver
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao

class AlarmScheduler(private val context: Context, private val reminderDao: ReminderDao, private val apiPrefs: ApiPrefs) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(assignmentId: Long, assignmentPath: String, assignmentName: String, dueIn: String, timeInMillis: Long, reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(AlarmReceiver.ASSIGNMENT_ID, assignmentId)
        intent.putExtra(AlarmReceiver.ASSIGNMENT_PATH, assignmentPath)
        intent.putExtra(AlarmReceiver.ASSIGNMENT_NAME, assignmentName)
        intent.putExtra(AlarmReceiver.DUE_IN, dueIn)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    suspend fun scheduleAllAlarmsForCurrentUser() {
        val reminders = reminderDao.findByUserId(apiPrefs.user?.id ?: return)
        reminders.forEach {
            scheduleAlarm(it.assignmentId, it.htmlUrl, it.name, it.text, it.time, it.id)
        }
    }

    fun cancelAlarm(reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    suspend fun cancelAllAlarmsForCurrentUser() {
        val reminders = reminderDao.findByUserId(apiPrefs.user?.id ?: return)
        reminders.forEach {
            cancelAlarm(it.id)
        }
    }
}

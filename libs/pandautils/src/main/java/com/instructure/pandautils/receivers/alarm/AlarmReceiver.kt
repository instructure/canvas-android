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

package com.instructure.pandautils.receivers.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var notificationHandler: AlarmReceiverNotificationHandler

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val assignmentId = intent.getLongExtra(ASSIGNMENT_ID, 0L)
            val assignmentPath = intent.getStringExtra(ASSIGNMENT_PATH) ?: return
            val assignmentName = intent.getStringExtra(ASSIGNMENT_NAME) ?: return
            val dueIn = intent.getStringExtra(DUE_IN) ?: return

            notificationHandler.createNotificationChannel(context)
            notificationHandler.showNotification(context, assignmentId, assignmentPath, assignmentName, dueIn)
            goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                reminderDao.deletePastReminders(System.currentTimeMillis())
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "REMINDERS_CHANNEL_ID"
        const val ASSIGNMENT_ID = "ASSIGNMENT_ID"
        const val ASSIGNMENT_PATH = "ASSIGNMENT_PATH"
        const val ASSIGNMENT_NAME = "ASSIGNMENT_NAME"
        const val DUE_IN = "DUE_IN"
    }
}
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
            val contentId = intent.getLongExtra(CONTENT_ID, 0L)
            val htmlPath = intent.getStringExtra(HTML_PATH) ?: return
            val title = intent.getStringExtra(TITLE) ?: return
            val message = intent.getStringExtra(MESSAGE) ?: return

            notificationHandler.createNotificationChannel(context)
            notificationHandler.showNotification(context, contentId, htmlPath, title, message)
            val pendingTask = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                reminderDao.deletePastReminders(System.currentTimeMillis())
                pendingTask.finish()
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "REMINDERS_CHANNEL_ID"
        const val CONTENT_ID = "CONTENT_ID"
        const val HTML_PATH = "HTML_PATH"
        const val TITLE = "TITLE"
        const val MESSAGE = "MESSAGE"
    }
}
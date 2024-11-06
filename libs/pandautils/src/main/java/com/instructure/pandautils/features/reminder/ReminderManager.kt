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
package com.instructure.pandautils.features.reminder

import android.content.Context
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.pandautils.utils.toast
import java.util.Calendar

class ReminderManager(
    private val dateTimePicker: DateTimePicker,
    private val reminderRepository: ReminderRepository
) {
    suspend fun setReminder(
        context: Context,
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String
    ) {
        dateTimePicker.show(context).collect { selectedDateTime ->
            createReminder(context, selectedDateTime, userId, contentId, contentName, contentHtmlUrl)
        }
    }

    private suspend fun createReminder(
        context: Context,
        calendar: Calendar,
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String
    ) {
        val alarmTimeInMillis = calendar.timeInMillis
        if (reminderRepository.isReminderAlreadySetForTime(userId, contentId, calendar.timeInMillis)) {
            context.toast(R.string.reminderAlreadySet)
            return
        }

        if (alarmTimeInMillis < System.currentTimeMillis()) {
            context.toast(R.string.reminderInPast)
            return
        }

        val dateTimeString = calendar.time.toFormattedString()

        reminderRepository.createReminder(
            userId,
            contentId,
            contentName,
            contentHtmlUrl,
            dateTimeString,
            alarmTimeInMillis
        )
    }
}
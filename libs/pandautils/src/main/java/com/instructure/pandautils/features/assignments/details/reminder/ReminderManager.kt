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

import android.content.Context
import com.instructure.pandautils.R
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.pandautils.utils.toast
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderManager(
    private val reminderDao: ReminderDao,
    private val alarmScheduler: AlarmScheduler,
) {
    suspend fun setReminder(
        context: Context,
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String
    ) {
        val dateTimePicker = DateTimePicker()
        val selectedDateTime = dateTimePicker.show(context).first()

        createReminder(context, selectedDateTime, userId, contentId, contentName, contentHtmlUrl)
    }

    private suspend fun createReminder(
        context: Context,
        calendar: Calendar,
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String
    ) {
        val existingAlerts = reminderDao.findByAssignmentId(userId, contentId)
        val alarmTimeInMillis = calendar.timeInMillis

        if (alarmTimeInMillis < System.currentTimeMillis()) {
            context.toast(R.string.reminderInPast)
            return
        }

        if (existingAlerts.any { it.time == alarmTimeInMillis }) {
            context.toast(R.string.reminderAlreadySet)
            return
        }

        val dateTimeString = calendar.time.toFormattedString()

        val reminderId = reminderDao.insert(ReminderEntity(
            userId = userId,
            assignmentId = contentId,
            htmlUrl = contentHtmlUrl,
            name = contentName,
            text = dateTimeString,
            time = alarmTimeInMillis
        ))

        alarmScheduler.scheduleAlarm(
            contentId,
            contentHtmlUrl,
            contentName,
            dateTimeString,
            alarmTimeInMillis,
            reminderId
        )
    }
}
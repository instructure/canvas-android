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

import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val alarmScheduler: AlarmScheduler,
) {
    suspend fun createReminder(
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String,
        alarmText: String,
        alarmTimeInMillis: Long
    ) {
        val reminder = ReminderEntity(
            userId = userId,
            assignmentId = contentId,
            name = contentName,
            htmlUrl = contentHtmlUrl,
            text = alarmText,
            time = alarmTimeInMillis
        )
        val reminderId = reminderDao.insert(reminder)

        alarmScheduler.scheduleAlarm(
            contentId,
            contentHtmlUrl,
            contentName,
            alarmText,
            alarmTimeInMillis,
            reminderId
        )
    }

    suspend fun isReminderAlreadySetForTime(userId: Long, contentId: Long, alarmTimeInMillis: Long): Boolean {
        return getExistingReminders(userId, contentId).any { it.time == alarmTimeInMillis }
    }

    suspend fun deleteReminder(reminderId: Long) {
        reminderDao.deleteById(reminderId)
        alarmScheduler.cancelAlarm(reminderId)
    }

    private suspend fun getExistingReminders(userId: Long, contentId: Long): List<ReminderEntity> {
        return reminderDao.findByAssignmentId(userId, contentId)
    }
}
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

import androidx.lifecycle.LiveData
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import com.instructure.pandautils.utils.toFormattedString
import java.util.Date

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val alarmScheduler: AlarmScheduler,
) {
    suspend fun createReminder(
        userId: Long,
        contentId: Long,
        contentHtmlUrl: String,
        title: String,
        alarmText: String,
        alarmTimeInMillis: Long
    ) {
        val reminder = ReminderEntity(
            userId = userId,
            assignmentId = contentId,
            name = title,
            htmlUrl = contentHtmlUrl,
            text = Date(alarmTimeInMillis).toFormattedString(),
            time = alarmTimeInMillis
        )
        val reminderId = reminderDao.insert(reminder)

        alarmScheduler.scheduleAlarm(
            contentId,
            contentHtmlUrl,
            title,
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

    fun findByAssignmentIdLiveData(userId: Long, contentId: Long): LiveData<List<ReminderEntity>> {
        return reminderDao.findByAssignmentIdLiveData(userId, contentId)
    }
}
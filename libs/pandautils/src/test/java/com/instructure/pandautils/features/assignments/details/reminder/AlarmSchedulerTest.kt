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
import android.content.Context
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AlarmSchedulerTest {

    private val context: Context = mockk(relaxed = true)
    private val reminderDao: ReminderDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val alarmManager: AlarmManager = mockk(relaxed = true)

    @Before
    fun setup() {
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
    }

    @Test
    fun `Test schedule all alarms for the current user`() = runTest {
        val alarmScheduler = spyk(AlarmScheduler(context, reminderDao, apiPrefs))

        val reminder1 = ReminderEntity(1, 1, 1, "path1", "Assignment 1", "1 day", 12345678)
        val reminder2 = ReminderEntity(2, 1, 2, "path2", "Assignment 2", "2 hours", 12345678)

        every { apiPrefs.user } returns User(id = 1)
        coEvery { reminderDao.findByUserId(1) } returns listOf(reminder1, reminder2)

        coEvery { alarmScheduler.scheduleAlarm(any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { alarmScheduler.scheduleAllAlarmsForCurrentUser() } answers { callOriginal() }

        alarmScheduler.scheduleAllAlarmsForCurrentUser()

        coVerify {
            alarmScheduler.scheduleAlarm(reminder1.assignmentId, reminder1.htmlUrl, reminder1.name, reminder1.text, reminder1.time, reminder1.id)
            alarmScheduler.scheduleAlarm(reminder2.assignmentId, reminder2.htmlUrl, reminder2.name, reminder2.text, reminder2.time, reminder2.id)
        }
    }

    @Test
    fun `Test cancel all alarms for the current user`() = runTest {
        val alarmScheduler = spyk(AlarmScheduler(context, reminderDao, apiPrefs))

        val reminder1 = ReminderEntity(1, 1, 1, "path1", "Assignment 1", "1 day", 12345678)
        val reminder2 = ReminderEntity(2, 1, 2, "path2", "Assignment 2", "2 hours", 12345678)

        every { apiPrefs.user } returns User(id = 1)
        coEvery { reminderDao.findByUserId(1) } returns listOf(reminder1, reminder2)

        coEvery { alarmScheduler.cancelAlarm(any()) } just Runs
        coEvery { alarmScheduler.cancelAllAlarmsForCurrentUser() } answers { callOriginal() }

        alarmScheduler.cancelAllAlarmsForCurrentUser()

        coVerify {
            alarmScheduler.cancelAlarm(reminder1.id)
            alarmScheduler.cancelAlarm(reminder2.id)
        }
    }
}
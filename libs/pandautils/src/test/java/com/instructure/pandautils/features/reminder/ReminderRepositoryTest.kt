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
import com.instructure.pandautils.utils.toFormattedString
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class ReminderRepositoryTest {
    private val reminderDao: ReminderDao = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)
    private val reminderRepository = ReminderRepository(reminderDao, alarmScheduler)

    @Test
    fun `Test isReminderAlreadySetForTime returns true when reminder is already set`() = runTest {
        val existingReminders = listOf(
            ReminderEntity(1, 1, 1, "path1", "Assignment 1", "123", 123),
            ReminderEntity(2, 1, 1, "path1", "Assignment 1", "12345", 12345)
        )
        coEvery { reminderDao.findByAssignmentId(any(), any()) } returns existingReminders

        val isReminder1Exists = reminderRepository.isReminderAlreadySetForTime(1, 1, 123)
        val isReminder2Exists = reminderRepository.isReminderAlreadySetForTime(1, 1, 12345)

        assertTrue(isReminder1Exists)
        assertTrue(isReminder2Exists)
    }

    @Test
    fun `Test isReminderAlreadySetForTime returns false when reminder is not set`() = runTest {
        val existingReminders = listOf(
            ReminderEntity(1, 1, 1, "path1", "Assignment 1", "123", 123),
            ReminderEntity(2, 1, 1, "path1", "Assignment 1", "12345", 12345)
        )
        coEvery { reminderDao.findByAssignmentId(any(), any()) } returns existingReminders

        val isReminderExists = reminderRepository.isReminderAlreadySetForTime(1, 1, 1234)

        assertFalse(isReminderExists)
    }

    @Test
    fun `Test createReminder inserts reminder and schedules alarm`() = runTest {
        val reminder = ReminderEntity(0, 1, 1, "path1", "Assignment 1", Date(123).toFormattedString(), 123)
        coEvery { reminderDao.insert(reminder) } returns 1
        coEvery { alarmScheduler.scheduleAlarm(any(), any(), any(), any(), any(), any()) } returns Unit

        reminderRepository.createReminder(1, 1, "path1", "Assignment 1", "details", 123)

        coVerify { reminderDao.insert(reminder) }
        coVerify { alarmScheduler.scheduleAlarm(1, "path1", "Assignment 1", "details", 123, 1) }
    }
}
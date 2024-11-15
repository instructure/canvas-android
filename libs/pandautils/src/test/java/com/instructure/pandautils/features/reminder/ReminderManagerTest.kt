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
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Calendar

class ReminderManagerTest {
    private val dateTimePicker: DateTimePicker = mockk(relaxed = true)
    private val reminderRepository: ReminderRepository = mockk(relaxed = true)

    private val reminderManager = ReminderManager(dateTimePicker, reminderRepository)

    @Test
    fun `Test set custom reminder not creates reminder if no values was selected`() = runTest {
        coEvery { dateTimePicker.show(any()) } returns callbackFlow { close() }

        reminderManager.showCustomReminderDialog(mockk(), 1, 1, "Assignment 1", "path1", null)

        coVerify { reminderRepository wasNot Called }
    }

    @Test
    fun `Test set reminder creates reminder if values was selected`() = runTest {
        val context: Context = mockk(relaxed = true)
        val userId = 1L
        val contentId = 1L
        val contentName = "Assignment 1"
        val contentHtmlUrl = "path1"
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        coEvery { dateTimePicker.show(any()) } returns callbackFlow {
            trySend(calendar)
            close()
        }
        val title = "Reminder for $contentName"
        val message = "Reminder for $contentName with no due date"
        every { context.getString(R.string.reminderNotificationTitleFor, contentName) } returns title
        every { context.getString(R.string.reminderNotificationMessageWithoutDueDate, contentName) } returns message

        reminderManager.showCustomReminderDialog(context, userId, contentId, contentName, contentHtmlUrl, null)

        coVerify { reminderRepository.createReminder(userId, contentId, contentHtmlUrl, title, message, calendar.timeInMillis) }
    }
}
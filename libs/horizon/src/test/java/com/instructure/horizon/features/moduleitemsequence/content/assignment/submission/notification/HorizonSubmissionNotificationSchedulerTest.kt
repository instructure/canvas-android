/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.notification

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.notification.HorizonSubmissionNotificationScheduler.Companion.deadlineWorkName
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.date.DateTimeProvider
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class HorizonSubmissionNotificationSchedulerTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private val dateTimeProvider: DateTimeProvider = mockk(relaxed = true)
    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)

    private lateinit var scheduler: HorizonSubmissionNotificationScheduler

    @Before
    fun setUp() {
        every { dateTimeProvider.getCalendar() } answers {
            Calendar.getInstance().apply { timeInMillis = NOW }
        }
        scheduler = HorizonSubmissionNotificationScheduler(workManager, dateTimeProvider, createSubmissionDao)
    }

    @Test
    fun `schedules both reminders when due date is far enough out`() = runTest {
        scheduler.onOfflineSubmissionQueued(SUBMISSION_ID, assignmentDueIn(hours = 30))

        coVerify { createSubmissionDao.setNotifyOnResult(SUBMISSION_ID, true) }
        verify {
            workManager.enqueueUniqueWork(
                deadlineWorkName(SUBMISSION_ID, SubmissionDeadlineThreshold.T24H),
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
        verify {
            workManager.enqueueUniqueWork(
                deadlineWorkName(SUBMISSION_ID, SubmissionDeadlineThreshold.T1H),
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `schedules only the 1h reminder when due date is less than 24h out`() = runTest {
        scheduler.onOfflineSubmissionQueued(SUBMISSION_ID, assignmentDueIn(hours = 12))

        verify(exactly = 0) {
            workManager.enqueueUniqueWork(
                deadlineWorkName(SUBMISSION_ID, SubmissionDeadlineThreshold.T24H),
                any(),
                any<OneTimeWorkRequest>()
            )
        }
        verify {
            workManager.enqueueUniqueWork(
                deadlineWorkName(SUBMISSION_ID, SubmissionDeadlineThreshold.T1H),
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `does not schedule when due date is null but still flags for result notification`() = runTest {
        val assignment: Assignment = mockk()
        every { assignment.dueDate } returns null

        scheduler.onOfflineSubmissionQueued(SUBMISSION_ID, assignment)

        coVerify { createSubmissionDao.setNotifyOnResult(SUBMISSION_ID, true) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `does not schedule when due date is already in the past`() = runTest {
        scheduler.onOfflineSubmissionQueued(SUBMISSION_ID, assignmentDueIn(hours = -1))

        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `cancel cancels both reminder work names`() {
        scheduler.cancelDeadlineReminders(SUBMISSION_ID)

        verify { workManager.cancelUniqueWork(deadlineWorkName(SUBMISSION_ID, SubmissionDeadlineThreshold.T24H)) }
        verify { workManager.cancelUniqueWork(deadlineWorkName(SUBMISSION_ID, SubmissionDeadlineThreshold.T1H)) }
    }

    private fun assignmentDueIn(hours: Long): Assignment {
        val assignment: Assignment = mockk()
        every { assignment.dueDate } returns Date(NOW + TimeUnit.HOURS.toMillis(hours))
        return assignment
    }

    companion object {
        private const val SUBMISSION_ID = 42L
        private const val NOW = 1_700_000_000_000L
    }
}

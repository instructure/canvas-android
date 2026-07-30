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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionDeadlineReminderWorkerTest {

    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val notifier: HorizonSubmissionNotifier = mockk(relaxed = true)

    private val canvasContext = CanvasContext.emptyCourseContext(id = 1L)

    @Test
    fun `posts reminder when submission is still active`() = runTest {
        coEvery { createSubmissionDao.findSubmissionById(SUBMISSION_ID) } returns entity(SubmissionState.QUEUED)

        val result = buildWorker(SubmissionDeadlineThreshold.T24H).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify {
            notifier.postDeadlineReminder(
                SUBMISSION_ID,
                any(),
                10L,
                canvasContext.id,
                SubmissionDeadlineThreshold.T24H
            )
        }
    }

    @Test
    fun `skips reminder when submission row is gone (completed)`() = runTest {
        coEvery { createSubmissionDao.findSubmissionById(SUBMISSION_ID) } returns null

        val result = buildWorker(SubmissionDeadlineThreshold.T1H).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notifier.postDeadlineReminder(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `skips reminder when submission has failed`() = runTest {
        coEvery { createSubmissionDao.findSubmissionById(SUBMISSION_ID) } returns entity(SubmissionState.FAILED)

        val result = buildWorker(SubmissionDeadlineThreshold.T1H).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notifier.postDeadlineReminder(any(), any(), any(), any(), any()) }
    }

    private fun entity(state: SubmissionState) = CreateSubmissionEntity(
        id = SUBMISSION_ID,
        assignmentId = 10L,
        assignmentName = "Essay",
        canvasContext = canvasContext,
        submissionType = "online_text_entry",
        userId = 1L,
        submissionState = state,
        notifyOnResult = true
    )

    private fun buildWorker(threshold: SubmissionDeadlineThreshold): SubmissionDeadlineReminderWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val data = Data.Builder()
            .putLong(SubmissionDeadlineReminderWorker.KEY_SUBMISSION_ID, SUBMISSION_ID)
            .putString(SubmissionDeadlineReminderWorker.KEY_THRESHOLD, threshold.name)
            .build()
        return TestListenableWorkerBuilder<SubmissionDeadlineReminderWorker>(context)
            .setInputData(data)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters,
                ) = SubmissionDeadlineReminderWorker(appContext, workerParameters, createSubmissionDao, notifier)
            })
            .build()
    }

    companion object {
        private const val SUBMISSION_ID = 42L
    }
}

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
package com.instructure.pandautils.features.submission

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SubmissionUploadWorker.uploadText].
 *
 * The worker's [uploadText] method is tested directly (it is `internal`) to avoid
 * the heavy Hilt/WorkManager test harness. Worker construction uses mockk for the
 * Android framework parameters that are irrelevant to the logic under test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionUploadWorkerTest {

    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk()

    private lateinit var worker: SubmissionUploadWorker

    private val course = Course(id = 42L)
    private val submission = CreateSubmissionEntity(
        id = 1L,
        assignmentId = 99L,
        canvasContext = course,
        submissionType = "online_text_entry",
        userId = 7L,
        submissionEntry = "Hello world"
    )

    @Before
    fun setUp() {
        worker = SubmissionUploadWorker(
            context = context,
            workerParameters = workerParams,
            createSubmissionDao = createSubmissionDao,
            apiPrefs = apiPrefs,
            submissionApi = submissionApi
        )
    }

    @Test
    fun `uploadText success calls updateProgress(100) and deleteSubmissionById, returns Result_success`() = runTest {
        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Success(Submission(id = 1L))

        val result = worker.uploadText(submission)

        coVerify { createSubmissionDao.updateProgress(100.0, submission.id) }
        coVerify { createSubmissionDao.deleteSubmissionById(submission.id) }
        assertEquals(Result.success(), result)
    }

    @Test
    fun `uploadText API failure calls setSubmissionError(true) and returns Result_failure`() = runTest {
        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Fail()

        val result = worker.uploadText(submission)

        coVerify { createSubmissionDao.setSubmissionError(true, submission.id) }
        assertEquals(Result.failure(), result)
    }

    @Test
    fun `uploadText encodes submission text as UTF-8 before posting`() = runTest {
        val specialText = "Hello & world = <test>"
        val entityWithSpecialChars = submission.copy(submissionEntry = specialText)

        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Success(Submission(id = 1L))

        worker.uploadText(entityWithSpecialChars)

        // Verify that postTextSubmission was called with URL-encoded text (not raw)
        coVerify {
            submissionApi.postTextSubmission(
                contextId = course.id,
                assignmentId = entityWithSpecialChars.assignmentId,
                submissionType = any(),
                text = neq(specialText), // encoded, so it won't match the raw value
                restParams = any()
            )
        }
    }

    @Test
    fun `uploadText passes correct courseId and assignmentId to API`() = runTest {
        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Success(Submission(id = 1L))

        worker.uploadText(submission)

        coVerify {
            submissionApi.postTextSubmission(
                contextId = course.id,
                assignmentId = submission.assignmentId,
                submissionType = "online_text_entry",
                text = any(),
                restParams = any<RestParams>()
            )
        }
    }
}

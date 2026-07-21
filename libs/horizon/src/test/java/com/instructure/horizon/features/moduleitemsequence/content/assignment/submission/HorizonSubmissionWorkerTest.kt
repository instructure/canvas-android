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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.features.submission.SubmissionWorkerAction
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HorizonSubmissionWorkerTest {

    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)
    private val createFileSubmissionDao: CreateFileSubmissionDao = mockk(relaxed = true)
    private val fileUploadManager: FileUploadManager = mockk(relaxed = true)

    private val canvasContext = CanvasContext.emptyCourseContext(id = 1L)

    @Before
    fun setUp() {
        ContextKeeper.appContext = ApplicationProvider.getApplicationContext()
        mockkObject(CanvasRestAdapter)
        every { CanvasRestAdapter.clearCacheUrls(any()) } just runs
        every { apiPrefs.overrideDomains } returns mutableMapOf()
        coEvery { createSubmissionDao.findSubmissionById(SUBMISSION_ID) } returns submissionEntity()
    }

    @After
    fun tearDown() {
        unmockkObject(CanvasRestAdapter)
    }

    @Test
    fun `text submission success completes and deletes the submission`() = runTest {
        coEvery { submissionApi.postTextSubmission(any(), any(), any(), any(), any()) } returns
            DataResult.Success(Submission(id = 99L))

        val result = buildWorker(SubmissionWorkerAction.TEXT_ENTRY).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.COMPLETED, any()) }
        coVerify { createSubmissionDao.deleteSubmissionById(SUBMISSION_ID) }
    }

    @Test
    fun `transient server error retries and bumps retry count`() = runTest {
        coEvery { submissionApi.postTextSubmission(any(), any(), any(), any(), any()) } returns
            DataResult.Fail(Failure.Network("boom", errorCode = 500))

        val result = buildWorker(SubmissionWorkerAction.TEXT_ENTRY).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.RETRYING, any()) }
        coVerify { createSubmissionDao.incrementRetryCount(SUBMISSION_ID, any()) }
        coVerify(exactly = 0) { createSubmissionDao.deleteSubmissionById(any()) }
    }

    @Test
    fun `permanent client error fails and marks the submission failed without logging out`() = runTest {
        coEvery { submissionApi.postTextSubmission(any(), any(), any(), any(), any()) } returns
            DataResult.Fail(Failure.Network("bad request", errorCode = 400))

        val result = buildWorker(SubmissionWorkerAction.TEXT_ENTRY).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.FAILED, any()) }
        coVerify { createSubmissionDao.setSubmissionError(true, SUBMISSION_ID) }
    }

    @Test
    fun `transient error after exhausting attempts fails permanently`() = runTest {
        coEvery { submissionApi.postTextSubmission(any(), any(), any(), any(), any()) } returns
            DataResult.Fail(Failure.Network("boom", errorCode = 500))

        val result = buildWorker(SubmissionWorkerAction.TEXT_ENTRY, runAttemptCount = 5).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.FAILED, any()) }
    }

    @Test
    fun `file submission uploads files, submits and cleans up on success`() = runTest {
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(SUBMISSION_ID) } returns listOf(fileEntity())
        every { fileUploadManager.uploadFile(any(), any(), any()) } returns DataResult.Success(Attachment(id = 7L))
        coEvery { submissionApi.postSubmissionAttachments(any(), any(), any(), any(), any()) } returns
            DataResult.Success(Submission(id = 99L))

        val result = buildWorker(SubmissionWorkerAction.FILE_ENTRY).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.UPLOADING_FILES, any()) }
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.SUBMITTING, any()) }
        coVerify { createSubmissionDao.deleteSubmissionById(SUBMISSION_ID) }
    }

    @Test
    fun `file upload uses the token-safe replay contract`() = runTest {
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(SUBMISSION_ID) } returns listOf(fileEntity())
        val params = slot<RestParams>()
        every { fileUploadManager.uploadFile(any(), any(), capture(params)) } returns DataResult.Success(Attachment(id = 7L))
        coEvery { submissionApi.postSubmissionAttachments(any(), any(), any(), any(), any()) } returns
            DataResult.Success(Submission(id = 99L))

        buildWorker(SubmissionWorkerAction.FILE_ENTRY).doWork()

        assertTrue(params.captured.shouldRefreshToken)
        assertFalse(params.captured.shouldLoginOnTokenError)
    }

    @Test
    fun `file upload transient failure retries`() = runTest {
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(SUBMISSION_ID) } returns listOf(fileEntity())
        every { fileUploadManager.uploadFile(any(), any(), any()) } returns
            DataResult.Fail(Failure.Network("boom", errorCode = 503))

        val result = buildWorker(SubmissionWorkerAction.FILE_ENTRY).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify(exactly = 0) { submissionApi.postSubmissionAttachments(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `file upload permanent failure fails`() = runTest {
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(SUBMISSION_ID) } returns listOf(fileEntity())
        every { fileUploadManager.uploadFile(any(), any(), any()) } returns
            DataResult.Fail(Failure.Network("forbidden", errorCode = 403))

        val result = buildWorker(SubmissionWorkerAction.FILE_ENTRY).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.FAILED, any()) }
    }

    @Test
    fun `missing file data fails permanently`() = runTest {
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(SUBMISSION_ID) } returns
            listOf(fileEntity(fullPath = null))

        val result = buildWorker(SubmissionWorkerAction.FILE_ENTRY).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        coVerify { createSubmissionDao.updateSubmissionState(SUBMISSION_ID, SubmissionState.FAILED, any()) }
    }

    private fun submissionEntity() = CreateSubmissionEntity(
        id = SUBMISSION_ID,
        assignmentId = 10L,
        canvasContext = canvasContext,
        submissionType = "online_upload",
        userId = 1L,
        submissionEntry = "text"
    )

    private fun fileEntity(fullPath: String? = "/tmp/file.pdf") = CreateFileSubmissionEntity(
        id = 1L,
        dbSubmissionId = SUBMISSION_ID,
        name = "file.pdf",
        size = 100L,
        contentType = "application/pdf",
        fullPath = fullPath
    )

    private fun buildWorker(
        action: SubmissionWorkerAction,
        runAttemptCount: Int = 0,
    ): HorizonSubmissionWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val data = Data.Builder()
            .putString(Const.ACTION, action.name)
            .putLong(Const.SUBMISSION_ID, SUBMISSION_ID)
            .build()
        return TestListenableWorkerBuilder<HorizonSubmissionWorker>(context)
            .setInputData(data)
            .setRunAttemptCount(runAttemptCount)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters,
                ) = HorizonSubmissionWorker(
                    appContext,
                    workerParameters,
                    createSubmissionDao,
                    apiPrefs,
                    submissionApi,
                    createFileSubmissionDao,
                    fileUploadManager
                )
            })
            .build()
    }

    companion object {
        private const val SUBMISSION_ID = 42L
    }
}

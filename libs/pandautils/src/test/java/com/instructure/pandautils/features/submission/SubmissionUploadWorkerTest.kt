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
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SubmissionUploadWorker.uploadText] and [SubmissionUploadWorker.uploadFileSubmission].
 *
 * Both methods are `internal` and tested directly to avoid the heavy Hilt/WorkManager test
 * harness. [FileUploadManager] is a Kotlin object and is mocked with [mockkObject].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionUploadWorkerTest {

    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val createFileSubmissionDao: CreateFileSubmissionDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk()

    private lateinit var worker: SubmissionUploadWorker

    private val course = Course(id = 42L)
    private val textSubmission = CreateSubmissionEntity(
        id = 1L,
        assignmentId = 99L,
        canvasContext = course,
        submissionType = "online_text_entry",
        userId = 7L,
        submissionEntry = "Hello world"
    )
    private val fileSubmission = CreateSubmissionEntity(
        id = 2L,
        assignmentId = 99L,
        canvasContext = course,
        submissionType = "online_upload",
        userId = 7L,
    )

    @Before
    fun setUp() {
        mockkObject(FileUploadManager)
        worker = SubmissionUploadWorker(
            context = context,
            workerParameters = workerParams,
            createSubmissionDao = createSubmissionDao,
            createFileSubmissionDao = createFileSubmissionDao,
            apiPrefs = apiPrefs,
            submissionApi = submissionApi,
            fileUploadManager = FileUploadManager,
        )
    }

    @After
    fun tearDown() {
        unmockkObject(FileUploadManager)
    }

    // -------------------------------------------------------------------------
    // uploadText
    // -------------------------------------------------------------------------

    @Test
    fun `uploadText success calls updateProgress(100) and deleteSubmissionById, returns Result_success`() = runTest {
        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Success(Submission(id = 1L))

        val result = worker.uploadText(textSubmission)

        coVerify { createSubmissionDao.updateProgress(100.0, textSubmission.id) }
        coVerify { createSubmissionDao.deleteSubmissionById(textSubmission.id) }
        assertEquals(Result.success(), result)
    }

    @Test
    fun `uploadText API failure calls setSubmissionError(true) and returns Result_failure`() = runTest {
        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Fail()

        val result = worker.uploadText(textSubmission)

        coVerify { createSubmissionDao.setSubmissionError(true, textSubmission.id) }
        assertEquals(Result.failure(), result)
    }

    @Test
    fun `uploadText encodes submission text as UTF-8 before posting`() = runTest {
        val specialText = "Hello & world = <test>"
        val entityWithSpecialChars = textSubmission.copy(submissionEntry = specialText)

        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Success(Submission(id = 1L))

        worker.uploadText(entityWithSpecialChars)

        coVerify {
            submissionApi.postTextSubmission(
                contextId = course.id,
                assignmentId = entityWithSpecialChars.assignmentId,
                submissionType = any(),
                text = neq(specialText), // encoded — not the raw value
                restParams = any()
            )
        }
    }

    @Test
    fun `uploadText passes correct courseId and assignmentId to API`() = runTest {
        coEvery {
            submissionApi.postTextSubmission(any(), any(), any(), any(), any())
        } returns DataResult.Success(Submission(id = 1L))

        worker.uploadText(textSubmission)

        coVerify {
            submissionApi.postTextSubmission(
                contextId = course.id,
                assignmentId = textSubmission.assignmentId,
                submissionType = "online_text_entry",
                text = any(),
                restParams = any<RestParams>()
            )
        }
    }

    // -------------------------------------------------------------------------
    // uploadFileSubmission — success
    // -------------------------------------------------------------------------

    @Test
    fun `uploadFileSubmission success - uploads pending files, posts attachments, updateProgress(100), deletes, returns success`() = runTest {
        val pendingFile = pendingFileEntity(id = 10L, submissionId = fileSubmission.id)
        val attachment = Attachment(id = 999L)

        coEvery { createFileSubmissionDao.findFilesForSubmissionId(fileSubmission.id) } returns listOf(pendingFile)
        every {
            FileUploadManager.uploadFile(any(), any(), any())
        } returns DataResult.Success(attachment)
        coEvery { createFileSubmissionDao.setFileAttachmentIdAndError(attachment.id, false, null, pendingFile.id) } returns Unit
        coEvery {
            submissionApi.postSubmissionAttachments(course.id, fileSubmission.assignmentId, any(), listOf(attachment.id), any())
        } returns DataResult.Success(Submission(id = 1L))

        // findSubmissionById is called inside the progress listener cancel-check
        coEvery { createSubmissionDao.findSubmissionById(fileSubmission.id) } returns fileSubmission

        val result = worker.uploadFileSubmission(fileSubmission)

        coVerify { createSubmissionDao.updateProgress(100.0, fileSubmission.id) }
        coVerify { createSubmissionDao.deleteSubmissionById(fileSubmission.id) }
        assertEquals(Result.success(), result)
    }

    @Test
    fun `uploadFileSubmission - already-uploaded files are not re-uploaded`() = runTest {
        val alreadyUploaded = pendingFileEntity(id = 11L, submissionId = fileSubmission.id, attachmentId = 888L)
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(fileSubmission.id) } returns listOf(alreadyUploaded)
        coEvery {
            submissionApi.postSubmissionAttachments(course.id, fileSubmission.assignmentId, any(), listOf(888L), any())
        } returns DataResult.Success(Submission(id = 1L))
        coEvery { createSubmissionDao.findSubmissionById(fileSubmission.id) } returns fileSubmission

        worker.uploadFileSubmission(fileSubmission)

        // FileUploadManager.uploadFile must never be called for already-uploaded files
        verify(exactly = 0) { FileUploadManager.uploadFile(any(), any(), any()) }
    }

    // -------------------------------------------------------------------------
    // uploadFileSubmission — failure paths
    // -------------------------------------------------------------------------

    @Test
    fun `uploadFileSubmission - upload failure calls setSubmissionError(true) and setDraft(true), returns failure`() = runTest {
        val pendingFile = pendingFileEntity(id = 12L, submissionId = fileSubmission.id)

        coEvery { createFileSubmissionDao.findFilesForSubmissionId(fileSubmission.id) } returns listOf(pendingFile)
        every {
            FileUploadManager.uploadFile(any(), any(), any())
        } returns DataResult.Fail()
        coEvery { createSubmissionDao.findSubmissionById(fileSubmission.id) } returns fileSubmission

        val result = worker.uploadFileSubmission(fileSubmission)

        coVerify { createSubmissionDao.setSubmissionError(true, fileSubmission.id) }
        coVerify { createSubmissionDao.setDraft(fileSubmission.id, true) }
        assertEquals(Result.failure(), result)
    }

    @Test
    fun `uploadFileSubmission - postSubmissionAttachments failure calls setSubmissionError(true) and setDraft(true)`() = runTest {
        val pendingFile = pendingFileEntity(id = 13L, submissionId = fileSubmission.id)
        val attachment = Attachment(id = 777L)

        coEvery { createFileSubmissionDao.findFilesForSubmissionId(fileSubmission.id) } returns listOf(pendingFile)
        every { FileUploadManager.uploadFile(any(), any(), any()) } returns DataResult.Success(attachment)
        coEvery { createFileSubmissionDao.setFileAttachmentIdAndError(attachment.id, false, null, pendingFile.id) } returns Unit
        coEvery { createSubmissionDao.findSubmissionById(fileSubmission.id) } returns fileSubmission
        coEvery {
            submissionApi.postSubmissionAttachments(any(), any(), any(), any(), any())
        } returns DataResult.Fail()

        val result = worker.uploadFileSubmission(fileSubmission)

        coVerify { createSubmissionDao.setSubmissionError(true, fileSubmission.id) }
        coVerify { createSubmissionDao.setDraft(fileSubmission.id, true) }
        assertEquals(Result.failure(), result)
    }

    @Test
    fun `uploadFileSubmission - missing file fields calls setSubmissionError(true) and returns failure`() = runTest {
        val malformedFile = CreateFileSubmissionEntity(
            id = 14L,
            dbSubmissionId = fileSubmission.id,
            name = null, // missing required field
            size = null,
            contentType = null,
            fullPath = null,
        )
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(fileSubmission.id) } returns listOf(malformedFile)
        coEvery { createSubmissionDao.findSubmissionById(fileSubmission.id) } returns fileSubmission

        val result = worker.uploadFileSubmission(fileSubmission)

        coVerify { createSubmissionDao.setSubmissionError(true, fileSubmission.id) }
        assertEquals(Result.failure(), result)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun pendingFileEntity(
        id: Long,
        submissionId: Long,
        attachmentId: Long? = null,
    ) = CreateFileSubmissionEntity(
        id = id,
        dbSubmissionId = submissionId,
        attachmentId = attachmentId,
        name = "file_$id.pdf",
        size = 1024L,
        contentType = "application/pdf",
        fullPath = "/tmp/file_$id.pdf",
    )
}

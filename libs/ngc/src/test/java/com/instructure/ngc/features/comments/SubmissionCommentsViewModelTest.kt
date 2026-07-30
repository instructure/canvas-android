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
package com.instructure.ngc.features.comments

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.ngc.features.comments.domain.CompleteFileCommentUseCase
import com.instructure.ngc.features.comments.domain.CompleteMediaCommentUseCase
import com.instructure.ngc.features.comments.domain.CreatePendingFileCommentUseCase
import com.instructure.ngc.features.comments.domain.CreatePendingMediaCommentUseCase
import com.instructure.ngc.features.comments.domain.CreatePendingTextCommentUseCase
import com.instructure.ngc.features.comments.domain.GetPendingCommentRetryUseCase
import com.instructure.ngc.features.comments.domain.GetSubmissionCommentsUseCase
import com.instructure.ngc.features.comments.domain.InsertFileUploadInputUseCase
import com.instructure.ngc.features.comments.domain.MarkFileCommentFailedUseCase
import com.instructure.ngc.features.comments.domain.MarkPendingCommentFailedUseCase
import com.instructure.ngc.features.comments.domain.MarkPendingCommentSendingUseCase
import com.instructure.ngc.features.comments.domain.MarkSubmissionCommentsReadUseCase
import com.instructure.ngc.features.comments.domain.ObservePendingSubmissionCommentsUseCase
import com.instructure.ngc.features.comments.domain.PendingCommentRetryData
import com.instructure.ngc.features.comments.domain.PendingSubmissionCommentsData
import com.instructure.ngc.features.comments.domain.PrepareFileUploadRetryUseCase
import com.instructure.ngc.features.comments.domain.SaveCommentDraftUseCase
import com.instructure.ngc.features.comments.domain.SendTextCommentUseCase
import com.instructure.ngc.features.comments.domain.SubmissionCommentItem
import com.instructure.ngc.features.comments.domain.SubmissionCommentsResult
import com.instructure.pandautils.features.file.upload.FileUploadEvent
import com.instructure.pandautils.features.file.upload.FileUploadEventHandler
import com.instructure.pandautils.services.NotoriousUploadWorker
import com.instructure.pandautils.views.RecordingMediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionCommentsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context = mockk<android.content.Context>(relaxed = true)
    private val getSubmissionCommentsUseCase = mockk<GetSubmissionCommentsUseCase>(relaxed = true)
    private val markSubmissionCommentsReadUseCase = mockk<MarkSubmissionCommentsReadUseCase>(relaxed = true)
    private val observePendingSubmissionCommentsUseCase = mockk<ObservePendingSubmissionCommentsUseCase>(relaxed = true)
    private val saveCommentDraftUseCase = mockk<SaveCommentDraftUseCase>(relaxed = true)
    private val createPendingTextCommentUseCase = mockk<CreatePendingTextCommentUseCase>(relaxed = true)
    private val sendTextCommentUseCase = mockk<SendTextCommentUseCase>(relaxed = true)
    private val createPendingFileCommentUseCase = mockk<CreatePendingFileCommentUseCase>(relaxed = true)
    private val completeFileCommentUseCase = mockk<CompleteFileCommentUseCase>(relaxed = true)
    private val markFileCommentFailedUseCase = mockk<MarkFileCommentFailedUseCase>(relaxed = true)
    private val createPendingMediaCommentUseCase = mockk<CreatePendingMediaCommentUseCase>(relaxed = true)
    private val completeMediaCommentUseCase = mockk<CompleteMediaCommentUseCase>(relaxed = true)
    private val markPendingCommentFailedUseCase = mockk<MarkPendingCommentFailedUseCase>(relaxed = true)
    private val getPendingCommentRetryUseCase = mockk<GetPendingCommentRetryUseCase>(relaxed = true)
    private val prepareFileUploadRetryUseCase = mockk<PrepareFileUploadRetryUseCase>(relaxed = true)
    private val markPendingCommentSendingUseCase = mockk<MarkPendingCommentSendingUseCase>(relaxed = true)
    private val insertFileUploadInputUseCase = mockk<InsertFileUploadInputUseCase>(relaxed = true)
    private val apiPrefs = mockk<ApiPrefs>(relaxed = true)
    private val fileUploadEventHandler = mockk<FileUploadEventHandler>(relaxed = true)
    private val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val uploadEvents = MutableSharedFlow<FileUploadEvent>(replay = 0)

    private lateinit var viewModel: SubmissionCommentsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { apiPrefs.domain } returns "domain"
        every { apiPrefs.user } returns mockk(relaxed = true)
        every { observePendingSubmissionCommentsUseCase(any()) } returns flowOf(
            PendingSubmissionCommentsData(draftText = null, pendingComments = emptyList())
        )
        coEvery { fileUploadEventHandler.events } returns uploadEvents
        coEvery { getSubmissionCommentsUseCase(any()) } returns SubmissionCommentsResult(emptyList(), 500L)

        // enqueueUpload is a companion function that would otherwise touch WorkManager; stub it out.
        mockkObject(NotoriousUploadWorker.Companion)
        every {
            NotoriousUploadWorker.enqueueUpload(any(), any(), any(), any(), any(), any(), any(), any())
        } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel() {
        viewModel = SubmissionCommentsViewModel(
            context,
            getSubmissionCommentsUseCase,
            markSubmissionCommentsReadUseCase,
            observePendingSubmissionCommentsUseCase,
            saveCommentDraftUseCase,
            createPendingTextCommentUseCase,
            sendTextCommentUseCase,
            createPendingFileCommentUseCase,
            completeFileCommentUseCase,
            markFileCommentFailedUseCase,
            createPendingMediaCommentUseCase,
            completeMediaCommentUseCase,
            markPendingCommentFailedUseCase,
            getPendingCommentRetryUseCase,
            prepareFileUploadRetryUseCase,
            markPendingCommentSendingUseCase,
            insertFileUploadInputUseCase,
            apiPrefs,
            fileUploadEventHandler,
            crashlytics,
            testDispatcher
        )
    }

    @Test
    fun `load with no comments shows empty state`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `load populates comments`() = runTest(testDispatcher) {
        coEvery { getSubmissionCommentsUseCase(any()) } returns SubmissionCommentsResult(
            comments = listOf(SubmissionCommentItem(id = "1", content = "Hi", isOwnComment = false)),
            submissionId = 500L
        )
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEmpty)
        assertEquals(1, viewModel.uiState.value.comments.size)
    }

    @Test
    fun `load marks unread comments read`() = runTest(testDispatcher) {
        coEvery { getSubmissionCommentsUseCase(any()) } returns SubmissionCommentsResult(
            comments = listOf(
                SubmissionCommentItem(id = "2", content = "Feedback", isOwnComment = false, isUnread = true)
            ),
            submissionId = 500L
        )
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        coVerify(timeout = 1000) {
            markSubmissionCommentsReadUseCase(MarkSubmissionCommentsReadUseCase.Params(500L, listOf(2L)))
        }
    }

    @Test
    fun `sending a comment creates a pending comment then sends it through the use cases`() = runTest(testDispatcher) {
        coEvery { createPendingTextCommentUseCase(any()) } returns 10L
        coEvery { sendTextCommentUseCase(any()) } returns SubmissionCommentItem(
            id = "10",
            content = "Hello",
            isOwnComment = true
        )
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.uiState.value.actions.onCommentChanged("Hello")
        viewModel.uiState.value.actions.onSendComment()

        coVerify(timeout = 1000) {
            createPendingTextCommentUseCase(
                CreatePendingTextCommentUseCase.Params(pageId = "domain-3-1-2", comment = "Hello", attemptId = 1L)
            )
        }
        coVerify(timeout = 1000) {
            sendTextCommentUseCase(
                SendTextCommentUseCase.Params(pendingId = 10L, submissionId = 500L, comment = "Hello", attemptId = 1L)
            )
        }
    }

    @Test
    fun `retrying a failed text comment resends it through the use cases`() = runTest(testDispatcher) {
        coEvery { getPendingCommentRetryUseCase(GetPendingCommentRetryUseCase.Params(commentId = 5L)) } returns
            PendingCommentRetryData(comment = "Hello again", workerId = null, filePaths = null, filePath = null)
        coEvery { sendTextCommentUseCase(any()) } returns SubmissionCommentItem(
            id = "5",
            content = "Hello again",
            isOwnComment = true
        )
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.uiState.value.actions.onRetryComment(
            SubmissionCommentItem(id = "5", content = "Hello again", isFailed = true)
        )

        coVerify(timeout = 1000) {
            sendTextCommentUseCase(
                SendTextCommentUseCase.Params(pendingId = 5L, submissionId = 500L, comment = "Hello again", attemptId = 1L)
            )
        }
    }

    @Test
    fun `load failure surfaces the error message and stops loading`() = runTest(testDispatcher) {
        coEvery { getSubmissionCommentsUseCase(any()) } throws RuntimeException("boom")
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("boom", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `changing the comment updates the input text`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.uiState.value.actions.onCommentChanged("Draft in progress")

        assertEquals("Draft in progress", viewModel.uiState.value.commentText)
    }

    @Test
    fun `a restored draft is shown in the input`() = runTest(testDispatcher) {
        every { observePendingSubmissionCommentsUseCase(any()) } returns flowOf(
            PendingSubmissionCommentsData(draftText = "Saved draft", pendingComments = emptyList())
        )
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        assertEquals("Saved draft", viewModel.uiState.value.commentText)
    }

    @Test
    fun `pending comments are appended to the list`() = runTest(testDispatcher) {
        every { observePendingSubmissionCommentsUseCase(any()) } returns flowOf(
            PendingSubmissionCommentsData(
                draftText = null,
                pendingComments = listOf(
                    SubmissionCommentItem(id = "p1", content = "Sending", isOwnComment = true, isPending = true)
                )
            )
        )
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEmpty)
        assertEquals(1, viewModel.uiState.value.comments.size)
        assertEquals("p1", viewModel.uiState.value.comments.single().id)
    }

    @Test
    fun `onAddAttachment shows the attachment type dialog and dismiss hides it`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.uiState.value.actions.onAddAttachment()
        assertTrue(viewModel.uiState.value.showAttachmentTypeDialog)

        viewModel.uiState.value.actions.onAttachmentTypeDialogDismiss()
        assertFalse(viewModel.uiState.value.showAttachmentTypeDialog)
    }

    @Test
    fun `onChooseFiles opens the file selector for the current ids and closes the type dialog`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()
        viewModel.uiState.value.actions.onAddAttachment()

        viewModel.uiState.value.actions.onChooseFiles()

        assertFalse(viewModel.uiState.value.showAttachmentTypeDialog)
        assertEquals(
            FileSelectorDialogData(assignmentId = 1L, courseId = 3L, userId = 2L, attempt = 1L),
            viewModel.uiState.value.fileSelectorDialogData
        )
    }

    @Test
    fun `record audio and video set the floating recorder and dismiss clears it`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.uiState.value.actions.onRecordAudio()
        assertEquals(RecordingMediaType.Audio, viewModel.uiState.value.showRecordFloatingView)

        viewModel.uiState.value.actions.onRecordVideo()
        assertEquals(RecordingMediaType.Video, viewModel.uiState.value.showRecordFloatingView)

        viewModel.uiState.value.actions.onRecordDialogDismiss()
        assertNull(viewModel.uiState.value.showRecordFloatingView)
    }

    @Test
    fun `recording a media comment creates a pending comment and enqueues the upload`() = runTest(testDispatcher) {
        // Uri.fromFile is an un-mocked Android framework call reached while enqueuing the recording.
        mockkStatic(Uri::class)
        every { Uri.fromFile(any()) } returns mockk { every { path } returns "/tmp/recording.mp4" }
        coEvery { createPendingMediaCommentUseCase(any()) } returns 20L
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.uiState.value.actions.onMediaRecorded(File("/tmp/recording.mp4"))
        advanceUntilIdle()

        coVerify(timeout = 1000) {
            createPendingMediaCommentUseCase(
                CreatePendingMediaCommentUseCase.Params(
                    pageId = "domain-3-1-2",
                    filePath = "/tmp/recording.mp4",
                    attemptId = 1L
                )
            )
        }
        coVerify(timeout = 1000) {
            NotoriousUploadWorker.enqueueUpload(any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a running file upload creates a pending file comment`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        val liveData = MutableLiveData<WorkInfo?>()
        val workInfo = mockk<WorkInfo>(relaxed = true) {
            every { state } returns WorkInfo.State.RUNNING
            every { id } returns UUID.randomUUID()
        }
        liveData.value = workInfo
        uploadEvents.emit(FileUploadEvent.UploadStarted(uuid = null, workInfoLiveData = liveData, filePaths = listOf("/a.pdf")))
        advanceUntilIdle()

        coVerify(timeout = 1000) { createPendingFileCommentUseCase(any()) }
    }

    @Test
    fun `a succeeded file upload completes the file comment`() = runTest(testDispatcher) {
        coEvery { completeFileCommentUseCase(any()) } returns SubmissionCommentItem(
            id = "f1",
            content = "",
            isOwnComment = true
        )
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        val liveData = MutableLiveData<WorkInfo?>()
        val workInfo = mockk<WorkInfo>(relaxed = true) {
            every { state } returns WorkInfo.State.SUCCEEDED
            every { id } returns UUID.randomUUID()
            every { outputData.getLong(any(), any()) } returns 111L
        }
        liveData.value = workInfo
        uploadEvents.emit(FileUploadEvent.UploadStarted(uuid = null, workInfoLiveData = liveData, filePaths = listOf("/a.pdf")))
        advanceUntilIdle()

        coVerify(timeout = 1000) { completeFileCommentUseCase(any()) }
    }

    @Test
    fun `a failed file upload marks the file comment failed`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        val liveData = MutableLiveData<WorkInfo?>()
        val workInfo = mockk<WorkInfo>(relaxed = true) {
            every { state } returns WorkInfo.State.FAILED
            every { id } returns UUID.randomUUID()
        }
        liveData.value = workInfo
        uploadEvents.emit(FileUploadEvent.UploadStarted(uuid = null, workInfoLiveData = liveData, filePaths = listOf("/a.pdf")))
        advanceUntilIdle()

        coVerify(timeout = 1000) { markFileCommentFailedUseCase(any()) }
    }

    @Test
    fun `selecting a new attempt reloads the comments for that attempt`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 2L)
        advanceUntilIdle()

        coVerify {
            getSubmissionCommentsUseCase(
                GetSubmissionCommentsUseCase.Params(
                    courseId = 3L,
                    studentId = 2L,
                    assignmentId = 1L,
                    selectedAttemptId = 2L
                )
            )
        }
    }

    @Test
    fun `reloading with the same attempt does not fetch again`() = runTest(testDispatcher) {
        createViewModel()
        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        viewModel.load(courseId = 3L, assignmentId = 1L, studentId = 2L, attemptId = 1L)
        advanceUntilIdle()

        coVerify(exactly = 1) { getSubmissionCommentsUseCase(any()) }
    }
}

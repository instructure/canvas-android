/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader.grade.comments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.file.upload.FileUploadEventHandler
import com.instructure.pandautils.features.speedgrader.SpeedGraderSelectedAttemptHolder
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.PendingSubmissionCommentWithFileUploadInput
import com.instructure.pandautils.views.RecordingMediaType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SpeedGraderCommentsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val savedStateHandle = SavedStateHandle(
        mapOf(
            SpeedGraderCommentsViewModel.ASSIGNMENT_ID_KEY to 1L,
            SpeedGraderCommentsViewModel.SUBMISSION_ID_KEY to 2L,
            "courseId" to 3L
        )
    )
    private val context = mockk<android.content.Context>(relaxed = true)
    private val repository = mockk<SpeedGraderCommentsRepository>(relaxed = true)
    private val pendingSubmissionCommentDao = mockk<PendingSubmissionCommentDao>(relaxed = true)
    private val fileUploadInputDao = mockk<FileUploadInputDao>(relaxed = true)
    private val submissionCommentDao = mockk<SubmissionCommentDao>(relaxed = true)
    private val attachmentDao = mockk<AttachmentDao>(relaxed = true)
    private val authorDao = mockk<AuthorDao>(relaxed = true)
    private val mediaCommentDao = mockk<MediaCommentDao>(relaxed = true)
    private val apiPrefs = mockk<ApiPrefs>(relaxed = true)
    private val selectedAttemptHolder = mockk<SpeedGraderSelectedAttemptHolder>(relaxed = true)
    private val fileUploadEventHandler = mockk<FileUploadEventHandler>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: SpeedGraderCommentsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { apiPrefs.domain } returns "domain"
        every { apiPrefs.user } returns mockk(relaxed = true)
        coEvery { selectedAttemptHolder.selectedAttemptIdFlowFor(any()) } returns MutableStateFlow(
            1L
        )
        coEvery { repository.getCourseFeatures(any()) } returns emptyList()
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns mockk {
            every { data.submission } returns null
            every { comments } returns emptyList()
        }
        coEvery { fileUploadEventHandler.events } returns MutableSharedFlow(replay = 0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = SpeedGraderCommentsViewModel(
            savedStateHandle,
            context,
            repository,
            pendingSubmissionCommentDao,
            fileUploadInputDao,
            submissionCommentDao,
            attachmentDao,
            authorDao,
            mediaCommentDao,
            apiPrefs,
            selectedAttemptHolder,
            fileUploadEventHandler
        )
    }

    @Test
    fun `fetchData updates uiState with empty state when api returns no comments`() = runTest {
        createViewModel()
        Assert.assertFalse(viewModel.uiState.value.isLoading)
        Assert.assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `fetchData updates uiState with comments`() = runTest {
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns mockk {
            every { data.submission } returns null
            every { comments } returns listOf(
                SubmissionCommentsQuery.Node1(
                    comment = "Test comment",
                    author = SubmissionCommentsQuery.Author(
                        _id = "1",
                        name = "Author",
                        avatarUrl = "avatarUrl",
                        email = "email@asd.com",
                        pronouns = "they/them"
                    ),
                    mediaObject = null,
                    createdAt = java.util.Date(),
                    canReply = true,
                    draft = false,
                    attempt = 1,
                    read = true,
                    attachments = null,
                    mediaCommentId = null
                )
            )
        }

        createViewModel()
        Thread.sleep(100)

        assert(!viewModel.uiState.value.isLoading)
        assert(!viewModel.uiState.value.isEmpty)
        assertEquals(1, viewModel.uiState.value.comments.size)
    }

    @Test
    fun `fetchData updates uiState with comments and pending comments`() = runTest {
        // Simulate fetchData
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns mockk {
            every { data.submission } returns null
            every { comments } returns listOf(
                SubmissionCommentsQuery.Node1(
                    comment = "Test comment",
                    author = SubmissionCommentsQuery.Author(
                        _id = "1",
                        name = "Author",
                        avatarUrl = "avatarUrl",
                        email = "email@asd.com",
                        pronouns = "they/them"
                    ),
                    mediaObject = null,
                    createdAt = java.util.Date(),
                    canReply = true,
                    draft = false,
                    attempt = 1,
                    read = true,
                    attachments = null,
                    mediaCommentId = null
                )
            )
        }

        val flow = flowOf(
            listOf(
                PendingSubmissionCommentWithFileUploadInput(
                    pendingSubmissionCommentEntity = PendingSubmissionCommentEntity(
                        pageId = "pageId",
                        comment = "Pending comment",
                        status = "SENDING"
                    ),
                    fileUploadInput = null
                )
            )
        )
        coEvery { pendingSubmissionCommentDao.findByPageIdFlow(any()) } returns flow

        createViewModel()
        Thread.sleep(100)

        assert(!viewModel.uiState.value.isLoading)
        assert(!viewModel.uiState.value.isEmpty)
        assertEquals(2, viewModel.uiState.value.comments.size)
    }

    @Test
    fun `handleAction CommentFieldChanged updates commentText`() = runTest {
        val comment = "Test comment"
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.CommentFieldChanged(comment))
        assertEquals(comment, viewModel.uiState.value.commentText)
    }

    @Test
    fun `handleAction SendCommentClicked clears commentText`() = runTest {
        val comment = "Send this"
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.CommentFieldChanged(comment))
        viewModel.handleAction(SpeedGraderCommentsAction.SendCommentClicked)
        Thread.sleep(100)
        assertEquals("", viewModel.uiState.value.commentText)
    }

    @Test
    fun `handleAction AddAttachmentClicked updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)

        assert(viewModel.uiState.value.showAttachmentTypeDialog)
    }

    @Test
    fun `handleAction AttachmentTypeSelectorDialogClosed updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.AttachmentTypeSelectorDialogClosed)

        assert(!viewModel.uiState.value.showAttachmentTypeDialog)
    }

    @Test
    fun `handleAction ChooseFilesClicked updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.ChooseFilesClicked)

        assert(!viewModel.uiState.value.showAttachmentTypeDialog)
        assert(viewModel.uiState.value.fileSelectorDialogData != null)
    }

    @Test
    fun `handleAction FileUploadDialogClosed updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.ChooseFilesClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.FileUploadDialogClosed)

        assert(!viewModel.uiState.value.showAttachmentTypeDialog)
        assert(viewModel.uiState.value.fileSelectorDialogData == null)
    }

    @Test
    fun `handleAction RecordAudioClicked updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.RecordAudioClicked)

        assert(!viewModel.uiState.value.showAttachmentTypeDialog)
        assertEquals(RecordingMediaType.Audio, viewModel.uiState.value.showRecordFloatingView)
    }

    @Test
    fun `handleAction RecordVideoClicked updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.RecordVideoClicked)

        assert(!viewModel.uiState.value.showAttachmentTypeDialog)
        assertEquals(RecordingMediaType.Video, viewModel.uiState.value.showRecordFloatingView)
    }

    @Test
    fun `handleAction AttachmentRecordDialogClosed updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.RecordVideoClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.AttachmentRecordDialogClosed)

        assert(!viewModel.uiState.value.showAttachmentTypeDialog)
        assert(viewModel.uiState.value.showRecordFloatingView == null)
    }

    @Test
    fun `handleAction FileUploadStarted updates uiState`() = runTest {
        createViewModel()
        viewModel.handleAction(SpeedGraderCommentsAction.AddAttachmentClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.ChooseFilesClicked)
        viewModel.handleAction(SpeedGraderCommentsAction.FileUploadStarted(mockk(relaxed = true)))

        assert(!viewModel.uiState.value.showAttachmentTypeDialog)
        assert(viewModel.uiState.value.fileSelectorDialogData == null)
    }

    @Test
    fun `comments for no attempt is shown for the first attempt`() = runTest {
        coEvery { repository.getCourseFeatures(any()) } returns listOf("assignments_2_student")
        val attemptFlow = MutableStateFlow(1L)
        coEvery { selectedAttemptHolder.selectedAttemptIdFlowFor(any()) } returns attemptFlow
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns mockk {
            every { data.submission } returns null
            every { comments } returns listOf(
                SubmissionCommentsQuery.Node1(
                    comment = "Comment for no attempt",
                    author = SubmissionCommentsQuery.Author(
                        _id = "1",
                        name = "Author",
                        avatarUrl = "avatarUrl",
                        email = "email@asd.com",
                        pronouns = "they/them"
                    ),
                    mediaObject = null,
                    createdAt = java.util.Date(),
                    canReply = true,
                    draft = false,
                    attempt = 0,
                    read = true,
                    attachments = null,
                    mediaCommentId = null
                )
            )
        }

        createViewModel()
        Thread.sleep(100)

        assertEquals(1, viewModel.uiState.value.comments.size)
    }

    @Test
    fun `comments for no attempt don't show for non first attempt`() = runTest {
        coEvery { repository.getCourseFeatures(any()) } returns listOf("assignments_2_student")
        val attemptFlow = MutableStateFlow(2L)
        coEvery { selectedAttemptHolder.selectedAttemptIdFlowFor(any()) } returns attemptFlow
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns mockk {
            every { data.submission } returns null
            every { comments } returns listOf(
                SubmissionCommentsQuery.Node1(
                    comment = "Comment for no attempt",
                    author = SubmissionCommentsQuery.Author(
                        _id = "1",
                        name = "Author",
                        avatarUrl = "avatarUrl",
                        email = "email@asd.com",
                        pronouns = "they/them"
                    ),
                    mediaObject = null,
                    createdAt = java.util.Date(),
                    canReply = true,
                    draft = false,
                    attempt = 0,
                    read = true,
                    attachments = null,
                    mediaCommentId = null
                )
            )
        }

        createViewModel()
        Thread.sleep(100)
        assertEquals(0, viewModel.uiState.value.comments.size)
    }
}
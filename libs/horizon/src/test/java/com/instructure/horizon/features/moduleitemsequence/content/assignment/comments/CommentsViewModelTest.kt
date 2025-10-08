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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.comments

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.ui.text.input.TextFieldValue
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.instructure.canvasapi2.managers.Comment
import com.instructure.canvasapi2.managers.CommentsData
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.R
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CommentsViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: CommentsRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val fileDownloadProgressDao: FileDownloadProgressDao = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val assignmentId = 1L
    private val courseId = 100L
    private val attempt = 1
    private val userId = 123L

    private val testCommentsData = CommentsData(
        comments = listOf(
            Comment(
                authorId = userId,
                authorName = "Test User",
                createdAt = Date(),
                commentText = "Test comment",
                read = true,
                attachments = emptyList()
            )
        ),
        endCursor = "cursor1",
        startCursor = null,
        hasNextPage = true,
        hasPreviousPage = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(DateFormat::class)
        every { DateFormat.is24HourFormat(any()) } returns false
        every { DateFormat.getBestDateTimePattern(any(), any()) } returns ""
        every { apiPrefs.user } returns User(id = userId, name = "Test User")
        coEvery { repository.getComments(any(), any(), any(), any(), any(), any()) } returns testCommentsData
        coEvery { repository.postComment(any(), any(), any(), any(), any()) } returns DataResult.Success(mockk(relaxed = true))
        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(null)
        coEvery { fileDownloadProgressDao.deleteByWorkerId(any()) } returns Unit
        every { workManager.enqueue(any<WorkRequest>()) } returns mockk {
            every { result } returns mockk()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test init with attempt loads comments`() = runTest {
        val viewModel = getViewModel()

        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        assertFalse(viewModel.uiState.value.loading)
        assertEquals(1, viewModel.uiState.value.comments.size)
        assertEquals("Test comment", viewModel.uiState.value.comments.first().commentText)
        coVerify { repository.getComments(assignmentId, userId, attempt, false, null, null) }
    }

    @Test
    fun `Test comments are mapped with user info`() = runTest {
        val viewModel = getViewModel()

        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        val comment = viewModel.uiState.value.comments.first()
        assertEquals("Test User", comment.title)
        assertTrue(comment.fromCurrentUser)
        assertTrue(comment.read)
    }

    @Test
    fun `Test paging controls visibility`() = runTest {
        val viewModel = getViewModel()

        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        assertTrue(viewModel.uiState.value.showPagingControls)
        assertTrue(viewModel.uiState.value.nextPageEnabled)
        assertFalse(viewModel.uiState.value.previousPageEnabled)
    }

    @Test
    fun `Test load next page`() = runTest {
        val nextPageData = testCommentsData.copy(
            comments = listOf(
                Comment(
                    authorId = userId,
                    authorName = "Test User",
                    createdAt = Date(),
                    commentText = "Next page comment",
                    read = true,
                    attachments = emptyList()
                )
            ),
            hasNextPage = false,
            hasPreviousPage = true
        )
        coEvery { repository.getComments(any(), any(), any(), any(), any(), any(), any()) } returns testCommentsData andThen nextPageData

        val viewModel = getViewModel()
        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        viewModel.uiState.value.onNextPageClicked()

        assertEquals("Next page comment", viewModel.uiState.value.comments.first().commentText)
        coVerify { repository.getComments(assignmentId, userId, attempt, any(), null, "cursor1", any()) }
    }

    @Test
    fun `Test load previous page`() = runTest {
        val firstPageData = testCommentsData.copy(hasPreviousPage = true)
        coEvery { repository.getComments(any(), any(), any(), any(), any(), any()) } returns firstPageData andThen testCommentsData

        val viewModel = getViewModel()
        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        viewModel.uiState.value.onPreviousPageClicked()

        coVerify { repository.getComments(assignmentId, userId, attempt, false, null, null) }
    }

    @Test
    fun `Test comment text change updates state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onCommentChanged(TextFieldValue("New comment"))

        assertEquals("New comment", viewModel.uiState.value.comment.text)
    }

    @Test
    fun `Test post comment with valid text`() {
        val viewModel = getViewModel()
        viewModel.initWithAttempt(assignmentId, attempt, courseId)
        viewModel.uiState.value.onCommentChanged(TextFieldValue("New comment"))

        viewModel.uiState.value.onPostClicked()

        coVerify { repository.postComment(courseId, assignmentId, userId, attempt, "New comment") }
        assertEquals("", viewModel.uiState.value.comment.text)
        assertFalse(viewModel.uiState.value.postingComment)
    }

    @Test
    fun `Test post comment with blank text does nothing`() = runTest {
        val viewModel = getViewModel()
        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        viewModel.uiState.value.onPostClicked()

        coVerify(exactly = 0) { repository.postComment(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test post comment error shows message`() = runTest {
        coEvery { repository.postComment(any(), any(), any(), any(), any()) } throws Exception("Post error")

        val viewModel = getViewModel()
        viewModel.initWithAttempt(assignmentId, attempt, courseId)
        viewModel.uiState.value.onCommentChanged(TextFieldValue("New comment"))

        viewModel.uiState.value.onPostClicked()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.postingComment)
    }

    @Test
    fun `Test error dismissed clears message`() = runTest {
        coEvery { repository.getComments(any(), any(), any(), any(), any(), any()) } throws Exception("Load error")

        val viewModel = getViewModel()
        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        viewModel.uiState.value.onErrorDismissed()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `Test file opened clears file path`() = runTest {
        val completedEntity = FileDownloadProgressEntity(
            workerId = "worker-id",
            progressState = FileDownloadProgressState.COMPLETED,
            progress = 100,
            filePath = "/path/to/file",
            fileName = "fileName"
        )
        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(completedEntity)

        val viewModel = getViewModel()

        viewModel.uiState.value.onFileOpened()

        assertNull(viewModel.uiState.value.filePathToOpen)
        assertNull(viewModel.uiState.value.mimeTypeToOpen)
    }

    @Test
    fun `Test load comments error shows message`() = runTest {
        coEvery { repository.getComments(any(), any(), any(), any(), any(), any()) } throws Exception("Load error")

        val viewModel = getViewModel()

        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        assertFalse(viewModel.uiState.value.loading)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `Test attempt 0 shows no subtitle`() = runTest {
        val viewModel = getViewModel()

        viewModel.initWithAttempt(assignmentId, 0, courseId)

        val comment = viewModel.uiState.value.comments.first()
        assertEquals("", comment.subtitle)
    }

    @Test
    fun `Test non-zero attempt shows subtitle`() = runTest {
        every { context.getString(R.string.commentsBottomSheet_attempt, any()) } returns "Attempt $attempt"
        val viewModel = getViewModel()

        viewModel.initWithAttempt(assignmentId, 2, courseId)

        val comment = viewModel.uiState.value.comments.first()
        assertTrue(comment.subtitle.isNotEmpty())
    }

    @Test
    fun `Test comment from different user`() = runTest {
        val otherUserComment = testCommentsData.copy(
            comments = listOf(
                Comment(
                    authorId = 999L,
                    authorName = "Other User",
                    createdAt = Date(),
                    commentText = "Other comment",
                    read = false,
                    attachments = emptyList()
                )
            )
        )
        coEvery { repository.getComments(any(), any(), any(), any(), any(), any(), any()) } returns otherUserComment

        val viewModel = getViewModel()
        viewModel.initWithAttempt(assignmentId, attempt, courseId)

        val comment = viewModel.uiState.value.comments.first()
        assertFalse(comment.fromCurrentUser)
        assertFalse(comment.read)
    }

    private fun getViewModel(): CommentsViewModel {
        return CommentsViewModel(
            context,
            repository,
            apiPrefs,
            workManager,
            fileDownloadProgressDao
        )
    }
}

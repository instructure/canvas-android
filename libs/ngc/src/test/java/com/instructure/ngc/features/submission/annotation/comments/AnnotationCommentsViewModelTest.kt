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
package com.instructure.ngc.features.submission.annotation.comments

import android.content.Context
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.SubmissionDateFormatter
import com.instructure.ngc.features.submission.annotation.CommentThreadRequest
import com.instructure.ngc.features.submission.annotation.CommentThreadResult
import com.instructure.ngc.features.submission.domain.AddAnnotationCommentReplyUseCase
import com.instructure.ngc.features.submission.domain.DeleteAnnotationCommentReplyUseCase
import com.instructure.ngc.features.submission.domain.EditAnnotationCommentReplyUseCase
import com.instructure.ngc.features.submission.domain.GetAnnotationCommentRepliesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnnotationCommentsViewModelTest {

    private val getAnnotationCommentRepliesUseCase: GetAnnotationCommentRepliesUseCase = mockk()
    private val addAnnotationCommentReplyUseCase: AddAnnotationCommentReplyUseCase = mockk()
    private val editAnnotationCommentReplyUseCase: EditAnnotationCommentReplyUseCase = mockk()
    private val deleteAnnotationCommentReplyUseCase: DeleteAnnotationCommentReplyUseCase = mockk()
    private val context: Context = mockk()
    private val dateFormatter = object : SubmissionDateFormatter {
        override fun formatDateTime(context: Context, date: java.util.Date) = "formatted-date"
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    private val headAnnotationId = "head1"
    private val sessionId = "session1"
    private val canvaDocsDomain = "https://canvadocs.example.com"
    private val documentId = "doc1"
    // The canvadocs-session-scoped AnnotationMetadata.userId threaded through CommentThreadRequest —
    // ownReply()'s userId matches this by default. Deliberately NOT ApiPrefs.user.id: the "own reply" key
    // must come from this nav arg (see AnnotationCommentsViewModel.currentUserId), not ApiPrefs.
    private val currentUserId = "42"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(R.string.ngc_annotationComments_loadError) } returns "Failed to load comments"
        every { context.getString(R.string.ngc_annotationComments_actionError) } returns "Something went wrong"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun ownReply(id: String, contents: String = "hello", page: Int = 3) = CanvaDocAnnotation(
        annotationId = id,
        page = page,
        isEditable = true,
        userId = "42",
        userName = "Current User",
        createdAt = "2026-07-24T10:00:00.000Z",
        contents = contents,
        annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
        inReplyTo = headAnnotationId,
    )

    private fun otherReply(id: String, contents: String = "hi there", page: Int = 3) = CanvaDocAnnotation(
        annotationId = id,
        page = page,
        isEditable = true,
        userId = "99",
        userName = "Someone Else",
        createdAt = "2026-07-24T09:00:00.000Z",
        contents = contents,
        annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
        inReplyTo = headAnnotationId,
    )

    /**
     * Builds the ViewModel and binds it to a thread. The params are supplied via [load] rather than a
     * SavedStateHandle now that the thread is a Tray over the submission pane rather than its own nav
     * destination (see AnnotationCommentsBottomSheet).
     */
    private fun createViewModel(
        canWrite: Boolean = true,
        canManage: Boolean = false,
        canComment: Boolean = true,
        currentUserId: String = this.currentUserId,
    ) =
        AnnotationCommentsViewModel(
            context = context,
            getAnnotationCommentRepliesUseCase = getAnnotationCommentRepliesUseCase,
            addAnnotationCommentReplyUseCase = addAnnotationCommentReplyUseCase,
            editAnnotationCommentReplyUseCase = editAnnotationCommentReplyUseCase,
            deleteAnnotationCommentReplyUseCase = deleteAnnotationCommentReplyUseCase,
            dateFormatter = dateFormatter,
        ).apply {
            load(
                CommentThreadRequest(
                    headAnnotationId = headAnnotationId,
                    sessionId = sessionId,
                    canvaDocsDomain = canvaDocsDomain,
                    documentId = documentId,
                    canWrite = canWrite,
                    canManage = canManage,
                    canComment = canComment,
                    currentUserId = currentUserId,
                )
            )
        }

    @Test
    fun `loadReplies success populates uiState with own and other replies gated correctly`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(otherReply("r1"), ownReply("r2"))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertNull(state.loadError)
        assertEquals(2, state.replies.size)
        assertFalse(state.replies[0].canEdit)
        assertFalse(state.replies[0].canDelete)
        assertTrue(state.replies[1].canEdit)
        assertTrue(state.replies[1].canDelete)
    }

    @Test
    fun `loadReplies failure surfaces loadError and a working retry`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } throws RuntimeException("network")

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.loading.not())
        assertEquals("Failed to load comments", vm.uiState.value.loadError)

        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1"))
        vm.uiState.value.onRetry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.loadError)
        assertEquals(1, vm.uiState.value.replies.size)
    }

    @Test
    fun `onSendClick adds a new reply using the first reply's page`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1", page = 7))
        val added = ownReply("r2", contents = "new comment", page = 7)
        coEvery { addAnnotationCommentReplyUseCase(any()) } returns added

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.uiState.value.onInputTextChanged("new comment")
        vm.uiState.value.onSendClick()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            addAnnotationCommentReplyUseCase(
                AddAnnotationCommentReplyUseCase.Params(
                    sessionId = sessionId,
                    canvaDocsDomain = canvaDocsDomain,
                    documentId = documentId,
                    headAnnotationId = headAnnotationId,
                    page = 7,
                    contents = "new comment",
                    // Authored as the canvadocs session user, NOT ApiPrefs.user.id — the same id
                    // canEdit/canDelete gate on, so the poster keeps edit/delete on their own reply
                    // even where the two identities diverge.
                    userId = currentUserId,
                )
            )
        }
        assertEquals(2, vm.uiState.value.replies.size)
        assertEquals("", vm.uiState.value.inputText)
        assertFalse(vm.uiState.value.sending)
        assertEquals(CommentThreadResult(headAnnotationId, listOf(ownReply("r1", page = 7), added)), vm.buildResult())
    }

    @Test
    fun `onSendClick blank input is a no-op`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1"))
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.onInputTextChanged("   ")
        vm.uiState.value.onSendClick()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { addAnnotationCommentReplyUseCase(any()) }
    }

    @Test
    fun `onEditClick is ignored for another user's reply even when canWrite is true`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(otherReply("r1"))
        val vm = createViewModel(canWrite = true)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.replies.first().let { assertFalse(it.canEdit) }
        vm.uiState.value.onEditClick("r1")

        assertNull(vm.uiState.value.editingReplyId)
    }

    @Test
    fun `onEditClick then onSendClick edits the target reply in place`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1", contents = "old"))
        val edited = ownReply("r1", contents = "updated")
        coEvery { editAnnotationCommentReplyUseCase(any()) } returns edited

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.uiState.value.onEditClick("r1")
        assertEquals("old", vm.uiState.value.inputText)

        vm.uiState.value.onInputTextChanged("updated")
        vm.uiState.value.onSendClick()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            editAnnotationCommentReplyUseCase(
                EditAnnotationCommentReplyUseCase.Params(
                    sessionId = sessionId,
                    canvaDocsDomain = canvaDocsDomain,
                    reply = ownReply("r1", contents = "old"),
                    newContents = "updated",
                )
            )
        }
        assertEquals(1, vm.uiState.value.replies.size)
        assertEquals("updated", vm.uiState.value.replies.first().body)
        assertNull(vm.uiState.value.editingReplyId)
        assertEquals(CommentThreadResult(headAnnotationId, listOf(edited)), vm.buildResult())
    }

    @Test
    fun `onDeleteClick is ignored when neither canManage nor own-reply canWrite applies`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(otherReply("r1"))
        val vm = createViewModel(canWrite = true, canManage = false)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.onDeleteClick("r1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { deleteAnnotationCommentReplyUseCase(any()) }
        assertEquals(1, vm.uiState.value.replies.size)
    }

    @Test
    fun `onDeleteClick allowed via canManage even for another user's reply`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(otherReply("r1"), ownReply("r2"))
        coEvery { deleteAnnotationCommentReplyUseCase(any()) } returns Unit

        val vm = createViewModel(canWrite = false, canManage = true)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.uiState.value.onDeleteClick("r1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            deleteAnnotationCommentReplyUseCase(
                DeleteAnnotationCommentReplyUseCase.Params(sessionId, canvaDocsDomain, "r1")
            )
        }
        assertEquals(1, vm.uiState.value.replies.size)
        assertFalse(vm.uiState.value.exitEvent)
    }

    @Test
    fun `deleting the last remaining reply fires the one-shot exit event with an empty result`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1"))
        coEvery { deleteAnnotationCommentReplyUseCase(any()) } returns Unit

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.uiState.value.onDeleteClick("r1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.exitEvent)
        assertTrue(vm.uiState.value.replies.isEmpty())
        assertEquals(CommentThreadResult(headAnnotationId, emptyList()), vm.buildResult())

        vm.uiState.value.onExitEventConsumed()
        assertFalse(vm.uiState.value.exitEvent)
    }

    @Test
    fun `add failure surfaces a one-shot errorMessage and stops sending`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1"))
        coEvery { addAnnotationCommentReplyUseCase(any()) } throws RuntimeException("network")

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.uiState.value.onInputTextChanged("new comment")
        vm.uiState.value.onSendClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Something went wrong", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.sending)

        vm.uiState.value.onErrorMessageConsumed()
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `canComment is false when the session does not allow writing`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1"))
        val vm = createViewModel(canWrite = false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.canComment)
    }

    @Test
    fun `ownership gating is keyed off the currentUserId nav arg, not ApiPrefs`() = runTest {
        // A userId that looks nothing like a typical local Canvas id (simulating the canvadocs-session
        // userId diverging from ApiPrefs.user.id, e.g. act-as/global/cross-shard ids). ApiPrefs is never
        // mocked/initialized in this test class, so if the VM still consulted it, this would either crash
        // or silently disable edit/delete for everyone (see the review's medium-severity finding) — both
        // are ruled out by asserting canEdit/canDelete are correctly granted here.
        val sessionUserId = "canvadocs-user-123"
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(
            ownReply("r1").copy(userId = sessionUserId),
            otherReply("r2"),
        )

        val vm = createViewModel(currentUserId = sessionUserId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.replies.first { it.id == "r1" }.canEdit)
        assertTrue(state.replies.first { it.id == "r1" }.canDelete)
        assertFalse(state.replies.first { it.id == "r2" }.canEdit)
        assertFalse(state.replies.first { it.id == "r2" }.canDelete)
    }

    @Test
    fun `onDeleteClick ignores rapid repeated taps while a delete is in flight`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1"), ownReply("r2"))
        val gate = CompletableDeferred<Unit>()
        coEvery { deleteAnnotationCommentReplyUseCase(any()) } coAnswers { gate.await() }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.onDeleteClick("r1")
        assertTrue(vm.uiState.value.sending)
        vm.uiState.value.onDeleteClick("r1") // second tap on the same reply while in flight — ignored
        vm.uiState.value.onDeleteClick("r2") // a different reply is also blocked by the shared busy flag

        coVerify(exactly = 1) { deleteAnnotationCommentReplyUseCase(any()) }

        gate.complete(Unit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.sending)
        assertEquals(1, vm.uiState.value.replies.size)
    }

    @Test
    fun `onSendClick ignores rapid repeated taps while a send is in flight, preventing a duplicate add`() = runTest {
        coEvery { getAnnotationCommentRepliesUseCase(any()) } returns listOf(ownReply("r1"))
        val gate = CompletableDeferred<CanvaDocAnnotation>()
        coEvery { addAnnotationCommentReplyUseCase(any()) } coAnswers { gate.await() }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.uiState.value.onInputTextChanged("new comment")
        vm.uiState.value.onSendClick()
        vm.uiState.value.onSendClick() // second tap while the first add is still in flight — ignored

        coVerify(exactly = 1) { addAnnotationCommentReplyUseCase(any()) }

        gate.complete(ownReply("r2", contents = "new comment"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Exactly one new reply landed — no duplicate id in the authoritative result.
        assertEquals(listOf("r1", "r2"), vm.buildResult().replies.map { it.annotationId })
    }
}

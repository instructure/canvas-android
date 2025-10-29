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
package com.instructure.horizon.features.inbox.details

import android.content.Context
import android.webkit.URLUtil
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.InboxEventHandler
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
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
class HorizonInboxDetailsViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: HorizonInboxDetailsRepository = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val fileDownloadProgressDao: FileDownloadProgressDao = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val inboxEventHandler: InboxEventHandler = InboxEventHandler()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testConversation = Conversation(
        id = 1L,
        subject = "Test Conversation",
        messages = listOf(
            Message(
                id = 1L,
                authorId = 1L,
                body = "Test message",
                createdAt = "2025-01-01T00:00:00Z",
                attachments = arrayListOf()
            )
        ),
        participants = mutableListOf(
            BasicUser(id = 1L, name = "Test User")
        )
    )

    private val testAnnouncement = DiscussionTopicHeader(
        id = 1L,
        title = "Test Announcement",
        message = "Test message",
        postedDate = Date(),
        author = DiscussionParticipant(id = 1L, displayName = "Test Author")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) } returns true
        every { savedStateHandle.get<Long>(any()) } returns 1L
        every { savedStateHandle.get<String>("courseId") } returns "1"
        every { savedStateHandle.get<String>("type") } returns HorizonInboxItemType.Inbox.navigationValue
        coEvery { repository.getConversation(any(), any(), any()) } returns testConversation
        coEvery { repository.getAnnouncement(any(), any(), any()) } returns testAnnouncement
        coEvery { repository.getAnnouncementTopic(any(), any(), any()) } returns mockk(relaxed = true) {
            every { views } returns mutableListOf()
        }
        coEvery { repository.markAnnouncementAsRead(any(), any(), any()) } returns DataResult.Success(Unit)
        coEvery { repository.addMessageToConversation(any(), any(), any(), any(), any(), any()) } returns testConversation
        coEvery { repository.invalidateConversationDetailsCachedResponse(any()) } returns Unit
        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel loads inbox conversation`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertEquals("Test Conversation", viewModel.uiState.value.title)
        assertEquals(1, viewModel.uiState.value.items.size)
        assertNotNull(viewModel.uiState.value.replyState)
        coVerify { repository.getConversation(1L, true, any()) }
    }

    @Test
    fun `Test conversation message details are mapped correctly`() = runTest {
        val viewModel = getViewModel()

        val item = viewModel.uiState.value.items.first()
        assertEquals("Test User", item.author)
        assertEquals("Test message", item.content)
        assertFalse(item.isHtmlContent)
    }

    @Test
    fun `Test reply text change updates state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.replyState?.onReplyTextValueChange?.invoke(TextFieldValue("Reply text"))

        assertEquals("Reply text", viewModel.uiState.value.replyState?.replyTextValue?.text)
    }

    @Test
    fun `Test send reply adds message to conversation`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.replyState?.onReplyTextValueChange?.invoke(TextFieldValue("Reply text"))

        viewModel.uiState.value.replyState?.onSendReply?.invoke()

        coVerify { repository.addMessageToConversation(any(), any(), any(), "Reply text", any(), any()) }
        coVerify { repository.invalidateConversationDetailsCachedResponse(1L) }
    }

    @Test
    fun `Test send reply clears input after success`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.replyState?.onReplyTextValueChange?.invoke(TextFieldValue("Reply text"))

        viewModel.uiState.value.replyState?.onSendReply?.invoke()

        assertEquals("", viewModel.uiState.value.replyState?.replyTextValue?.text)
        assertFalse(viewModel.uiState.value.replyState?.isLoading == true)
    }

    @Test
    fun `Test send reply handles error`() = runTest {
        coEvery { repository.addMessageToConversation(any(), any(), any(), any(), any(), any()) } throws Exception("Error")

        val viewModel = getViewModel()
        viewModel.uiState.value.replyState?.onReplyTextValueChange?.invoke(TextFieldValue("Reply text"))

        viewModel.uiState.value.replyState?.onSendReply?.invoke()

        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
        assertFalse(viewModel.uiState.value.replyState?.isLoading == true)
    }

    @Test
    fun `Test refresh reloads data`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify(exactly = 2) { repository.getConversation(1L, any(), any()) }
    }

    @Test
    fun `Test refresh handles error`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getConversation(any(), any(), any()) } throws Exception("Error")

        viewModel.uiState.value.loadingState.onRefresh()

        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
        assertFalse(viewModel.uiState.value.loadingState.isRefreshing)
    }

    @Test
    fun `Test snackbar dismiss clears message`() = runTest {
        coEvery { repository.getConversation(any(), any(), any()) } throws Exception("Error")

        val viewModel = getViewModel()
        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)

        viewModel.uiState.value.loadingState.onSnackbarDismiss()

        assertEquals(null, viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `Test attachment picker visibility toggle`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.replyState?.onShowAttachmentPickerChanged?.invoke(true)

        assertTrue(viewModel.uiState.value.replyState?.showAttachmentPicker == true)
    }

    @Test
    fun `Test attachments change updates reply state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.replyState?.onAttachmentsChanged?.invoke(emptyList())

        assertEquals(0, viewModel.uiState.value.replyState?.attachments?.size)
    }

    @Test
    fun `Test exit confirmation dialog visibility toggle`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.replyState?.updateShowExitConfirmationDialog?.invoke(true)

        assertTrue(viewModel.uiState.value.replyState?.showExitConfirmationDialog == true)
    }

    @Test
    fun `Test invalid parameters set error state`() = runTest {
        every { savedStateHandle.get<Long>(any()) } returns null

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.loadingState.isError)
    }

    @Test
    fun `Test load error sets error state`() = runTest {
        coEvery { repository.getConversation(any(), any(), any()) } throws Exception("Error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `Test conversation with attachments are mapped`() = runTest {
        val conversationWithAttachment = testConversation.copy(
            messages = listOf(
                Message(
                    id = 1L,
                    authorId = 1L,
                    body = "Message with attachment",
                    createdAt = "2025-01-01T00:00:00Z",
                    attachments = arrayListOf(
                        Attachment(
                            id = 1L,
                            displayName = "test.pdf",
                            url = "http://example.com/test.pdf",
                            contentType = "application/pdf"
                        )
                    )
                )
            )
        )
        coEvery { repository.getConversation(any(), any(), any()) } returns conversationWithAttachment

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.items.first()
        assertEquals(1, item.attachments.size)
        assertEquals("test.pdf", item.attachments.first().name)
    }

    @Test
    fun `Test course announcement loads correctly`() = runTest {
        every { savedStateHandle.get<String>("type") } returns HorizonInboxItemType.CourseNotification.navigationValue

        val viewModel = getViewModel()

        assertEquals("Test Announcement", viewModel.uiState.value.title)
        assertNotNull(viewModel.uiState.value.titleIcon)
        coVerify { repository.getAnnouncement(1L, 1L, false) }
        coVerify { repository.getAnnouncementTopic(1L, 1L, false) }
    }

    private fun getViewModel(): HorizonInboxDetailsViewModel {
        return HorizonInboxDetailsViewModel(
            context,
            repository,
            workManager,
            fileDownloadProgressDao,
            savedStateHandle,
            inboxEventHandler
        )
    }
}

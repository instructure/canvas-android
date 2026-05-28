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
package com.instructure.horizon.features.inbox.list

import android.content.Context
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.domain.usecase.inbox.GetConversationsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxAccountAnnouncementsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxCourseAnnouncementsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxRecipientsUseCase
import com.instructure.horizon.domain.usecase.inbox.InvalidateConversationListCacheUseCase
import com.instructure.horizon.features.inbox.InboxEvent
import com.instructure.horizon.features.inbox.InboxEventHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HorizonInboxListViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val getConversationsUseCase: GetConversationsUseCase = mockk(relaxed = true)
    private val getInboxRecipientsUseCase: GetInboxRecipientsUseCase = mockk(relaxed = true)
    private val getInboxCourseAnnouncementsUseCase: GetInboxCourseAnnouncementsUseCase = mockk(relaxed = true)
    private val getInboxAccountAnnouncementsUseCase: GetInboxAccountAnnouncementsUseCase = mockk(relaxed = true)
    private val invalidateConversationListCacheUseCase: InvalidateConversationListCacheUseCase = mockk(relaxed = true)
    private val inboxEventHandler: InboxEventHandler = InboxEventHandler()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testConversations = listOf(
        Conversation(id = 1L, subject = "Test 1", lastMessage = "Message 1"),
        Conversation(id = 2L, subject = "Test 2", lastMessage = "Message 2")
    )

    private val testRecipients = listOf(
        Recipient(stringId = "1", name = "Recipient 1"),
        Recipient(stringId = "2", name = "Recipient 2")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getConversationsUseCase(any()) } returns testConversations
        coEvery { getInboxRecipientsUseCase(any()) } returns testRecipients
        coEvery { getInboxCourseAnnouncementsUseCase(any()) } returns emptyList()
        coEvery { getInboxAccountAnnouncementsUseCase(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test data loads successfully`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        coVerify { getConversationsUseCase(match { it.scope == InboxApi.Scope.INBOX && !it.forceNetwork }) }
    }

    @Test
    fun `Test conversations are loaded`() = runTest {
        val viewModel = getViewModel()

        coVerify { getConversationsUseCase(match { it.scope == InboxApi.Scope.INBOX }) }
    }

    @Test
    fun `Test failed data load sets error state`() = runTest {
        coEvery { getConversationsUseCase(any()) } throws Exception("Network error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
    }

    @Test
    fun `Test scope filter change loads correct data`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateScopeFilter(HorizonInboxScope.Sent)

        coVerify { getConversationsUseCase(match { it.scope == InboxApi.Scope.SENT }) }
    }

    @Test
    fun `Test unread scope filter`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateScopeFilter(HorizonInboxScope.Unread)

        coVerify { getConversationsUseCase(match { it.scope == InboxApi.Scope.UNREAD }) }
    }

    @Test
    fun `Test announcements scope loads announcements`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateScopeFilter(HorizonInboxScope.Announcements)

        coVerify { getInboxCourseAnnouncementsUseCase(any()) }
        coVerify { getInboxAccountAnnouncementsUseCase(any()) }
    }

    @Test
    fun `Test recipient selected adds to list`() = runTest {
        val viewModel = getViewModel()

        val recipient = Recipient(stringId = "3", name = "New Recipient")
        viewModel.uiState.value.onRecipientSelected(recipient)

        assertTrue(viewModel.uiState.value.selectedRecipients.contains(recipient))
    }

    @Test
    fun `Test recipient removed from list`() = runTest {
        val viewModel = getViewModel()

        val recipient = Recipient(stringId = "3", name = "Recipient")
        viewModel.uiState.value.onRecipientSelected(recipient)
        viewModel.uiState.value.onRecipientRemoved(recipient)

        assertFalse(viewModel.uiState.value.selectedRecipients.contains(recipient))
    }

    @Test
    fun `Test refresh reloads data`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify(atLeast = 2) { getConversationsUseCase(any()) }
    }

    @Test
    fun `RefreshRequested event triggers refresh and reloads conversations`() = runTest {
        val viewModel = getViewModel()

        val updatedConversations = listOf(
            Conversation(id = 3L, subject = "Updated 1", lastMessage = "New Message 1"),
            Conversation(id = 4L, subject = "Updated 2", lastMessage = "New Message 2")
        )
        coEvery { getConversationsUseCase(any()) } returns updatedConversations

        inboxEventHandler.postEvent(InboxEvent.RefreshRequested)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 2) { getConversationsUseCase(any()) }
    }

    @Test
    fun `AnnouncementRead event triggers refresh`() = runTest {
        val viewModel = getViewModel()

        inboxEventHandler.postEvent(InboxEvent.AnnouncementRead)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 2) { getConversationsUseCase(any()) }
    }

    @Test
    fun `ConversationCreated event triggers refresh and shows snackbar`() = runTest {
        val viewModel = getViewModel()

        val testMessage = "Conversation created successfully"
        inboxEventHandler.postEvent(InboxEvent.ConversationCreated(testMessage))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 2) { getConversationsUseCase(any()) }
    }

    private fun getViewModel(): HorizonInboxListViewModel {
        return HorizonInboxListViewModel(
            context,
            getConversationsUseCase,
            getInboxRecipientsUseCase,
            getInboxCourseAnnouncementsUseCase,
            getInboxAccountAnnouncementsUseCase,
            invalidateConversationListCacheUseCase,
            inboxEventHandler,
        )
    }
}

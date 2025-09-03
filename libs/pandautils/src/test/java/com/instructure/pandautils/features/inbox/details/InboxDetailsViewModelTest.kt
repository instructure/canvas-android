/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandares.R
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState
import com.instructure.pandautils.features.inbox.utils.MessageAction
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.ScreenState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxDetailsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val inboxDetailsRepository: InboxDetailsRepository = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val inboxDetailsBehavior: InboxDetailsBehavior = mockk(relaxed = true)

    private val conversation = Conversation(
        id = 1,
        participants = mutableListOf(BasicUser(id = 1, name = "User 1"), BasicUser(id = 2, name = "User 2")),
        messages = mutableListOf(
            Message(id = 1, authorId = 1, body = "Message 1", participatingUserIds = mutableListOf(1, 2)),
            Message(id = 2, authorId = 2, body = "Message 2", participatingUserIds = mutableListOf(1, 2)),
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context

        coEvery { inboxDetailsRepository.getConversation(any(), any(), any()) } returns DataResult.Success(conversation)
        coEvery { savedStateHandle.get<Long>(InboxDetailsFragment.CONVERSATION_ID) } returns conversation.id
        coEvery { savedStateHandle.get<Boolean>(InboxDetailsFragment.UNREAD) } returns false
        coEvery { context.getString(
            com.instructure.pandautils.R.string.inboxForwardSubjectFwPrefix,
            conversation.subject
        ) } returns "Fwd: ${conversation.subject}"
        coEvery { context.getString(
            com.instructure.pandautils.R.string.inboxReplySubjectRePrefix,
            conversation.subject
        ) } returns "Re: ${conversation.subject}"
        coEvery { featureFlagProvider.checkRestrictStudentAccessFlag() } returns false
        coEvery { featureFlagProvider.checkAccountSurveyNotificationsFlag() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel init`() {
        coEvery { inboxDetailsRepository.getConversation(any(), any(), any()) } returns DataResult.Success(conversation)
        coEvery { inboxDetailsBehavior.getShowBackButton(any()) } returns true

        val viewModel = getViewModel()

        assertEquals(conversation.id, viewModel.conversationId)

        val messageStates = listOf(
            InboxMessageUiState(
                message = conversation.messages[0],
                author = conversation.participants[0],
                recipients = listOf(conversation.participants[1]),
                enabledActions = true,
                canReplyAll = true,
                canDelete = true,
            ),
            InboxMessageUiState(
                message = conversation.messages[1],
                author = conversation.participants[1],
                recipients = listOf(conversation.participants[0]),
                enabledActions = true,
                canReplyAll = true,
                canDelete = true,
            ),
        )
        val expectedUiState = InboxDetailsUiState(
            conversationId = conversation.id,
            conversation = conversation,
            messageStates = messageStates,
            state = ScreenState.Content,
            showReplyAllButton = true,
        )

        assertEquals(expectedUiState, viewModel.uiState.value)

    }

    // region: InboxDetailsAction tests

    @Test
    fun `Test Close fragment action`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(InboxDetailsAction.CloseFragment)

        assertEquals(InboxDetailsFragmentAction.CloseFragment, events.last())
    }

    @Test
    fun `Test Refresh action`() {
        coEvery { inboxDetailsBehavior.getShowBackButton(any()) } returns true
        val viewModel = getViewModel()

        coEvery { inboxDetailsRepository.getConversation(any(), any(), any()) } returns DataResult.Success(conversation)
        val messageStates = listOf(
            InboxMessageUiState(
                message = conversation.messages[0],
                author = conversation.participants[0],
                recipients = listOf(conversation.participants[1]),
                enabledActions = true,
                canReplyAll = true,
                canDelete = true,
            ),
            InboxMessageUiState(
                message = conversation.messages[1],
                author = conversation.participants[1],
                recipients = listOf(conversation.participants[0]),
                enabledActions = true,
                canReplyAll = true,
                canDelete = true,
            ),
        )
        val expectedUiState = InboxDetailsUiState(
            conversationId = conversation.id,
            conversation = conversation,
            messageStates = messageStates,
            state = ScreenState.Content,
            showReplyAllButton = true,
        )

        viewModel.handleAction(InboxDetailsAction.RefreshCalled)

        assertEquals(expectedUiState, viewModel.uiState.value)
        coVerify(exactly = 1) { inboxDetailsRepository.getConversation(conversation.id, true, true) }

    }

    @Test
    fun `Test Conversation Delete action with Cancel`() {
        val viewModel = getViewModel()

        viewModel.handleAction(InboxDetailsAction.DeleteConversation(conversation.id))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteConversation), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteConversation), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onNegativeButtonClick.invoke()

        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)
    }

    @Test
    fun `Test Conversation Delete action with successful Delete`() = runTest {
        val viewModel = getViewModel()
        coEvery { inboxDetailsRepository.deleteConversation(conversation.id) } returns DataResult.Success(conversation)

        viewModel.handleAction(InboxDetailsAction.DeleteConversation(conversation.id))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteConversation), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteConversation), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onPositiveButtonClick.invoke()

        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)
        coVerify(exactly = 1) { inboxDetailsRepository.deleteConversation(conversation.id) }

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(3, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationDeleted)), events[0])
        assertEquals(InboxDetailsFragmentAction.UpdateParentFragment, events[1])
        assertEquals(InboxDetailsFragmentAction.CloseFragment, events[2])
    }

    @Test
    fun `Test Conversation Delete action with failed Delete`() = runTest {
        val viewModel = getViewModel()
        coEvery { inboxDetailsRepository.deleteConversation(conversation.id) } returns DataResult.Fail()

        viewModel.handleAction(InboxDetailsAction.DeleteConversation(conversation.id))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteConversation), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteConversation), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onPositiveButtonClick.invoke()

        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)
        coVerify(exactly = 1) { inboxDetailsRepository.deleteConversation(conversation.id) }

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(1, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationDeletedFailed)), events[0])
    }

    @Test
    fun `Test Message Delete action with Cancel`() {
        val viewModel = getViewModel()

        viewModel.handleAction(InboxDetailsAction.DeleteMessage(conversation.id, conversation.messages[0]))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteMessage), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteMessage), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onNegativeButtonClick.invoke()

        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)
    }

    @Test
    fun `Test Message Delete action with successful Delete`() = runTest {
        coEvery { inboxDetailsBehavior.getShowBackButton(any()) } returns true
        val viewModel = getViewModel()
        val newConversation = conversation.copy(messages = listOf(conversation.messages[1]))
        val messageStates = listOf(
            InboxMessageUiState(
                message = conversation.messages[1],
                author = conversation.participants[1],
                recipients = listOf(conversation.participants[0]),
                enabledActions = true,
                canReplyAll = true,
                canDelete = true,
            ),
        )
        val expectedUiState = InboxDetailsUiState(
            conversationId = newConversation.id,
            conversation = newConversation,
            messageStates = messageStates,
            state = ScreenState.Content,
            showReplyAllButton = true,
        )
        coEvery { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) } returns DataResult.Success(newConversation)
        coEvery { inboxDetailsRepository.getConversation(any(), any(), any()) } returns DataResult.Success(newConversation)

        viewModel.handleAction(InboxDetailsAction.DeleteMessage(conversation.id, conversation.messages[0]))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteMessage), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteMessage), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onPositiveButtonClick.invoke()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(2, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.messageDeleted)), events[0])
        assertEquals(InboxDetailsFragmentAction.UpdateParentFragment, events[1])
        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)
        assertEquals(expectedUiState, viewModel.uiState.value)

        coVerify(exactly = 1) { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) }
    }

    @Test
    fun `Test Message Delete action with failed Delete`() = runTest {
        val viewModel = getViewModel()
        coEvery { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) } returns DataResult.Fail()

        viewModel.handleAction(InboxDetailsAction.DeleteMessage(conversation.id, conversation.messages[0]))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteMessage), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteMessage), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onPositiveButtonClick.invoke()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(1, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.messageDeletedFailed)), events[0])
        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)

        coVerify(exactly = 1) { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) }
    }

    @Test
    fun `Test Reply action`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(InboxDetailsAction.Reply(conversation.messages.last()))

        assertEquals(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildReply(context, conversation, conversation.messages.last())), events.last())
    }

    @Test
    fun `Test Reply All action`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(InboxDetailsAction.ReplyAll(conversation.messages.last()))

        assertEquals(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildReplyAll(context, conversation, conversation.messages.last())), events.last())
    }

    @Test
    fun `Test Forward action`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(InboxDetailsAction.Forward(conversation.messages.last()))

        assertEquals(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildForward(context, conversation, conversation.messages.last())), events.last())
    }

    @Test
    fun `Test Conversation isStarred state update successfully`() {
        val viewModel = getViewModel()
        val isStarred = true
        val newConversation = conversation.copy(isStarred = isStarred)

        coEvery { inboxDetailsRepository.updateStarred(conversation.id, isStarred) } returns DataResult.Success(newConversation)

        viewModel.handleAction(InboxDetailsAction.UpdateStarred(conversation.id, isStarred))

        assertEquals(isStarred, viewModel.uiState.value.conversation?.isStarred)
        coVerify(exactly = 1) { inboxDetailsRepository.updateStarred(conversation.id, isStarred) }
    }

    @Test
    fun `Test Conversation isStarred state update failed`() = runTest {
        val viewModel = getViewModel()
        val isStarred = true

        coEvery { inboxDetailsRepository.updateStarred(conversation.id, isStarred) } returns DataResult.Fail()

        viewModel.handleAction(InboxDetailsAction.UpdateStarred(conversation.id, isStarred))

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }
        assertEquals(1, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationUpdateFailed)), events[0])
        coVerify(exactly = 1) { inboxDetailsRepository.updateStarred(conversation.id, isStarred) }
    }

    @Test
    fun `Test Conversation workflow state update successfully`() {
        val viewModel = getViewModel()
        val newState = Conversation.WorkflowState.READ
        val newConversation = conversation.copy(workflowState = newState)

        coEvery { inboxDetailsRepository.updateState(conversation.id, newState) } returns DataResult.Success(newConversation)

        viewModel.handleAction(InboxDetailsAction.UpdateState(conversation.id, newState))

        assertEquals(newState, viewModel.uiState.value.conversation?.workflowState)
        coVerify(exactly = 1) { inboxDetailsRepository.updateState(conversation.id, newState) }
    }

    @Test
    fun `Test Conversation workflow state update failed`() = runTest {
        val viewModel = getViewModel()
        val newState = Conversation.WorkflowState.READ

        coEvery { inboxDetailsRepository.updateState(conversation.id, newState) } returns DataResult.Fail()

        viewModel.handleAction(InboxDetailsAction.UpdateState(conversation.id, newState))

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }
        assertEquals(1, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationUpdateFailed)), events[0])
        coVerify(exactly = 1) { inboxDetailsRepository.updateState(conversation.id, newState) }
    }

    // endregion

    //region MessageAction tests

    @Test
    fun `Test MessageAction Attachment onClick`() = runTest {
        val viewModel = getViewModel()
        val attachment = Attachment()

        viewModel.messageActionHandler(MessageAction.OpenAttachment(attachment))

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(1, events.size)
        assertEquals(InboxDetailsFragmentAction.OpenAttachment(attachment), events[0])
    }

    @Test
    fun `Test MessageAction open url in message`() = runTest {
        val viewModel = getViewModel()
        val url = "testURL"

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.messageActionHandler(MessageAction.UrlSelected(url))

        assertEquals(InboxDetailsFragmentAction.UrlSelected(url), events.last())
    }

    @Test
    fun `Test MessageAction Reply action`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.messageActionHandler(MessageAction.Reply(conversation.messages.last()))

        assertEquals(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildReply(context, conversation, conversation.messages.last())), events.last())
    }

    @Test
    fun `Test MessageAction Reply All action`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.messageActionHandler(MessageAction.ReplyAll(conversation.messages.last()))

        assertEquals(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildReplyAll(context, conversation, conversation.messages.last())), events.last())
    }

    @Test
    fun `Test MessageAction Forward action`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.messageActionHandler(MessageAction.Forward(conversation.messages.last()))

        assertEquals(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildForward(context, conversation, conversation.messages.last())), events.last())
    }

    @Test
    fun `Test MessageAction Delete Message action with Cancel`() {
        val viewModel = getViewModel()

        viewModel.messageActionHandler(MessageAction.DeleteMessage(conversation.messages[0]))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteMessage), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteMessage), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onNegativeButtonClick.invoke()

        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)
    }

    @Test
    fun `Test MessageAction Delete Message action with successful Delete`() = runTest {
        coEvery { inboxDetailsBehavior.getShowBackButton(any()) } returns true
        val viewModel = getViewModel()
        val newConversation = conversation.copy(messages = listOf(conversation.messages[1]))
        val messageStates = listOf(
            InboxMessageUiState(
                message = conversation.messages[1],
                author = conversation.participants[1],
                recipients = listOf(conversation.participants[0]),
                enabledActions = true,
                canReplyAll = true,
                canDelete = true,
            ),
        )
        val expectedUiState = InboxDetailsUiState(
            conversationId = newConversation.id,
            conversation = newConversation,
            messageStates = messageStates,
            state = ScreenState.Content,
            showReplyAllButton = true,
        )
        coEvery { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) } returns DataResult.Success(newConversation)
        coEvery { inboxDetailsRepository.getConversation(any(), any(), any()) } returns DataResult.Success(newConversation)

        viewModel.messageActionHandler(MessageAction.DeleteMessage(conversation.messages[0]))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteMessage), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteMessage), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onPositiveButtonClick.invoke()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(2, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.messageDeleted)), events[0])
        assertEquals(InboxDetailsFragmentAction.UpdateParentFragment, events[1])
        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)
        assertEquals(expectedUiState, viewModel.uiState.value)

        coVerify(exactly = 1) { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) }
    }

    @Test
    fun `Test MessageAction Delete Message action with failed Delete`() = runTest {
        val viewModel = getViewModel()
        coEvery { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) } returns DataResult.Fail()

        viewModel.messageActionHandler(MessageAction.DeleteMessage(conversation.messages[0]))

        val alertDialogState = viewModel.uiState.value.confirmationDialogState
        assertEquals(true, alertDialogState.showDialog)
        assertEquals(context.getString(R.string.deleteMessage), alertDialogState.title)
        assertEquals(context.getString(R.string.confirmDeleteMessage), alertDialogState.message)
        assertEquals(context.getString(R.string.delete), alertDialogState.positiveButton)
        assertEquals(context.getString(R.string.cancel), alertDialogState.negativeButton)

        alertDialogState.onPositiveButtonClick.invoke()

        val events = mutableListOf<InboxDetailsFragmentAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(1, events.size)
        assertEquals(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.messageDeletedFailed)), events[0])
        assertEquals(ConfirmationDialogState(), viewModel.uiState.value.confirmationDialogState)

        coVerify(exactly = 1) { inboxDetailsRepository.deleteMessage(conversation.id, listOf(conversation.messages[0].id)) }
    }

    @Test
    fun `Test restrict_student_access feature flag hides delete button`() {
        coEvery { featureFlagProvider.checkRestrictStudentAccessFlag() } returns true
        val viewModel = getViewModel()
        val uiState = viewModel.uiState.value

        assertEquals(false, uiState.showDeleteButton)
        assertEquals(false, uiState.showReplyAllButton)
        // Also check that individual messages can't be deleted or replied all
        uiState.messageStates.forEach { messageState ->
            assertEquals(false, messageState.canDelete)
            assertEquals(false, messageState.canReplyAll)
        }
    }

    @Test
    fun `Test without restrict_student_access feature flag delete button is shown`() {
        val viewModel = getViewModel()
        val uiState = viewModel.uiState.value

        assertEquals(true, uiState.showDeleteButton)
        assertEquals(true, uiState.showReplyAllButton)
        // Also check that individual messages can be deleted and replied all
        uiState.messageStates.forEach { messageState ->
            assertEquals(true, messageState.canDelete)
            assertEquals(true, messageState.canReplyAll)
        }
    }

    @Test
    fun `Test default state shows delete button when feature flag is not set`() {
        val viewModel = getViewModel()
        val uiState = viewModel.uiState.value

        assertEquals(true, uiState.showDeleteButton)
        assertEquals(true, uiState.showReplyAllButton)
        // Also check that individual messages can be deleted and replied all by default
        uiState.messageStates.forEach { messageState ->
            assertEquals(true, messageState.canDelete)
            assertEquals(true, messageState.canReplyAll)
        }
    }

    // endregion

    private fun getViewModel(): InboxDetailsViewModel {
        return InboxDetailsViewModel(context, savedStateHandle, inboxDetailsBehavior, inboxDetailsRepository, featureFlagProvider)
    }
}
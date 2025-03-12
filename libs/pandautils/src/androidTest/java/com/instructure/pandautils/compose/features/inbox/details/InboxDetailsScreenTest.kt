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
package com.instructure.pandautils.compose.features.inbox.details

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandautils.features.inbox.details.ConfirmationDialogState
import com.instructure.pandautils.features.inbox.details.InboxDetailsUiState
import com.instructure.pandautils.features.inbox.details.ScreenState
import com.instructure.pandautils.features.inbox.details.composables.InboxDetailsScreen
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState
import com.instructure.pandautils.utils.ScreenState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class InboxDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val title = "Message"

    @Test
    fun testInboxDetailsScreenErrorState() {
        setDetailsScreen(getUiState(state = ScreenState.Error))

        composeTestRule.onNodeWithText("Failed to load conversation")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testInboxDetailsScreenEmptyState() {
        setDetailsScreen(getUiState(state = ScreenState.Empty))

        composeTestRule.onNodeWithText("No messages found")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testInboxDetailsScreenContentStateWithStar() {
        val conversation = getConversation(
            messages = listOf(
                getMessage(id = 1, authorId = 1),
            )
        )
        setDetailsScreen(getUiState(conversation = conversation))

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasContentDescription("More options")
            )
        )
        .assertIsDisplayed()
        .assertHasClickAction()

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasText("Message")
            )
        )
        .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test subject")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Star")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("User 1 to User 2")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test message")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Reply")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithContentDescription("Reply")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).not().and(
                hasContentDescription("More options")
            )
        )
        .assertIsDisplayed()
        .assertHasClickAction()
    }

    @Test
    fun testInboxDetailsScreenContentStateWithUnStar() {
        val conversation = getConversation(
            messages = listOf(
                getMessage(id = 1, authorId = 1),
            ),
            isStarred = true
        )
        setDetailsScreen(getUiState(conversation = conversation))

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasContentDescription("More options")
            )
        )
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasText("Message")
            )
        )
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test subject")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Unstar")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("User 1 to User 2")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test message")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Reply")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithContentDescription("Reply")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).not().and(
                hasContentDescription("More options")
            )
        )
        .assertIsDisplayed()
        .assertHasClickAction()
    }

    @Test
    fun testInboxDetailsAlertDialog() {
        setDetailsScreen(getUiState(
            confirmationDialogState = ConfirmationDialogState(
                showDialog = true,
                title = "Test title",
                message = "Test message",
                positiveButton = "Positive",
                negativeButton = "Negative"
            ),
            conversation = getConversation()
        ))

        composeTestRule.onNodeWithText("Test title")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test message")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Positive")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("Negative")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testInboxDetailsScreenWithCannotReply() {
        val conversation = getConversation(
            messages = listOf(
                getMessage(id = 1, authorId = 1),
            ),
            cannotReply = true
        )
        setDetailsScreen(getUiState(conversation = conversation))

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasContentDescription("More options")
            )
        )
        .assertIsDisplayed()
        .assertHasClickAction()

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasText("Message")
            )
        )
        .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test subject")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Star")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("User 1 to User 2")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test message")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Reply")
            .assertIsNotDisplayed()

        composeTestRule.onNodeWithContentDescription("Reply")
            .assertIsNotDisplayed()

        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).not().and(
                hasContentDescription("More options")
            )
        )
        .assertIsDisplayed()
        .assertHasClickAction()
    }

    private fun setDetailsScreen(uiState: InboxDetailsUiState = getUiState()) {
        composeTestRule.setContent {
            InboxDetailsScreen(
                title = title,
                uiState = uiState,
                messageActionHandler = {},
                actionHandler = {}
            )
        }
    }

    private fun getUiState(
        id: Long = 1,
        conversation: Conversation? = null,
        state: ScreenState = ScreenState.Content,
        confirmationDialogState: ConfirmationDialogState = ConfirmationDialogState()
    ): InboxDetailsUiState {
        return InboxDetailsUiState(
            conversationId = id,
            conversation = conversation,
            messageStates = conversation?.messages?.map { getMessageViewState(conversation, it) } ?: emptyList(),
            state = state,
            confirmationDialogState = confirmationDialogState
        )
    }

    private fun getConversation(
        id: Long = 1,
        subject: String = "Test subject",
        workflowState: Conversation.WorkflowState = Conversation.WorkflowState.READ,
        messages: List<Message> = emptyList(),
        participants: MutableList<BasicUser> = mutableListOf(
            BasicUser(id = 1, name = "User 1"),
            BasicUser(id = 2, name = "User 2"),
            BasicUser(id = 3, name = "User 3"),
        ),
        isStarred: Boolean = false,
        cannotReply: Boolean = false,
    ): Conversation {
        return Conversation(
            id = id,
            subject = subject,
            workflowState = workflowState,
            messages = messages,
            messageCount = messages.size,
            lastMessage = messages.lastOrNull()?.body,
            participants = participants,
            isStarred = isStarred,
            cannotReply = cannotReply
        )
    }

    private fun getMessage(
        id: Long = 1,
        authorId: Long = 1,
        body: String = "Test message",
        participatingUserIds: List<Long> = listOf(2),
        createdAt: String = ZonedDateTime.now().toString()
    ): Message {
        return Message(
            id = id,
            authorId = authorId,
            body = body,
            participatingUserIds = participatingUserIds,
            createdAt = createdAt
        )
    }

    private fun getMessageViewState(conversation: Conversation, message: Message): InboxMessageUiState {
        val author = conversation.participants.find { it.id == message.authorId }
        val recipients = conversation.participants.filter { message.participatingUserIds.filter { it != message.authorId }.contains(it.id) }
        return InboxMessageUiState(
            message = message,
            author = author,
            recipients = recipients,
            enabledActions = true,
            cannotReply = conversation.cannotReply
        )
    }
}
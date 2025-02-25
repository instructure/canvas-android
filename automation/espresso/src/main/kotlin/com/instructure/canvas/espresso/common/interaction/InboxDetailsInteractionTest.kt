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
package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.common.pages.compose.InboxComposePage
import com.instructure.canvas.espresso.common.pages.compose.InboxDetailsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.utils.Randomizer
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.User
import org.junit.Test

abstract class InboxDetailsInteractionTest : CanvasComposeTest() {

    private val inboxPage = InboxPage()
    private val inboxDetailsPage = InboxDetailsPage(composeTestRule)
    private val inboxComposePage = InboxComposePage(composeTestRule)

    @Test
    fun testIfConversationDisplayedCorrectly() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.assertTitle("Message")
        inboxDetailsPage.assertConversationSubject(conversation.subject!!)
        inboxDetailsPage.assertAllMessagesDisplayed(conversation)
    }

    @Test
    fun testMessageReplyTextButton() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        val message = conversation.messages.first()
        inboxDetailsPage.pressReplyTextButtonForMessage(message.body.orEmpty())

        assertReplyComposeScreenDisplayed(conversation)
    }

    @Test
    fun testMessageReplyIconButton() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        val message = conversation.messages.first()
        inboxDetailsPage.pressReplyIconButtonForMessage(message.body.orEmpty())

        assertReplyComposeScreenDisplayed(conversation)
    }

    @Test
    fun testMessageOverflowMenuReplyButton() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        val message = conversation.messages.first()
        inboxDetailsPage.pressOverflowMenuItemForMessage(message.body.orEmpty(), "Reply")

        inboxDetailsPage.assertConversationSubject("Re: ${conversation.subject}")
        assertReplyComposeScreenDisplayed(conversation)
    }

    @Test
    fun testMessageOverflowMenuReplyAllButton() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        val message = conversation.messages.first()
        inboxDetailsPage.pressOverflowMenuItemForMessage(message.body.orEmpty(), "Reply All")

        inboxDetailsPage.assertConversationSubject("Re: ${conversation.subject}")
        assertReplyAllComposeScreenDisplayed(conversation)
    }

    @Test
    fun testMessageOverflowMenuForwardButton() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        val message = conversation.messages.first()
        inboxDetailsPage.pressOverflowMenuItemForMessage(message.body.orEmpty(), "Forward")

        inboxDetailsPage.assertConversationSubject("Fwd: ${conversation.subject}")
        assertForwardComposeScreenDisplayed(conversation)
    }

    @Test
    fun testMessageOverflowMenuDeleteMessageButtonWithCancel() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        val message = conversation.messages.first()
        inboxDetailsPage.pressOverflowMenuItemForMessage(message.body.orEmpty(), "Delete")
        inboxDetailsPage.assertDeleteMessageAlertDialog()
        inboxDetailsPage.pressAlertButton("Cancel")

        inboxDetailsPage.assertConversationSubject(conversation.subject!!)
        inboxDetailsPage.assertAllMessagesDisplayed(conversation)
    }

    @Test
    fun testMessageOverflowMenuDeleteMessageButtonWithConfirm() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        val message = conversation.messages.first()
        inboxDetailsPage.pressOverflowMenuItemForMessage(message.body.orEmpty(), "Delete")
        inboxDetailsPage.assertDeleteMessageAlertDialog()
        inboxDetailsPage.pressAlertButton("Delete")

        inboxDetailsPage.assertConversationSubject(conversation.subject!!)
        inboxDetailsPage.assertAllMessagesDisplayed(conversation.copy(messages = conversation.messages.filter { it.id != message.id }))
    }

    @Test
    fun testConversationStar() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.assertStarred(false)
        inboxDetailsPage.pressStarButton(true)
        inboxDetailsPage.assertStarred(true)

        inboxDetailsPage.pressBackButton()
        inboxPage.assertConversationStarred(conversation.subject!!)
        inboxPage.filterInbox("Starred")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
        inboxPage.openConversation(conversation.subject!!)

        inboxDetailsPage.pressStarButton(false)

        inboxDetailsPage.assertStarred(false)
    }

    @Test
    fun testConversationOverflowMenuReply() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply")

        assertReplyComposeScreenDisplayed(conversation)
    }

    @Test
    fun testConversationOverflowMenuReplyAll() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply All")

        assertReplyAllComposeScreenDisplayed(conversation)
    }

    @Test
    fun testConversationOverflowMenuForward() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.pressOverflowMenuItemForConversation("Forward")

        assertForwardComposeScreenDisplayed(conversation)
    }

    @Test
    fun testConversationOverflowMenuMarkAsUnread() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.pressOverflowMenuItemForConversation("Mark as Unread")

        inboxDetailsPage.pressOverflowMenuItemForConversation("Mark as Read")

        inboxDetailsPage.assertConversationSubject(conversation.subject!!)
        inboxDetailsPage.assertAllMessagesDisplayed(conversation)
    }

    @Test
    fun testConversationOverflowMenuArchive() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")

        inboxDetailsPage.pressBackButton()
        inboxPage.assertConversationNotDisplayed(conversation.subject!!)
        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
        inboxPage.openConversation(conversation.subject!!)

        inboxDetailsPage.pressOverflowMenuItemForConversation("Unarchive")

        inboxDetailsPage.assertConversationSubject(conversation.subject!!)
        inboxDetailsPage.assertAllMessagesDisplayed(conversation)
    }

    @Test
    fun testConversationOverflowMenuDeleteWithCancel() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.pressOverflowMenuItemForConversation("Delete")
        inboxDetailsPage.assertDeleteConversationAlertDialog()

        inboxDetailsPage.pressAlertButton("Cancel")

        inboxDetailsPage.assertConversationSubject(conversation.subject!!)
        inboxDetailsPage.assertAllMessagesDisplayed(conversation)
    }

    @Test
    fun testConversationOverflowMenuDeleteWithConfirm() {
        val data = initData()
        val conversation = getConversations(data).first()
        goToInboxDetails(data, conversation)
        composeTestRule.waitForIdle()

        inboxDetailsPage.pressOverflowMenuItemForConversation("Delete")
        inboxDetailsPage.assertDeleteConversationAlertDialog()

        inboxDetailsPage.pressAlertButton("Delete")

        composeTestRule.waitForIdle()

        inboxPage.assertConversationNotDisplayed(conversation.subject!!)
    }

    private fun assertReplyComposeScreenDisplayed(conversation: Conversation) {
        inboxComposePage.assertTitle("Reply")
        inboxComposePage.assertContextSelected(conversation.contextName!!)

        conversation.participants.filter { it.id == conversation.messages.first().authorId }.map { it.name!! }.forEach {
            inboxComposePage.assertRecipientSelected(it)
        }

        inboxComposePage.assertPreviousMessagesDisplayed(conversation, conversation.messages)
    }

    private fun assertReplyAllComposeScreenDisplayed(conversation: Conversation) {
        inboxComposePage.assertTitle("Reply All")
        inboxComposePage.assertContextSelected(conversation.contextName!!)

        conversation.participants.filter { it.id != getLoggedInUser().id }.map { it.name!! }.forEach {
            inboxComposePage.assertRecipientSelected(it)
        }

        inboxComposePage.assertPreviousMessagesDisplayed(conversation, conversation.messages)
    }

    private fun assertForwardComposeScreenDisplayed(conversation: Conversation) {
        inboxComposePage.assertTitle("Forward")
        inboxComposePage.assertContextSelected(conversation.contextName!!)

        inboxComposePage.assertPreviousMessagesDisplayed(conversation, conversation.messages)
    }

    override fun displaysPageObjects() = Unit

    abstract fun goToInboxDetails(data: MockCanvas, conversation: Conversation)

    abstract fun goToInboxDetails(data: MockCanvas, conversationSubject: String)

    abstract fun initData(): MockCanvas

    abstract fun getLoggedInUser(): User

    abstract fun getTeachers(): List<User>

    abstract fun getConversations(data: MockCanvas): List<Conversation>

    abstract fun addNewConversation(
        data: MockCanvas,
        authorId: Long,
        recipients: List<Long>,
        messageSubject: String = Randomizer.randomConversationSubject(),
        messageBody: String = Randomizer.randomConversationBody(),
    ): Conversation
}
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

import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addConversation
import com.instructure.canvas.espresso.mockcanvas.addConversations
import com.instructure.canvas.espresso.mockcanvas.addConversationsToCourseMap
import com.instructure.canvas.espresso.mockcanvas.createBasicConversation
import com.instructure.canvas.espresso.refresh
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.User
import org.junit.Test

abstract class InboxListInteractionTest : CanvasTest() {
    
    private val inboxPage = InboxPage()

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_showEditToolbarWhenConversationIsSelected() {
        val data = createInitialData()
        val conversation = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        goToInbox(data)
        inboxPage.selectConversation(conversation)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertSelectedConversationNumber("1")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_archiveMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")
        goToInbox(data)
        inboxPage.selectConversations(listOf(conversation1.subject!!, conversation2.subject!!))
        inboxPage.clickArchive()
        inboxPage.assertConversationNotDisplayed(conversation1.subject!!)
        inboxPage.assertConversationNotDisplayed(conversation2.subject!!)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.GONE)

        refresh()
        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversation1.subject!!)
        inboxPage.assertConversationDisplayed(conversation2.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_starMultipleConversations() {
        val data = createInitialData()
        val conversation1 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        goToInbox(data)
        inboxPage.selectConversations(listOf(conversation1.subject!!, conversation2.subject!!))
        inboxPage.clickStar()
        inboxPage.assertConversationStarred(conversation1.subject!!)
        inboxPage.assertConversationStarred(conversation2.subject!!)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_unstarMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        data.conversations[conversation1.id] = conversation1.copy(isStarred = true)
        val conversation2 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")
        data.conversations[conversation2.id] = conversation2.copy(isStarred = true)

        goToInbox(data)
        inboxPage.filterInbox("Starred")
        inboxPage.selectConversations(listOf(conversation1.subject!!, conversation2.subject!!))
        inboxPage.assertSelectedConversationNumber("2")
        inboxPage.clickUnstar()
        inboxPage.assertConversationNotDisplayed(conversation1.subject!!)
        inboxPage.assertConversationNotDisplayed(conversation2.subject!!)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.GONE)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_starMultipleConversationWithDifferentStates() {
        val data = createInitialData()
        val conversation1 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        data.conversations[conversation1.id] = conversation1.copy(isStarred = true)
        val conversation2 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")
        data.conversations[conversation2.id] = conversation2.copy(isStarred = false)
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")

        goToInbox(data)
        inboxPage.selectConversations(listOf(conversation1.subject!!, conversation2.subject!!))
        inboxPage.assertSelectedConversationNumber("2")

        inboxPage.assertStarDisplayed()
        inboxPage.clickStar()

        inboxPage.assertConversationStarred(conversation1.subject!!)
        inboxPage.assertConversationStarred(conversation2.subject!!)
        inboxPage.assertSelectedConversationNumber("2")
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.VISIBLE)

        inboxPage.assertUnStarDisplayed()
        inboxPage.clickUnstar()

        inboxPage.assertConversationNotStarred(conversation1.subject!!)
        inboxPage.assertConversationNotStarred(conversation2.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_markAsReadUnreadMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")

        goToInbox(data)

        inboxPage.selectConversations(listOf(conversation1.subject!!, conversation2.subject!!))
        inboxPage.assertSelectedConversationNumber("2")

        inboxPage.clickMarkAsRead()
        inboxPage.assertUnreadMarkerVisibility(conversation1.subject!!, ViewMatchers.Visibility.GONE)
        inboxPage.assertUnreadMarkerVisibility(conversation2.subject!!, ViewMatchers.Visibility.GONE)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.VISIBLE)

        inboxPage.clickMarkAsUnread()
        inboxPage.assertUnreadMarkerVisibility(conversation1.subject!!, ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertUnreadMarkerVisibility(conversation2.subject!!, ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.VISIBLE)

        inboxPage.selectConversation(conversation1)
        inboxPage.clickMarkAsRead()

        inboxPage.assertUnreadMarkerVisibility(conversation1.subject!!, ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertUnreadMarkerVisibility(conversation2.subject!!, ViewMatchers.Visibility.GONE)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.VISIBLE)

        inboxPage.selectConversation(conversation1)
        inboxPage.clickMarkAsRead()

        inboxPage.assertUnreadMarkerVisibility(conversation1.subject!!, ViewMatchers.Visibility.GONE)
        inboxPage.assertUnreadMarkerVisibility(conversation2.subject!!, ViewMatchers.Visibility.GONE)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_deleteMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")

        goToInbox(data)

        inboxPage.selectConversations(listOf(conversation1.subject!!, conversation2.subject!!))
        inboxPage.assertSelectedConversationNumber("2")

        inboxPage.clickDelete()
        inboxPage.confirmDelete()

        inboxPage.assertConversationNotDisplayed(conversation1.subject!!)
        inboxPage.assertConversationNotDisplayed(conversation2.subject!!)
        inboxPage.assertEditToolbarIs(ViewMatchers.Visibility.GONE)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_swipeToReadUnread() {
        val data = createInitialData()
        val conversation = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")

        goToInbox(data)
        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertUnreadMarkerVisibility(conversation.subject!!, ViewMatchers.Visibility.GONE)

        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertUnreadMarkerVisibility(conversation.subject!!, ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_swipeGesturesInUnreadScope() {
        val data = createInitialData()
        val conversation = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Unread body",
            messageSubject = "Unread Subject")
        val unreadConversation = data.createBasicConversation(getOtherUser().id, workflowState = Conversation.WorkflowState.UNREAD, messageBody = "Unread Body 2")
        data.conversations[unreadConversation.id] = unreadConversation

        goToInbox(data)
        inboxPage.filterInbox("Unread")
        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertConversationNotDisplayed(conversation.subject!!)

        inboxPage.swipeConversationLeft(unreadConversation)
        inboxPage.assertConversationNotDisplayed(unreadConversation.subject!!)

        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(unreadConversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_swipeGesturesInSentScope() {
        val data = createInitialData()
        val sentConversation = data.addConversation(
            senderId = getLoggedInUser().id,
            receiverIds = listOf(getOtherUser().id),
            messageBody = "Sent body",
            messageSubject = "Sent Subject")
        val sentConversation2 = data.createBasicConversation(getLoggedInUser().id, messageBody = "Sent Body 2")
        data.conversations[sentConversation2.id] = sentConversation2

        goToInbox(data)
        inboxPage.filterInbox("Sent")
        inboxPage.swipeConversationRight(sentConversation)
        inboxPage.assertUnreadMarkerVisibility(sentConversation.subject!!, ViewMatchers.Visibility.GONE)

        inboxPage.swipeConversationRight(sentConversation)
        inboxPage.assertUnreadMarkerVisibility(sentConversation.subject!!, ViewMatchers.Visibility.VISIBLE)

        inboxPage.filterInbox("Unread")
        inboxPage.assertConversationDisplayed(sentConversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_swipeToArchive() {
        val data = createInitialData()
        val conversation = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")

        goToInbox(data)
        inboxPage.swipeConversationLeft(conversation)
        inboxPage.assertConversationNotDisplayed(conversation.subject!!)

        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_swipeGesturesInArchivedScope() {
        val data = createInitialData()
        val conversation = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val archivedConversation = data.createBasicConversation(getOtherUser().id, subject = "Archived subject", workflowState = Conversation.WorkflowState.ARCHIVED, messageBody = "Body 2")
        data.conversations[archivedConversation.id] = archivedConversation

        goToInbox(data)
        inboxPage.swipeConversationLeft(conversation)

        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertConversationNotDisplayed(conversation.subject!!) //Because an Unread conversation cannot be Archived.

        inboxPage.swipeConversationLeft(archivedConversation)
        inboxPage.assertConversationNotDisplayed(archivedConversation.subject!!)

        inboxPage.filterInbox("Inbox")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
        inboxPage.assertUnreadMarkerVisibility(conversation.subject!!, ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertConversationDisplayed(archivedConversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_swipeToUnstar() {
        val data = createInitialData()
        val conversation = data.addConversation(
            senderId = getOtherUser().id,
            receiverIds = listOf(getLoggedInUser().id),
            messageBody = "Body",
            messageSubject = "Subject")
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        data.conversations[conversation.id] = conversation.copy(isStarred = true)

        goToInbox(data)
        inboxPage.filterInbox("Starred")
        inboxPage.assertUnreadMarkerVisibility(conversation.subject!!, ViewMatchers.Visibility.VISIBLE)
        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertUnreadMarkerVisibility(conversation.subject!!, ViewMatchers.Visibility.GONE)
        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertUnreadMarkerVisibility(conversation.subject!!, ViewMatchers.Visibility.VISIBLE)

        inboxPage.swipeConversationLeft(conversation)
        inboxPage.assertConversationNotDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeAll() {
        // Should be able to filter messages by All
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        val conversation = getFirstConversation(data)
        goToInbox(data)
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeUnread() {
        // Should be able to filter messages by Unread
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        goToInbox(data)
        val conversation = data.conversations.values.first {
            it.workflowState == Conversation.WorkflowState.UNREAD
        }
        inboxPage.filterInbox("Unread")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeStarred() {
        // Should be able to filter messages by Starred
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        goToInbox(data)
        val conversation = data.conversations.values.first {
            it.isStarred
        }
        inboxPage.filterInbox("Starred")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeSend() {
        // Should be able to filter messages by Send
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        goToInbox(data)
        val conversation = data.conversations.values.first {
            it.workflowState == Conversation.WorkflowState.UNREAD
        }
        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeArchived() {
        // Should be able to filter messages by Archived
        val data = createInitialData()
        data.addConversations(userId = getLoggedInUser().id, messageBody = "Short body")
        goToInbox(data)
        val conversation = data.conversations.values.first {
            it.workflowState == Conversation.WorkflowState.ARCHIVED
        }
        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByContext() {
        // Should be able to filter messages by course or group
        val data = createInitialData(courseCount = 2)
        data.addConversationsToCourseMap(getLoggedInUser().id, data.courses.values.toList(), messageBody = "Short body")
        val firstCourse = data.courses.values.first()
        val conversation = data.conversationCourseMap[firstCourse.id]!!.first()
        goToInbox(data)
        inboxPage.selectInboxFilter(firstCourse)
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    private fun getFirstConversation(data: MockCanvas, includeIsAuthor: Boolean = false): Conversation {
        return data.conversations.values.toList()
            .filter { it.workflowState != Conversation.WorkflowState.ARCHIVED }
            .first {
                if (includeIsAuthor) it.messages.first().authorId == getLoggedInUser().id else it.messages.first().authorId != getLoggedInUser().id
            }
    }

    override fun displaysPageObjects() = Unit

    abstract fun goToInbox(data: MockCanvas)

    abstract fun createInitialData(courseCount: Int = 1): MockCanvas

    abstract fun getLoggedInUser(): User
    
    abstract fun getOtherUser(): User
}
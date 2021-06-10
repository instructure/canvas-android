/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.*
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class InboxInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToIndividual() {
        // Should be able to create and send a message to an individual recipient
        val data = goToInbox()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipient(data.teachers.first(), userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToMultiple() {
        // Should be able to create and send a message to multiple recipients
        val data = goToInbox(teacherCount = 3)
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipients(data.teachers, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToAllUsers() {
        // Should be able to create and send a message to all users in a course
        // Note: There isn't a "single" way to send a message to all users, so I'm just going to select all the
        // recipient group checkboxes
        val data = goToInbox()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.selectAllRecipients(listOf("Teachers", "Students"))
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToIndividualWithAttachment() {
        // Should be able to create and send a message, with an attachment, to an individual recipient
        val data = goToInbox()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        val message = "What is this, hodor?"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipient(teacher1, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage(message)
        newMessagePage.hitSend()
        // Now let's append the attachment after-the-fact, since it is very hard
        // to manually attach anything via Espresso, since it would require manipulating
        // system UIs.
        val attachmentName = "attachment.html"
        val sentConversation = getFirstConversation(data, true)
        addAttachmentToConversation(
            attachmentName,
            sentConversation,
            data
        )
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.selectConversation(sentConversation)
        inboxConversationPage.assertAttachmentDisplayed(attachmentName)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToMultipleWithAttachment() {
        // Should be able to create and send a message, with an attachment, to multiple recipients
        val data = goToInbox()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipients(data.teachers, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        // Now let's append the attachment after-the-fact, since it is very hard
        // to manually attach anything via Espresso, since it would require manipulating
        // system UIs.
        val attachmentName = "attachment.html"
        val sentConversation = getFirstConversation(data, true)
        addAttachmentToConversation(
            attachmentName,
            sentConversation,
            data
        )
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.selectConversation(sentConversation)
        inboxConversationPage.assertAttachmentDisplayed(attachmentName)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToAllUsersWithAttachment() {
        // Should be able to create and send a message, with an attachment, to all users in a course
        val data = goToInbox()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.selectAllRecipients(listOf("Teachers", "Students"))
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        // Now let's append the attachment after-the-fact, since it is very hard
        // to manually attach anything via Espresso, since it would require manipulating
        // system UIs.
        val attachmentName = "attachment.html"
        val sentConversation = getFirstConversation(data, true)
        addAttachmentToConversation(
            attachmentName,
            sentConversation,
            data
        )
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.selectConversation(sentConversation)
        inboxConversationPage.assertAttachmentDisplayed(attachmentName)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_replyToMessage() {
        // Should be able to reply to a message
        val data = goToInbox()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        inboxPage.selectConversation(getFirstConversation(data))
        val message = "What is this, hodor?"
        inboxConversationPage.replyToMessage(message)
        inboxConversationPage.assertMessageDisplayed(message)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_replyToMessageWithAttachment() {
        // Should be able to reply (with attachment) to a message
        val data = goToInbox()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        val conversation = getFirstConversation(data)
        inboxPage.selectConversation(conversation)
        val message = "What is this, hodor?"
        inboxConversationPage.replyToMessage(message)

        // Now let's append the attachment after-the-fact, since it is very hard
        // to manually attach anything via Espresso, since it would require manipulating
        // system UIs.
        val attachmentName = "attachment.html"
        addAttachmentToMessage(attachmentName, conversation.id, message, data)
        inboxConversationPage.refresh()
        inboxConversationPage.assertAttachmentDisplayed(attachmentName)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeAll() {
        // Should be able to filter messages by All
        val data = goToInbox()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        val conversation = getFirstConversation(data)
        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeUnread() {
        // Should be able to filter messages by Unread
        val data = goToInbox()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        val conversation = data.conversations.values.first {
            it.workflowState == Conversation.WorkflowState.UNREAD
        }
        inboxPage.selectInboxScope(InboxApi.Scope.UNREAD)
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeStarred() {
        // Should be able to filter messages by Starred
        val data = goToInbox()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        val conversation = data.conversations.values.first {
            it.isStarred
        }
        inboxPage.selectInboxScope(InboxApi.Scope.STARRED)
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeSend() {
        // Should be able to filter messages by Send
        val data = goToInbox()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        val conversation = data.conversations.values.first {
            it.workflowState == Conversation.WorkflowState.UNREAD
        }
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByTypeArchived() {
        // Should be able to filter messages by Archived
        val data = goToInbox()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        val conversation = data.conversations.values.first {
            it.workflowState == Conversation.WorkflowState.ARCHIVED
        }
        inboxPage.selectInboxScope(InboxApi.Scope.ARCHIVED)
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_filterMessagesByContext() {
        // Should be able to filter messages by course or group
        val data = goToInbox(courseCount = 2)
        data.addConversationsToCourseMap(student1.id, data.courses.values.toList(), messageBody = "Short body")
        val conversation = data.conversationCourseMap[course1.id]!!.first()
        dashboardPage.clickInboxTab()
        inboxPage.selectInboxFilter(course1)
        inboxPage.assertConversationDisplayed(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canComposeAndSendToRoleGroupsIfPermissionEnabled() {
        // Can compose and send messages to one or more role groups if "Send messages to the entire class is enabled"
        val data = goToInbox(teacherCount = 3)
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipientGroup(userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canNotComposeAndSendToRoleGroupsIfPermissionDisabled() {
        // Can NOT compose and send messages to one or more role groups if "Send messages to the entire class is disabled"
        val data = goToInbox(sendMessagesAll = false)
        dashboardPage.clickInboxTab()
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.assertRecipientGroupsNotDisplayed(userType = "Teachers")
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canComposeAndSendToIndividualCourseMembersIfPermissionEnabled() {
        // Can compose and send messages to individual course members if "Send messages to individual course members" is enabled
        // This test is identical to testInbox_createAndSendMessageToIndividual, not sure if its worth having both
        val data = goToInbox()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipient(teacher1, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        inboxPage.selectInboxScope(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canNotComposeAndSendToIndividualCourseMembersIfPermissionDisabled() {
        // Can NOT compose and send messages to individual course members if "Send messages to individual course members" is disabled
        // This test is controlled by the api, so while we are utilizing a mocked CanvasContextPermission value, the only
        // thing making this test pass or fail is what's implemented in the MockCanvas endpoints.
        val data = goToInbox(sendMessages = false, studentCount = 10)
        dashboardPage.clickInboxTab()
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.assertRecipientGroupContains("Students", 1)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_replyAll() {
        val data = goToInbox(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Reply All Message Subject"
        val conversationMessageBody = "Reply All Message Body"
        val replyAllReply = "Reply All Reply"
        val conversation = data.addConversation(
                senderId = data.students[2].id,
                receiverIds = data.students.take(2).map {user -> user.id},
                messageBody = conversationMessageBody,
                messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.selectConversation(conversation)
        inboxConversationPage.assertMessageDisplayed(conversationMessageBody)
        inboxConversationPage.replyAllToMessage(replyAllReply, 2)
        Espresso.pressBack() // To main inbox page
        inboxPage.assertMessageBodyDisplayed(replyAllReply)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_toggleStarred() {
        val data = goToInbox(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Toggle Starred Message Subject"
        val conversationMessageBody = "Toggle Starred Message Body"
        val conversation = data.addConversation(
                senderId = data.students[2].id,
                receiverIds = data.students.take(2).map {user -> user.id},
                messageBody = conversationMessageBody,
                messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.selectConversation(conversation)
        inboxConversationPage.assertNotStarred()
        inboxConversationPage.toggleStarred()
        inboxConversationPage.assertStarred()
        Espresso.pressBack() // To main inbox page
        inboxPage.assertConversationStarred(conversation)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_markUnread() {
        val data = goToInbox(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Mark Unread Message Subject"
        val conversationMessageBody = "Mark Unread Message Body"
        val conversation = data.addConversation(
                senderId = data.students[2].id,
                receiverIds = data.students.take(2).map {user -> user.id},
                messageBody = conversationMessageBody,
                messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.assertUnreadMarkerVisibility(conversation, ViewMatchers.Visibility.VISIBLE)
        inboxPage.selectConversation(conversation)
        Espresso.pressBack()
        inboxPage.assertUnreadMarkerVisibility(conversation, ViewMatchers.Visibility.GONE)
        inboxPage.selectConversation(conversation)
        inboxConversationPage.markUnread() // Should kick us back to the main inbox page
        inboxPage.assertUnreadMarkerVisibility(conversation, ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_archive() {
        val data = goToInbox(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Archive Message Subject"
        val conversationMessageBody = "Archive Message Body"
        val conversation = data.addConversation(
                senderId = data.students[2].id,
                receiverIds = data.students.take(2).map {user -> user.id},
                messageBody = conversationMessageBody,
                messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.selectConversation(conversation)
        inboxConversationPage.archive() // Should kick you back to the main inbox page
        inboxPage.selectInboxScope(InboxApi.Scope.ARCHIVED)
        inboxPage.assertConversationDisplayed(conversationSubject)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_deleteConversation() {
        val data = goToInbox(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Delete Conversation Message Subject"
        val conversationMessageBody = "Delete Conversation Message Body"
        val conversation = data.addConversation(
                senderId = data.students[2].id,
                receiverIds = data.students.take(2).map {user -> user.id},
                messageBody = conversationMessageBody,
                messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.selectConversation(conversation)
        inboxConversationPage.deleteConversation() // Should kick you back to the main inbox page
        inboxPage.assertConversationNotDisplayed(conversationSubject)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_deleteMessage() {
        val data = goToInbox(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Delete Message Message Subject"
        val conversationMessageBody = "Delete Message Message Body"
        val replyMessage = "A reply to be deleted"
        val conversation = data.addConversation(
                senderId = data.students[2].id,
                receiverIds = data.students.take(2).map {user -> user.id},
                messageBody = conversationMessageBody,
                messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.selectConversation(conversation)
        inboxConversationPage.replyToMessage(replyMessage)
        inboxConversationPage.deleteMessage(replyMessage)
        inboxConversationPage.assertMessageNotDisplayed(replyMessage)
    }

    /*
    This test case was probably copied from iOS. Android does not currently have the ability to send to "entire class",
    if it gets added, these tests will need to be implemented.
    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_canComposeAndSendToEntireClassIfPermissionEnabled() {
        // Can compose and send messages to entire class if "Send messages to the entire class" is enabled
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_canNotComposeAndSendToEntireClassIfPermissionDisabled() {
        // Can NOT compose and send messages to entire class if "Send messages to the entire class" is disabled
    }
    */

    private fun getFirstConversation(data: MockCanvas, includeIsAuthor: Boolean = false): Conversation {
        return data.conversations.values.toList().first {
            if(includeIsAuthor) it.messages.first().authorId == student1.id else it.messages.first().authorId != student1.id
        }
    }

    private fun createHtmlAttachment(displayName: String, mockCanvas: MockCanvas): Attachment {
        val attachmentHtml =
                """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">That's one small step for man, one giant leap for mankind -- Neil Armstrong</p>
        </body>
        </html> """

        return Attachment(
                id = mockCanvas.newItemId(),
                contentType = "html",
                filename = "mockhtmlfile.html",
                displayName = displayName,
                size = attachmentHtml.length.toLong()
        )
    }

    private fun addAttachmentToMessage(attachmentName: String, conversationId: Long, message: String, mockCanvas: MockCanvas) {
        val attachment = createHtmlAttachment(attachmentName, mockCanvas)
        val conversation = mockCanvas.conversations[conversationId]!!
        val newMessage = conversation.messages.find { it.body == message }
        val newMessageList = listOf(conversation.messages.first(), newMessage!!.copy(attachments = listOf(attachment)))
        mockCanvas.conversations[conversationId] = conversation.copy(messages = newMessageList)
    }

    private fun addAttachmentToConversation(attachmentName: String, conversation: Conversation, mockCanvas: MockCanvas) {
        val attachment = createHtmlAttachment(attachmentName, mockCanvas)
        val newMessageList = listOf(conversation.messages.first().copy(attachments = listOf(attachment)))
        mockCanvas.conversations[conversation.id] = conversation.copy(messages = newMessageList)
    }

    private lateinit var course1 : Course
    private lateinit var student1 : User
    private lateinit var teacher1 : User

    private fun goToInbox(
        studentCount: Int = 1,
        teacherCount: Int = 1,
        courseCount: Int = 1,
        sendMessages: Boolean = true,
        sendMessagesAll: Boolean = true
    ): MockCanvas {
        val data = MockCanvas.init(
            studentCount = studentCount,
            courseCount = courseCount,
            teacherCount = teacherCount,
            favoriteCourseCount = courseCount
        )

        student1 = data.students[0]
        teacher1 = data.teachers[0]
        course1 = data.courses.values.first()

        data.addCoursePermissions(
            course1.id,
            CanvasContextPermission(send_messages_all = sendMessagesAll, send_messages = sendMessages)
        )

        data.addRecipientsToCourse(
            course = course1,
            students = data.students,
            teachers = data.teachers
        )

        val token = data.tokenFor(student1)!!
        tokenLogin(data.domain, token, student1)
        dashboardPage.waitForRender()

        return data
    }
}

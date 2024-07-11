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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addConversation
import com.instructure.canvas.espresso.mockCanvas.addConversations
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addRecipientsToCourse
import com.instructure.canvas.espresso.mockCanvas.addSentConversation
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class InboxConversationInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToIndividual() {
        // Should be able to create and send a message to an individual recipient
        val data = createInitialData()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipient(data.teachers.first(), userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.clickSend()
        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToMultiple() {
        // Should be able to create and send a message to multiple recipients
        val data = createInitialData(teacherCount = 3)
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipients(data.teachers, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.clickSend()
        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToAllUsers() {
        // Should be able to create and send a message to all users in a course
        // Note: There isn't a "single" way to send a message to all users, so I'm just going to select all the
        // recipient group checkboxes
        val data = createInitialData()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.selectAllRecipients(listOf("Teachers", "Students"))
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.clickSend()
        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToIndividualWithAttachment() {
        // Should be able to create and send a message, with an attachment, to an individual recipient
        val data = createInitialData()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        val message = "What is this, hodor?"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipient(teacher1, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage(message)
        newMessagePage.clickSend()
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
        inboxPage.filterInbox("Sent")
        inboxPage.openConversation(sentConversation)
        inboxConversationPage.assertAttachmentDisplayed(attachmentName)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToMultipleWithAttachment() {
        // Should be able to create and send a message, with an attachment, to multiple recipients
        val data = createInitialData()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipients(data.teachers, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.clickSend()
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
        inboxPage.filterInbox("Sent")
        inboxPage.openConversation(sentConversation)
        inboxConversationPage.assertAttachmentDisplayed(attachmentName)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_createAndSendMessageToAllUsersWithAttachment() {
        // Should be able to create and send a message, with an attachment, to all users in a course
        val data = createInitialData()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.selectAllRecipients(listOf("Teachers", "Students"))
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.clickSend()
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
        inboxPage.filterInbox("Sent")
        inboxPage.openConversation(sentConversation)
        inboxConversationPage.assertAttachmentDisplayed(attachmentName)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_replyToMessage() {
        // Should be able to reply to a message
        val data = createInitialData()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        inboxPage.openConversation(getFirstConversation(data))
        val message = "What is this, hodor?"
        inboxConversationPage.replyToMessage(message)
        inboxConversationPage.assertMessageDisplayed(message)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_replyToMessageWithAttachment() {
        // Should be able to reply (with attachment) to a message
        val data = createInitialData()
        data.addConversations(userId = student1.id, messageBody = "Short body")
        dashboardPage.clickInboxTab()
        val conversation = getFirstConversation(data)
        inboxPage.openConversation(conversation)
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
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canComposeAndSendToRoleGroupsIfPermissionEnabled() {
        // Can compose and send messages to one or more role groups if "Send messages to the entire class is enabled"
        val data = createInitialData(teacherCount = 3)
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipientGroup(userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.clickSend()
        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canNotComposeAndSendToRoleGroupsIfPermissionDisabled() {
        // Can NOT compose and send messages to one or more role groups if "Send messages to the entire class is disabled"
        val data = createInitialData(sendMessagesAll = false)
        dashboardPage.clickInboxTab()
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.assertRecipientGroupsNotDisplayed(userType = "Teachers")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canComposeAndSendToIndividualCourseMembersIfPermissionEnabled() {
        // Can compose and send messages to individual course members if "Send messages to individual course members" is enabled
        // This test is identical to testInbox_createAndSendMessageToIndividual, not sure if its worth having both
        val data = createInitialData()
        dashboardPage.clickInboxTab()
        val subject = "Hodor"
        data.addSentConversation(subject, student1.id, messageBody = "Short body")
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.setRecipient(teacher1, userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.clickSend()
        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed(subject)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_canNotComposeAndSendToIndividualCourseMembersIfPermissionDisabled() {
        // Can NOT compose and send messages to individual course members if "Send messages to individual course members" is disabled
        // This test is controlled by the api, so while we are utilizing a mocked CanvasContextPermission value, the only
        // thing making this test pass or fail is what's implemented in the MockCanvas endpoints.
        val data = createInitialData(sendMessages = false, studentCount = 10)
        dashboardPage.clickInboxTab()
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(course1)
        newMessagePage.assertRecipientGroupContains("Students", 1)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_replyAll() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
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
        inboxPage.openConversation(conversation)
        inboxConversationPage.assertMessageDisplayed(conversationMessageBody)
        inboxConversationPage.replyAllToMessage(replyAllReply, 2)
        Espresso.pressBack() // To main inbox page
        inboxPage.assertMessageBodyDisplayed(replyAllReply)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_toggleStarred() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
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
        inboxPage.openConversation(conversation)
        inboxConversationPage.assertNotStarred()
        inboxConversationPage.toggleStarred()
        inboxConversationPage.assertStarred()
        Espresso.pressBack() // To main inbox page
        inboxPage.assertConversationStarred(conversation.subject!!)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_markUnread() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
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
        inboxPage.openConversation(conversation)
        Espresso.pressBack()
        inboxPage.assertUnreadMarkerVisibility(conversation, ViewMatchers.Visibility.GONE)
        inboxPage.openConversation(conversation)
        inboxConversationPage.markUnread() // Should kick us back to the main inbox page
        inboxPage.assertUnreadMarkerVisibility(conversation, ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_archive() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
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
        inboxPage.openConversation(conversation)
        inboxConversationPage.archive() // Should kick you back to the main inbox page
        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversationSubject)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_deleteConversation() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
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
        inboxPage.openConversation(conversation)
        inboxConversationPage.deleteConversation() // Should kick you back to the main inbox page
        inboxPage.assertConversationNotDisplayed(conversationSubject)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_deleteMessage() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
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
        inboxPage.openConversation(conversation)
        inboxConversationPage.replyToMessage(replyMessage)
        inboxConversationPage.deleteMessage(replyMessage)
        inboxConversationPage.assertMessageNotDisplayed(replyMessage)
    }

    private fun getFirstConversation(data: MockCanvas, includeIsAuthor: Boolean = false): Conversation {
        return data.conversations.values.toList()
            .filter { it.workflowState != Conversation.WorkflowState.ARCHIVED }
            .first {
                if (includeIsAuthor) it.messages.first().authorId == student1.id else it.messages.first().authorId != student1.id
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

    private fun createInitialData(
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

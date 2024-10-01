/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.teacher.ui

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addConversation
import com.instructure.canvas.espresso.mockCanvas.addConversations
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addRecipientsToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.clickInboxTab
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class InboxMessagePageTest: TeacherTest() {

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

    @Test
    override fun displaysPageObjects() {
        getToMessageThread()
        inboxMessagePage.assertPageObjects()
    }

    @Test
    fun displaysMessage() {
        getToMessageThread()
        inboxMessagePage.assertHasMessage()
    }

    private fun getToMessageThread() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Test Subject"
        val conversationMessageBody = "Test Message Body"
        val conversation = data.addConversation(
            senderId = data.students[2].id,
            receiverIds = data.students.take(2).map {user -> user.id},
            messageBody = conversationMessageBody,
            messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.openConversation(conversation)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_showReplyButton() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Test Subject"
        val conversationMessageBody = "Test Message Body"
        val conversation = data.addConversation(
            senderId = data.students[2].id,
            receiverIds = data.students.take(2).map {user -> user.id},
            messageBody = conversationMessageBody,
            messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.openConversation(conversation)
        inboxMessagePage.assertReplyButtonVisible(true)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_hideReplyButton() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Test Subject"
        val conversationMessageBody = "Test Message Body"
        val conversation = data.addConversation(
            senderId = data.students[2].id,
            receiverIds = data.students.take(2).map {user -> user.id},
            messageBody = conversationMessageBody,
            messageSubject = conversationSubject,
            cannotReply = true
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.openConversation(conversation)
        inboxMessagePage.assertReplyButtonVisible(false)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_showReplyMenuItems() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Test Subject"
        val conversationMessageBody = "Test Message Body"
        val conversation = data.addConversation(
            senderId = data.students[2].id,
            receiverIds = data.students.take(2).map {user -> user.id},
            messageBody = conversationMessageBody,
            messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.openConversation(conversation)
        inboxMessagePage.assertReplyMenuItemsVisible(true)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_hideReplyMenuItems() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Test Subject"
        val conversationMessageBody = "Test Message Body"
        val conversation = data.addConversation(
            senderId = data.students[2].id,
            receiverIds = data.students.take(2).map {user -> user.id},
            messageBody = conversationMessageBody,
            messageSubject = conversationSubject,
            cannotReply = true
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.openConversation(conversation)
        inboxMessagePage.assertReplyMenuItemsVisible(false)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_showReplyMessageOptions() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Test Subject"
        val conversationMessageBody = "Test Message Body"
        val conversation = data.addConversation(
            senderId = data.students[2].id,
            receiverIds = data.students.take(2).map {user -> user.id},
            messageBody = conversationMessageBody,
            messageSubject = conversationSubject
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.openConversation(conversation)
        inboxMessagePage.assertReplyMessageOptionsVisible(true)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun testInbox_hideReplyMessageOptions() {
        val data = createInitialData(studentCount = 3, teacherCount = 1)
        val conversationSubject = "Test Subject"
        val conversationMessageBody = "Test Message Body"
        val conversation = data.addConversation(
            senderId = data.students[2].id,
            receiverIds = data.students.take(2).map {user -> user.id},
            messageBody = conversationMessageBody,
            messageSubject = conversationSubject,
            cannotReply = true
        )

        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(conversationSubject)
        inboxPage.openConversation(conversation)
        inboxMessagePage.assertReplyMessageOptionsVisible(false)
    }
}

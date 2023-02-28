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

import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addConversation
import com.instructure.canvas.espresso.mockCanvas.addConversations
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.User
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.clickInboxTab
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class InboxPageTest: TeacherTest() {

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    override fun displaysPageObjects() {
        val data = createInitialData()
        val teacher = data.teachers[0]
        data.addConversations(userId = teacher.id)
        navigateToInbox(data, teacher)
        inboxPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun displaysConversation() {
        val data = createInitialData()
        val teacher = data.teachers[0]
        data.addConversations(userId = teacher.id)

        // Test expects single conversation; filter down to starred conversation
        val unwanted = data.conversations.filter() {entry -> !entry.value.isStarred}
        unwanted.forEach() { (id, conversation) -> data.conversations.remove(id)}

        navigateToInbox(data, teacher)
        inboxPage.assertHasConversation()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun showEditToolbarWhenConversationIsSelected() {
        val data = createInitialData()
        val conversation = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")

        navigateToInbox(data, data.teachers.first())
        inboxPage.selectConversation(conversation)
        inboxPage.assertEditToolbarDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun archiveMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")

        navigateToInbox(data, data.teachers.first())
        inboxPage.selectConversation(conversation1)
        inboxPage.selectConversation(conversation2)
        inboxPage.clickArchive()
        inboxPage.assertConversationNotDisplayed(conversation1.subject ?: "")
        inboxPage.assertConversationNotDisplayed(conversation2.subject ?: "")

        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversation1.subject ?: "")
        inboxPage.assertConversationDisplayed(conversation2.subject ?: "")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun starMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")

        navigateToInbox(data, data.teachers.first())
        inboxPage.selectConversation(conversation1)
        inboxPage.selectConversation(conversation2)
        inboxPage.clickStar()
        inboxPage.assertConversationStarred(conversation1.subject ?: "")
        inboxPage.assertConversationStarred(conversation2.subject ?: "")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun unstarMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")
        data.conversations[conversation1.id] = conversation1.copy(isStarred = true)
        val conversation2 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")
        data.conversations[conversation2.id] = conversation2.copy(isStarred = true)

        navigateToInbox(data, data.teachers.first())
        inboxPage.filterInbox("Starred")
        inboxPage.selectConversation(conversation1)
        inboxPage.selectConversation(conversation2)
        inboxPage.clickUnstar()
        inboxPage.assertConversationNotDisplayed(conversation1.subject ?: "")
        inboxPage.assertConversationNotDisplayed(conversation2.subject ?: "")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun markAsReadUnreadMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")

        navigateToInbox(data, data.teachers.first())
        inboxPage.selectConversation(conversation1)
        inboxPage.selectConversation(conversation2)
        inboxPage.clickMarkAsRead()
        inboxPage.assertUnreadMarkerVisibility(conversation1.subject ?: "", ViewMatchers.Visibility.GONE)
        inboxPage.assertUnreadMarkerVisibility(conversation2.subject ?: "", ViewMatchers.Visibility.GONE)

        inboxPage.clickMarkAsUnread()
        inboxPage.assertUnreadMarkerVisibility(conversation1.subject ?: "", ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertUnreadMarkerVisibility(conversation2.subject ?: "", ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun deleteMultipleConversations() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation1 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")
        val conversation2 = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body 2",
            messageSubject = "Subject 2")

        navigateToInbox(data, data.teachers.first())
        inboxPage.selectConversation(conversation1)
        inboxPage.selectConversation(conversation2)
        inboxPage.clickDelete()
        inboxPage.confirmDelete()
        inboxPage.assertConversationNotDisplayed(conversation1.subject ?: "")
        inboxPage.assertConversationNotDisplayed(conversation2.subject ?: "")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun swipeToReadUnread() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")

        navigateToInbox(data, data.teachers.first())
        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertUnreadMarkerVisibility(conversation.subject ?: "", ViewMatchers.Visibility.GONE)

        inboxPage.swipeConversationRight(conversation)
        inboxPage.assertUnreadMarkerVisibility(conversation.subject ?: "", ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun swipeToArchive() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")

        navigateToInbox(data, data.teachers.first())
        inboxPage.swipeConversationLeft(conversation)
        inboxPage.assertConversationNotDisplayed(conversation.subject ?: "")

        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(conversation.subject ?: "")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.INTERACTION)
    fun swipeToUnstar() {
        val data = createInitialData()
        data.addConversations(userId = data.teachers.first().id, messageBody = "Short body")
        val conversation = data.addConversation(
            senderId = data.students.first().id,
            receiverIds = listOf(data.teachers.first().id),
            messageBody = "Body",
            messageSubject = "Subject")
        data.conversations[conversation.id] = conversation.copy(isStarred = true)

        navigateToInbox(data, data.teachers.first())
        inboxPage.filterInbox("Starred")
        inboxPage.swipeConversationLeft(conversation)
        inboxPage.assertConversationNotDisplayed(conversation.subject ?: "")
    }

    private fun createInitialData(): MockCanvas {
        return MockCanvas.init(
            courseCount = 1,
            favoriteCourseCount = 1,
            teacherCount = 1,
            studentCount = 1
        )
    }

    private fun navigateToInbox(data: MockCanvas, teacher: User) {
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.clickInboxTab()
    }
}

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
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class InboxInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToIndividual() {
        // Should be able to create and send a message to an individual recipient
        val data = goToInbox()
        val subject = "Hodor"
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(data.courses.values.first())
        newMessagePage.setRecipient(data.teachers.first(), userType = "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        Espresso.pressBack()
        inboxPage.selectInboxFilter(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(subject)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToMultiple() {
        // Should be able to create and send a message to multiple recipients
        val data = goToInbox(teacherCount = 3)
        val subject = "Hodor"
        inboxPage.pressNewMessageButton()
        newMessagePage.selectCourse(data.courses.values.first())
        newMessagePage.setRecipients(data.teachers, "Teachers")
        newMessagePage.setSubject(subject)
        newMessagePage.setMessage("Hodor, Hodor? Hodor!")
        newMessagePage.hitSend()
        Espresso.pressBack()
        inboxPage.selectInboxFilter(InboxApi.Scope.SENT)
        inboxPage.assertConversationDisplayed(subject)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToAllUsers() {
        // Should be able to create and send a message to all users in a course
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToIndividualWithAttachment() {
        // Should be able to create and send a message, with an attachment, to an individual recipient
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToMultipleWithAttachment() {
        // Should be able to create and send a message, with an attachment, to multiple recipients
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToAllUsersWithAttachment() {
        // Should be able to create and send a message, with an attachment, to all users in a course
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_replyToMessage() {
        // Should be able to reply to a message
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_replyToMessageWithAttachment() {
        // Should be able to reply (with attachment) to a message
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_filterMessagesByType() {
        // Should be able to filter messages by All, Unread, Starred, Send, and Archived
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_filterMessagesByContext() {
        // Should be able to filter messages by course or group
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_canComposeAndSendToRoleGroupsIfPermissionEnabled() {
        // Can compose and send messages to one or more role groups if "Send messages to the entire class is enabled"
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_canNotComposeAndSendToRoleGroupsIfPermissionDisabled() {
        // Can NOT compose and send messages to one or more role groups if "Send messages to the entire class is disabled"
    }

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

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_canComposeAndSendToIndividualCourseMembersIfPermissionEnabled() {
        // Can compose and send messages to individual course members if "Send messages to individual course members" is enabled
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_canNotComposeAndSendToIndividualCourseMembersIfPermissionDisabled() {
        // Can NOT compose and send messages to individual course members if "Send messages to individual course members" is disabled
    }

    private fun goToInbox(
        studentCount: Int = 1,
        teacherCount: Int = 1,
        courseCount: Int = 1
    ): MockCanvas {
        val data = MockCanvas.init(
            studentCount = studentCount,
            courseCount = courseCount,
            teacherCount = teacherCount,
            favoriteCourseCount = courseCount
        )

        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()
        dashboardPage.clickInboxTab()

        return data
    }
}

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

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class InboxInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToIndividual() {
        // Should be able to create and send a message to an individual recipient
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.INBOX, TestCategory.INTERACTION, true)
    fun testInbox_createAndSendMessageToMultiple() {
        // Should be able to create and send a message to multiple recipients
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
}

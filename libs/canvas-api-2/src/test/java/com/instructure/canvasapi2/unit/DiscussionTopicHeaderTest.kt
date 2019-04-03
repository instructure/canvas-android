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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.DiscussionTopicHeader

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class DiscussionTopicHeaderTest {

    @Test
    fun convertToDiscussionEntryTest_Null() {
        val header = DiscussionTopicHeader()

        assertNotNull(header.convertToDiscussionEntry("localized_graded", "localized_points"))
    }

    @Test
    fun convertToDiscussionEntryTest_Message() {
        val message = "here is a message"
        val header = DiscussionTopicHeader()
        header.message = message
        val entry = header.convertToDiscussionEntry("graded", "points")
        assertEquals(header.message, entry.message)
    }

    @Test
    fun convertToDiscussionEntryTest_Description() {
        val localizedGraded = "Graded discussion"
        val header = DiscussionTopicHeader()
        val assignment = Assignment()
        header.assignment = assignment

        val entry = header.convertToDiscussionEntry(localizedGraded, "points")

        assertEquals(localizedGraded, entry.description)
    }

    @Test
    fun convertToDiscussionEntryTest_NullParent() {
        val header = DiscussionTopicHeader()

        val entry = header.convertToDiscussionEntry("grade", "points")
        assertEquals(null, entry.parent)
    }

    @Test
    fun convertToDiscussionEntryTest_ParentID() {
        val header = DiscussionTopicHeader()

        val entry = header.convertToDiscussionEntry("grade", "points")
        assertEquals(-1, entry.parentId)
    }

    @Test
    fun getTypeTest_SideComment() {
        val discussionTopicHeader = DiscussionTopicHeader()
        discussionTopicHeader.discussionType = "side_comment"

        assertEquals(DiscussionTopicHeader.DiscussionType.SIDE_COMMENT, discussionTopicHeader.type)
    }

    @Test
    fun getTypeTest_Threaded() {
        val discussionTopicHeader = DiscussionTopicHeader()
        discussionTopicHeader.discussionType = "threaded"

        assertEquals(DiscussionTopicHeader.DiscussionType.THREADED, discussionTopicHeader.type)
    }

    @Test
    fun getTypeTest_Unknown() {
        val discussionTopicHeader = DiscussionTopicHeader()
        discussionTopicHeader.discussionType = "other"

        assertEquals(DiscussionTopicHeader.DiscussionType.UNKNOWN, discussionTopicHeader.type)
    }

    @Test
    fun getStatusTest_Read() {
        val discussionTopicHeader = DiscussionTopicHeader()
        discussionTopicHeader.readState = "read"

        assertEquals(DiscussionTopicHeader.ReadState.READ, discussionTopicHeader.status)
    }

    @Test
    fun getStatusTest_Unread() {
        val discussionTopicHeader = DiscussionTopicHeader()
        discussionTopicHeader.readState = "unread"

        assertEquals(DiscussionTopicHeader.ReadState.UNREAD, discussionTopicHeader.status)
    }

    @Test
    fun getStatusTest_Other() {
        val discussionTopicHeader = DiscussionTopicHeader()
        discussionTopicHeader.readState = "anything_else"

        assertEquals(DiscussionTopicHeader.ReadState.UNREAD, discussionTopicHeader.status)
    }

}
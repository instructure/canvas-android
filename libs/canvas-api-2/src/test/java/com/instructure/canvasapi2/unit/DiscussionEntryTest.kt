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


import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DiscussionEntryTest {

    @Test
    fun initTest_Unread() {
        val topic = DiscussionTopic()
        val parent = DiscussionEntry()
        val entry = DiscussionEntry()

        entry.init(topic, parent)

        assertEquals(0, entry.unreadChildren)
    }

    @Test
    fun initTest_TotalChildrenZero() {
        val topic = DiscussionTopic()
        val parent = DiscussionEntry()
        val entry = DiscussionEntry()

        entry.init(topic, parent)

        assertEquals(0, entry.totalChildren)
    }

    @Test
    fun initTest_TotalChildrenOne() {
        val topic = DiscussionTopic()
        val parent = DiscussionEntry()
        val entry = DiscussionEntry()
        val reply = DiscussionEntry()
        entry.addReply(reply)

        entry.init(topic, parent)

        assertEquals(1, entry.totalChildren)
    }

    @Test
    fun initTest_UnreadChildrenOne() {
        val id = 7L
        val unreadEntries = arrayListOf(id)

        val topic = DiscussionTopic(unreadEntries = unreadEntries)
        val parent = DiscussionEntry()
        val entry = DiscussionEntry()
        val reply = DiscussionEntry(id = id)

        entry.addReply(reply)
        entry.init(topic, parent)

        assertEquals(1, entry.unreadChildren)
    }

    @Test
    fun depthTest_Zero() {
        val entry = DiscussionEntry()

        assertEquals(0, entry.depth)
    }

    @Test
    fun depthTest_One() {
        val entry = DiscussionEntry()
        val parent = DiscussionEntry()
        entry.parent = parent

        assertEquals(1, entry.depth)
    }

    @Test
    fun addReplyTest() {
        val entry = DiscussionEntry()

        entry.addReply(null)
        assertNotNull(entry.replies)
    }

    @Test
    fun addInnerReplyTest() {
        val entry = DiscussionEntry(id = 1L)
        val reply = DiscussionEntry()
        entry.addReply(reply)

        val innerReply = DiscussionEntry()

        entry.addInnerReply(reply, innerReply)

        assertEquals(1, entry.replies?.get(0)?.replies?.size)
    }
}
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

import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic
import org.junit.Assert.assertEquals
import org.junit.Test

class DiscussionTopicTest {
    @Test
    fun getUnreadEntriesMap_Size() {
        val unreadList = arrayListOf(8L, 13435L)
        val topic = DiscussionTopic(unreadEntries = unreadList)

        assertEquals(2, topic.unreadEntriesMap().size)
    }

    @Test
    fun getUnreadEntriesMap_Content() {
        val id = 8L
        val unreadList = arrayListOf(id)

        val topic = DiscussionTopic(unreadEntries = unreadList)

        assertEquals(id, topic.unreadEntriesMap().keys.toTypedArray()[0])
    }

    @Test
    fun getParticipantsMap_Size() {
        val participant = DiscussionParticipant(id = 64343L)
        val participants = arrayListOf(participant)

        val topic = DiscussionTopic(participants = participants)

        assertEquals(1, topic.participantsMap.size)
    }

    @Test
    fun getParticipantsMap_Content() {
        val id = 534234L
        val participant = DiscussionParticipant(id = id)
        val participants = arrayListOf(participant)

        val topic = DiscussionTopic(participants = participants)

        assertEquals(id, topic.participantsMap.keys.toTypedArray()[0])
    }

    @Test
    fun getDiscussionURL() {
        val url = "https://mobiledev.instructure.com/courses/24219/discussion_topics/1129998"

        assertEquals(url, DiscussionTopic.getDiscussionURL("https", "mobiledev.instructure.com", 24219, 1129998))
    }
}
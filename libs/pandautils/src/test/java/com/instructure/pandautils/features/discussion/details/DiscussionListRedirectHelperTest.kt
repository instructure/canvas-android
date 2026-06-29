/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.discussion.details

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscussionListRedirectHelperTest {

    @Test
    fun `Announcement index urls map to ANNOUNCEMENT`() {
        assertEquals(DiscussionListType.ANNOUNCEMENT, discussionListRedirectType("https://domain.com/courses/3/announcements"))
        assertEquals(DiscussionListType.ANNOUNCEMENT, discussionListRedirectType("https://domain.com/groups/5/announcements/"))
        assertEquals(DiscussionListType.ANNOUNCEMENT, discussionListRedirectType("https://domain.com/courses/3/announcements?foo=bar"))
    }

    @Test
    fun `Discussion index urls map to DISCUSSION`() {
        assertEquals(DiscussionListType.DISCUSSION, discussionListRedirectType("https://domain.com/courses/3/discussion_topics"))
        assertEquals(DiscussionListType.DISCUSSION, discussionListRedirectType("https://domain.com/courses/3/discussion_topics/#anchor"))
    }

    @Test
    fun `Topic detail urls return null`() {
        assertNull(discussionListRedirectType("https://domain.com/courses/3/announcements/6"))
        assertNull(discussionListRedirectType("https://domain.com/courses/3/discussion_topics/6"))
        assertNull(discussionListRedirectType("https://domain.com/courses/3/discussion_topics/6?embed=true"))
    }
}
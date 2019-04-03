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

package com.instructure.pandautils.unit;

import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.pandautils.discussions.DiscussionEntryHTMLHelper;
import com.instructure.pandautils.discussions.DiscussionEntryHtmlConverter;

import org.junit.Test;

import static org.junit.Assert.*;

public class DiscussionEntryHTMLConverterTest {
    @Test
    public void getReadState_read() throws Exception {
        DiscussionEntryHtmlConverter converter = new DiscussionEntryHtmlConverter();
        DiscussionEntry discussionEntry = new DiscussionEntry();
        discussionEntry.setUnread(false);

        String testValue = converter.getReadState(discussionEntry);

        assertEquals("read", testValue);
    }

    @Test
    public void getReadState_unread() throws Exception {
        DiscussionEntryHtmlConverter converter = new DiscussionEntryHtmlConverter();
        DiscussionEntry discussionEntry = new DiscussionEntry();
        discussionEntry.setUnread(true);

        String testValue = converter.getReadState(discussionEntry);

        assertEquals("unread", testValue);
    }

}
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

import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class UnreadNotificationCountUnitTest : Assert() {

    @Test
    fun testUnreadNotificationCount() {
        val unreadNotificationCount: Array<UnreadNotificationCount> = unreadNotificationCountJSON.parse()

        Assert.assertNotNull(unreadNotificationCount)
        Assert.assertEquals(unreadNotificationCount.size, 5)

        for (unc in unreadNotificationCount) {
            Assert.assertTrue(unc.count > 0)
            Assert.assertTrue(unc.unreadCount > 0)
            Assert.assertNotNull(unc.notificationCategory)
            Assert.assertNotNull(unc.type)
        }
    }

    @Language("JSON")
    private var unreadNotificationCountJSON = """
      [
        {
          "type": "Announcement",
          "count": 1,
          "unread_count": 1,
          "notification_category": "null"
        },
        {
          "type": "DiscussionTopic",
          "count": 17,
          "unread_count": 9,
          "notification_category": "null"
        },
        {
          "type": "Message",
          "count": 10,
          "unread_count": 10,
          "notification_category": "Due Date"
        },
        {
          "type": "Message",
          "count": 8,
          "unread_count": 10,
          "notification_category": "Late Grading"
        },
        {
          "type": "Submission",
          "count": 5,
          "unread_count": 10,
          "notification_category": "null"
        }
      ]"""

}

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
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class DiscussionEntryUnitTest : Assert() {

    @Test
    fun testDiscussionEntry() {
        val longIdTopic: DiscussionTopic = longIdJson.parse()
        Assert.assertNotNull(longIdTopic)
        for (discussionEntry in longIdTopic.views) {
            testDiscussionEntryView(discussionEntry)
        }

        val stringIdTopic: DiscussionTopic = stringIdJson.parse()
        for (discussionEntry in stringIdTopic.views) {
            testDiscussionEntryView(discussionEntry)
        }
    }

    private fun testDiscussionEntryView(discussionEntry: DiscussionEntry) {
        Assert.assertNotNull(discussionEntry)
        Assert.assertTrue(discussionEntry.id > 0)

        discussionEntry.replies?.let { replies ->
            for (reply in replies) {
                testDiscussionEntryView(reply)
            }
        }

        Assert.assertTrue(discussionEntry.userId > 0)
        Assert.assertNotNull(discussionEntry.createdAt)
        Assert.assertNotNull(discussionEntry.updatedAt)
    }

    @Language("JSON")
    private var longIdJson = """
      {
        "unread_entries": [],
        "forced_entries": [],
        "participants": [
          {
            "id": 3828648,
            "display_name": "Drip Derskey",
            "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/32957548/krblSV5HHvhqqlxUCtvAsR6AkGMI21qsw8i2y1Tx",
            "html_url": "https://mobiledev.instructure.com/courses/24219/users/3828648"
          }
        ],
        "view": [
          {
            "created_at": "2013-05-29T15:50:24Z",
            "id": 5203752,
            "parent_id": null,
            "updated_at": "2013-05-29T15:50:24Z",
            "user_id": 3828648,
            "message": "Clojure1!!11!!"
          },
          {
            "created_at": "2013-05-29T15:51:18Z",
            "id": 5203767,
            "parent_id": null,
            "updated_at": "2013-05-29T15:51:18Z",
            "user_id": 3828648,
            "message": "I mean: Clojure is the best programming language."
          }
        ],
        "new_entries": []
      }"""

    @Language("JSON")
    private var stringIdJson = """
      {
        "unread_entries": [],
        "forced_entries": [],
        "participants": [
          {
            "id": 3828648,
            "display_name": "Drip Derskey",
            "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/32957548/krblSV5HHvhqqlxUCtvAsR6AkGMI21qsw8i2y1Tx",
            "html_url": "https://mobiledev.instructure.com/courses/24219/users/3828648"
          }
        ],
        "view": [
          {
            "created_at": "2013-05-29T15:50:24Z",
            "id": "5203752",
            "parent_id": null,
            "updated_at": "2013-05-29T15:50:24Z",
            "user_id": "3828648",
            "message": "Clojure1!!11!!"
          },
          {
            "created_at": "2013-05-29T15:51:18Z",
            "id": "5203767",
            "parent_id": null,
            "updated_at": "2013-05-29T15:51:18Z",
            "user_id": "3828648",
            "message": "I mean: Clojure is the best programming language."
          }
        ],
        "new_entries": []
      }"""

}

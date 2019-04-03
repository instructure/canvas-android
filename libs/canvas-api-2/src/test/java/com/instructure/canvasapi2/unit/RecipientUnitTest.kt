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

import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class RecipientUnitTest : Assert() {

    @Test
    fun testRecipient() {
        val recipients: Array<Recipient> = recipientJSON.parse()

        Assert.assertNotNull(recipients)

        for (recipient in recipients) {
            Assert.assertNotNull(recipient)

            Assert.assertNotNull(recipient.name)
            Assert.assertNotNull(recipient.avatarURL)
            Assert.assertNotNull(recipient.recipientType)
            Assert.assertNotNull(recipient.stringId)
        }
    }

    @Language("JSON")
    private var recipientJSON = """
      [
        {
          "id": "course_1016013",
          "name": "An In-Depth Study of the Year 2000",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 4,
          "permissions": {}
        },
        {
          "id": "course_833052",
          "name": "Android Development",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 43,
          "permissions": {}
        },
        {
          "id": "course_953090",
          "name": "Android Unit Tests",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 4,
          "permissions": {}
        },
        {
          "id": "course_1279999",
          "name": "Candroid",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 10,
          "permissions": {}
        },
        {
          "id": "section_892683",
          "name": "Advanced",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 6,
          "permissions": {},
          "context_name": "Android Development"
        },
        {
          "id": "section_889720",
          "name": "Android Development",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 34,
          "permissions": {},
          "context_name": "Android Development"
        },
        {
          "id": "group_220118",
          "name": "Add another one",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 4,
          "permissions": {},
          "context_name": "Beginning iOS Development"
        },
        {
          "id": "group_155489",
          "name": "Sorry guys, another group",
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "type": "context",
          "user_count": 4,
          "permissions": {},
          "context_name": "Beginning iOS Development"
        },
        {
          "id": 5803223,
          "name": "acannon+s@instructure.com",
          "common_courses": {
            "24219": [
              "StudentEnrollment"
            ]
          },
          "common_groups": {
            "220118": [
              "Member"
            ]
          },
          "avatar_url": "https://secure.gravatar.com/avatar/827ab0b5176ee8ce02b780b272dbf857?s=50&d=https%3A%2F%2Fcanvas.instructure.com%2Fimages%2Fmessages%2Favatar-50.png"
        },
        {
          "id": 5803222,
          "name": "acannon+t@instructure.com",
          "common_courses": {
            "24219": [
              "TeacherEnrollment"
            ]
          },
          "common_groups": {},
          "avatar_url": "https://secure.gravatar.com/avatar/452e281f449f50719c4fca1b06289bbb?s=50&d=https%3A%2F%2Fcanvas.instructure.com%2Fimages%2Fmessages%2Favatar-50.png"
        }
      ]"""
}

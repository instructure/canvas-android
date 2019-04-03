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

import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class UserUnitTest : Assert() {

    @Test
    fun testUser() {
        val user: User = userJSON.parse()
        
        Assert.assertEquals(user.avatarUrl, "https://www.example.com")
        Assert.assertEquals(user.id, 1111)
        Assert.assertEquals(user.primaryEmail, "primary_email")
        Assert.assertEquals(user.loginId, "login_id")
        Assert.assertEquals(user.name, "Sam Franklen")
        Assert.assertEquals(user.shortName, "Samf")
    }

    @Language("JSON")
    private val userJSON = """
      {
        "id": 1111,
        "name": "Sam Franklen",
        "short_name": "Samf",
        "sortable_name": "Franklen, Sam",
        "login_id": "login_id",
        "avatar_url": "https://www.example.com",
        "title": null,
        "bio": null,
        "primary_email": "primary_email",
        "time_zone": "America/Denver",
        "calendar": {
          "ics": "https://mobiledev.instructure.com/feeds/calendars/user_8JCkdINx6RO3dB8Ao5aPQCJO49p8XUpCbZgmqk7X.ics"
        }
      }"""
}

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

import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class PageUnitTest : Assert() {

    @Test
    fun testPage() {
        val page: Page = pageJSON.parse()

        Assert.assertNotNull(page)
        Assert.assertEquals(page.body, "body")
        Assert.assertNotNull(page.createdAt)
        Assert.assertEquals(page.title, "Front Page")
        Assert.assertNotNull(page.updatedAt)
        Assert.assertEquals(page.url, "front-page")
        Assert.assertFalse(page.hideFromStudents)
    }

    @Language("JSON")
    private var pageJSON = """
      {
        "created_at": "2011-01-10T08:26:38-07:00",
        "editing_roles": "teachers",
        "hide_from_students": false,
        "title": "Front Page",
        "updated_at": "2013-05-29T13:23:43-06:00",
        "url": "front-page",
        "last_edited_by": {
          "id": 170000003828513,
          "display_name": "Derrick Hathaway",
          "avatar_image_url": "https://secure.gravatar.com/avatar/1753d19b1ddf16cb0a31d983f97f4488?s=50\u0026d=https%3A%2F%2Fmobiledev.instructure.com%2Fimages%2Fdotted_pic.png",
          "html_url": "https://mobiledev.instructure.com/courses/24219/users/17~3828513"
        },
        "published": true,
        "front_page": true,
        "html_url": "https://mobiledev.instructure.com/courses/24219/wiki/front-page",
        "locked_for_user": false,
        "body": "body"
      }"""

}

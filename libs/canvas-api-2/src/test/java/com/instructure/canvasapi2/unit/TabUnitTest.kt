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

import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class TabUnitTest : Assert() {

    @Test
    fun testTabs() {
        val tabs: Array<Tab> = tabJSON.parse()

        Assert.assertNotNull(tabs)

        for (tab in tabs) {
            Assert.assertNotNull(tab)
            Assert.assertNotNull(tab.type)
            Assert.assertNotNull(tab.externalUrl)
            Assert.assertNotNull(tab.label)
            Assert.assertNotNull(tab.tabId)
        }
    }

    @Language("JSON")
    private var tabJSON = """
      [
        {
          "id": "home",
          "html_url": "/courses/833052",
          "full_url": "https://mobiledev.instructure.com/courses/833052",
          "position": 1,
          "visibility": "public",
          "label": "Home",
          "type": "internal"
        },
        {
          "id": "syllabus",
          "html_url": "/courses/833052/assignments/syllabus",
          "full_url": "https://mobiledev.instructure.com/courses/833052/assignments/syllabus",
          "position": 2,
          "visibility": "public",
          "label": "Syllabus",
          "type": "internal"
        },
        {
          "id": "people",
          "html_url": "/courses/833052/users",
          "full_url": "https://mobiledev.instructure.com/courses/833052/users",
          "position": 3,
          "visibility": "public",
          "label": "People",
          "type": "internal"
        },
        {
          "id": "discussions",
          "html_url": "/courses/833052/discussion_topics",
          "full_url": "https://mobiledev.instructure.com/courses/833052/discussion_topics",
          "position": 4,
          "visibility": "public",
          "label": "Discussions",
          "type": "internal"
        },
        {
          "id": "modules",
          "html_url": "/courses/833052/modules",
          "full_url": "https://mobiledev.instructure.com/courses/833052/modules",
          "position": 5,
          "visibility": "public",
          "label": "Modules",
          "type": "internal"
        },
        {
          "id": "assignments",
          "html_url": "/courses/833052/assignments",
          "full_url": "https://mobiledev.instructure.com/courses/833052/assignments",
          "position": 6,
          "visibility": "public",
          "label": "Assignments",
          "type": "internal"
        },
        {
          "id": "conferences",
          "html_url": "/courses/833052/conferences",
          "full_url": "https://mobiledev.instructure.com/courses/833052/conferences",
          "position": 7,
          "visibility": "public",
          "label": "Conferences",
          "type": "internal"
        },
        {
          "id": "grades",
          "html_url": "/courses/833052/grades",
          "full_url": "https://mobiledev.instructure.com/courses/833052/grades",
          "position": 8,
          "visibility": "public",
          "label": "Grades",
          "type": "internal"
        },
        {
          "id": "quizzes",
          "html_url": "/courses/833052/quizzes",
          "full_url": "https://mobiledev.instructure.com/courses/833052/quizzes",
          "position": 9,
          "visibility": "public",
          "label": "Quizzes",
          "type": "internal"
        },
        {
          "id": "announcements",
          "html_url": "/courses/833052/announcements",
          "full_url": "https://mobiledev.instructure.com/courses/833052/announcements",
          "position": 10,
          "visibility": "public",
          "label": "Announcements",
          "type": "internal"
        },
        {
          "id": "files",
          "html_url": "/courses/833052/files",
          "full_url": "https://mobiledev.instructure.com/courses/833052/files",
          "position": 11,
          "visibility": "public",
          "label": "Files",
          "type": "internal"
        },
        {
          "id": "collaborations",
          "html_url": "/courses/833052/collaborations",
          "full_url": "https://mobiledev.instructure.com/courses/833052/collaborations",
          "position": 12,
          "visibility": "public",
          "label": "Collaborations",
          "type": "internal"
        },
        {
          "id": "context_external_tool_131971",
          "html_url": "/courses/833052/external_tools/131971",
          "full_url": "https://mobiledev.instructure.com/courses/833052/external_tools/131971",
          "position": 13,
          "visibility": "public",
          "label": "Redirect Tool",
          "type": "external",
          "url": "https://mobiledev.instructure.com/api/v1/courses/833052/external_tools/sessionless_launch?id=131971&launch_type=course_navigation"
        }
      ]"""
}

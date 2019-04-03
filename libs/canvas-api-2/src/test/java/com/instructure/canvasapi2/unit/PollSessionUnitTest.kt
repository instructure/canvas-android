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

import com.instructure.canvasapi2.models.PollSession
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class PollSessionUnitTest : Assert() {

    @Test
    fun testPollSession() {
        val pollSessions: Array<PollSession> = pollSessionJSON.parse()

        Assert.assertNotNull(pollSessions)

        for (pollSession in pollSessions) {
            Assert.assertNotNull(pollSession)
            Assert.assertNotNull(pollSession.createdAt)
            Assert.assertTrue(pollSession.id > 0)
            Assert.assertTrue(pollSession.courseId > 0)
            Assert.assertTrue(pollSession.courseSectionId > 0)
            Assert.assertTrue(pollSession.pollId > 0)
        }
    }

    @Language("JSON")
    private var pollSessionJSON = """
      [
        {
          "id": "1230",
          "is_published": true,
          "course_id": "833052",
          "course_section_id": "892683",
          "created_at": "2015-03-17T15:52:23Z",
          "poll_id": "712",
          "has_submitted": false,
          "poll_submissions": [],
          "has_public_results": false,
          "results": {}
        },
        {
          "id": "1229",
          "is_published": true,
          "course_id": "833052",
          "course_section_id": "892682",
          "created_at": "2015-03-17T15:52:23Z",
          "poll_id": "712",
          "has_submitted": false,
          "poll_submissions": [],
          "has_public_results": false,
          "results": {}
        },
        {
          "id": "1227",
          "is_published": true,
          "course_id": "833052",
          "course_section_id": "889720",
          "created_at": "2015-03-17T15:52:22Z",
          "poll_id": "712",
          "has_submitted": false,
          "poll_submissions": [],
          "has_public_results": false,
          "results": {}
        },
        {
          "id": "1228",
          "is_published": true,
          "course_id": "833052",
          "course_section_id": "1772044",
          "created_at": "2015-03-17T15:52:22Z",
          "poll_id": "712",
          "has_submitted": false,
          "poll_submissions": [],
          "has_public_results": false,
          "results": {}
        }
      ]"""
}

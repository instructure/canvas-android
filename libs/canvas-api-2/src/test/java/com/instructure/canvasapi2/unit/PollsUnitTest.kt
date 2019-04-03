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

import com.instructure.canvasapi2.models.Poll
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test


class PollsUnitTest : Assert() {

    @Test
    fun testPoll() {
        val polls: Array<Poll> = pollsJSON.parse()

        for (poll in polls) {
            Assert.assertNotNull(poll.createdAt)
            Assert.assertNotNull(poll.question)
            Assert.assertTrue(poll.id > 0)
        }
    }

    @Language("JSON")
    private var pollsJSON = """
      [
        {
          "id": "289",
          "question": "Jcjjdjjdd",
          "description": null,
          "created_at": "2014-08-19T15:34:06Z",
          "total_results": {},
          "user_id": "4599568"
        },
        {
          "id": "270",
          "question": "fewqfewq",
          "description": null,
          "created_at": "2014-08-11T20:16:44Z",
          "total_results": {},
          "user_id": "4599568"
        }
      ]"""

}

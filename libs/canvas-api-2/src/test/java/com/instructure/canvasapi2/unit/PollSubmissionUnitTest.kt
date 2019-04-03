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

import com.instructure.canvasapi2.models.PollSubmission
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class PollSubmissionUnitTest : Assert() {

    @Test
    fun testPollSubmission() {
        val pollSubmission: PollSubmission = pollSubmissionJSON.parse()

        Assert.assertNotNull(pollSubmission.createdAt)
        Assert.assertTrue(pollSubmission.id > 0)
        Assert.assertTrue(pollSubmission.pollChoiceId > 0)
        Assert.assertTrue(pollSubmission.userId > 0)
    }

    @Language("JSON")
    private var pollSubmissionJSON = """
      {
        "id": "7741",
        "poll_session_id": "1230",
        "poll_choice_id": "2212",
        "user_id": "3360251",
        "created_at": "2015-03-17T16:17:08Z"
      }"""
}

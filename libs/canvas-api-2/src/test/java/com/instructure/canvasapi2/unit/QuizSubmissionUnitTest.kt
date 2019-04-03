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

import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class QuizSubmissionUnitTest : Assert() {

    @Test
    fun testQuizSubmissions() {
        val quizSubmissions: Array<QuizSubmission> = quizSubmissionJSON.parse()

        Assert.assertNotNull(quizSubmissions)

        for (quizSubmission in quizSubmissions) {
            Assert.assertNotNull(quizSubmission)
            Assert.assertTrue(quizSubmission.attempt > 0)
            Assert.assertNotNull(quizSubmission.endAt)
            Assert.assertNotNull(quizSubmission.finishedAt)
            Assert.assertTrue(quizSubmission.id > 0)
            Assert.assertTrue(quizSubmission.keptScore > 0)
            Assert.assertTrue(quizSubmission.quizId > 0)
            Assert.assertTrue(quizSubmission.userId > 0)
            Assert.assertNotNull(quizSubmission.startedAt)
            Assert.assertNotNull(quizSubmission.workflowState)
            Assert.assertTrue(quizSubmission.timeSpent > 0)
            Assert.assertTrue(quizSubmission.quizPointsPossible > 0)
        }
    }

    @Language("JSON")
    private var quizSubmissionJSON = """
      [
        {
          "attempt": 4,
          "end_at": "2015-03-30T21:22:37Z",
          "extra_attempts": null,
          "extra_time": null,
          "finished_at": "2015-03-30T21:22:37Z",
          "fudge_points": null,
          "has_seen_results": null,
          "id": 2491257,
          "kept_score": 3,
          "manually_unlocked": null,
          "quiz_id": 757314,
          "quiz_points_possible": 5,
          "quiz_version": 29,
          "score": 0,
          "score_before_regrade": null,
          "started_at": "2015-03-30T21:09:37Z",
          "submission_id": 11193366,
          "user_id": 3360251,
          "validation_token": null,
          "workflow_state": "pending_review",
          "time_spent": 780,
          "attempts_left": -1,
          "questions_regraded_since_last_attempt": 0,
          "html_url": "https://mobiledev.instructure.com/courses/833052/quizzes/757314/submissions/2491257"
        }
      ]"""
}

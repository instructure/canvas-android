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

import com.google.gson.Gson;
import com.instructure.canvasapi.model.QuizSubmission;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class QuizSubmissionUnitTest extends Assert {

    @Test
    public void testQuizSubmissions() {
        Gson gson = CanvasRestAdapter.getGSONParser();

        QuizSubmission[] quizSubmissions = gson.fromJson(quizSubmissionJSON, QuizSubmission[].class);

        assertNotNull(quizSubmissions);

        for(QuizSubmission quizSubmission : quizSubmissions) {
            assertNotNull(quizSubmission);
            assertTrue(quizSubmission.getAttempt() > 0);
            assertNotNull(quizSubmission.getEndAt());
            assertNotNull(quizSubmission.getFinishedAt());
            assertTrue(quizSubmission.getId() > 0);
            assertTrue(quizSubmission.getKeptScore() > 0);
            assertTrue(quizSubmission.getQuizId() > 0);
            assertTrue(quizSubmission.getUserId() > 0);
            assertNotNull(quizSubmission.getStartedAt());
            assertNotNull(quizSubmission.getWorkflowState());
            assertTrue(quizSubmission.getTimeSpent() > 0);
            assertTrue(quizSubmission.getQuizPointsPossible() > 0);
        }
    }


    String quizSubmissionJSON = "[\n" +
            "{\n" +
            "\"attempt\": 4,\n" +
            "\"end_at\": \"2015-03-30T21:22:37Z\",\n" +
            "\"extra_attempts\": null,\n" +
            "\"extra_time\": null,\n" +
            "\"finished_at\": \"2015-03-30T21:22:37Z\",\n" +
            "\"fudge_points\": null,\n" +
            "\"has_seen_results\": null,\n" +
            "\"id\": 2491257,\n" +
            "\"kept_score\": 3,\n" +
            "\"manually_unlocked\": null,\n" +
            "\"quiz_id\": 757314,\n" +
            "\"quiz_points_possible\": 5,\n" +
            "\"quiz_version\": 29,\n" +
            "\"score\": 0,\n" +
            "\"score_before_regrade\": null,\n" +
            "\"started_at\": \"2015-03-30T21:09:37Z\",\n" +
            "\"submission_id\": 11193366,\n" +
            "\"user_id\": 3360251,\n" +
            "\"validation_token\": null,\n" +
            "\"workflow_state\": \"pending_review\",\n" +
            "\"time_spent\": 780,\n" +
            "\"attempts_left\": -1,\n" +
            "\"questions_regraded_since_last_attempt\": 0,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/quizzes/757314/submissions/2491257\"\n" +
            "}\n" +
            "]";
}

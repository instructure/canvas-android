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
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class QuizUnitTest extends Assert {

    @Test
    public void testQuiz() {
        Gson gson = CanvasRestAdapter.getGSONParser();

        Quiz quiz = gson.fromJson(quizJSON, Quiz.class);

        assertNotNull(quiz);

        assertNotNull(quiz.getDescription());

        assertNotNull(quiz.getType());

        assertNotNull(quiz.getTitle());

        assertNotNull(quiz.getUrl());

        assertTrue(quiz.getId() > 0);
    }

    String quizJSON = "{\"access_code\":null,\"allowed_attempts\":1,\"assignment_group_id\":5724,\"cant_go_back\":null,\"description\":\"\",\"due_at\":\"2011-01-14T23:08:00Z\",\"hide_results\":null,\"id\":11456,\"ip_filter\":null,\"lock_at\":\"2011-01-17T17:57:00Z\",\"one_question_at_a_time\":null,\"points_possible\":2,\"quiz_type\":\"assignment\",\"scoring_policy\":\"keep_highest\",\"show_correct_answers\":true,\"shuffle_answers\":false,\"time_limit\":null,\"title\":\"Cocoa quiz\",\"unlock_at\":\"2011-01-17T17:55:00Z\",\"html_url\":\"https://mobiledev.instructure.com/courses/24219/quizzes/11456\",\"mobile_url\":\"https://mobiledev.instructure.com/courses/24219/quizzes/11456?force_user=1\\u0026persist_headless=1\",\"question_count\":2,\"published\":true,\"locked_for_user\":false}";

}

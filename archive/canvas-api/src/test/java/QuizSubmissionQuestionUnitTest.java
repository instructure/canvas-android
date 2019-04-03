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
import com.instructure.canvasapi.model.QuizSubmissionQuestion;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class QuizSubmissionQuestionUnitTest extends Assert {

    @Test
    public void testQuizSubmissionQuestion() {
        Gson gson = CanvasRestAdapter.getGSONParser();

        QuizSubmissionQuestion[] quizSubmissionQuestions = gson.fromJson(quizSubmissionQuestionJSON, QuizSubmissionQuestion[].class);

        assertNotNull(quizSubmissionQuestions);

        for(QuizSubmissionQuestion quizSubmissionQuestion : quizSubmissionQuestions) {
            assertTrue(quizSubmissionQuestion.getId() > 0);
            assertTrue(quizSubmissionQuestion.getQuizId() > 0);
            assertTrue(quizSubmissionQuestion.getPosition() > 0);
            assertNotNull(quizSubmissionQuestion.getQuestionName());
            assertNotNull(quizSubmissionQuestion.getQuestionType());
            assertNotNull(quizSubmissionQuestion.getQuestionText());
        }
    }

    String quizSubmissionQuestionJSON = "[\n" +
            "{\n" +
                "\"assessment_question_id\": 95245838,\n" +
                "\"id\": 49815255,\n" +
                "\"position\": 1,\n" +
                "\"quiz_group_id\": null,\n" +
                "\"quiz_id\": 2565933,\n" +
                "\"question_name\": \"Question\",\n" +
                "\"question_type\": \"essay_question\",\n" +
                "\"question_text\": \"<p>Which of the Fast &amp; Furious movies is your favorite?</p>\",\n" +
                "\"matches\": null,\n" +
                "\"flagged\": false,\n" +
                "\"correct\": \"undefined\"\n" +
            "},\n" +
            "{\n" +
                "\"assessment_question_id\": 95271409,\n" +
                "\"id\": 49835224,\n" +
                "\"position\": 2,\n" +
                "\"quiz_group_id\": null,\n" +
                "\"quiz_id\": 2565933,\n" +
                "\"question_name\": \"Question\",\n" +
                "\"question_type\": \"essay_question\",\n" +
                "\"question_text\": \"<p>Who is this:</p>\\n<p><a href=\\\"https://secure.flickr.com/photos/45173781@N04/16927854371\\\"><img src=\\\"https://farm9.static.flickr.com/8718/16927854371_29371b2011.jpg\\\" alt=\\\"Furious 7 Photo Sequence: One Last Ride\\\" width=\\\"500\\\" height=\\\"211\\\"></a></p>\",\n" +
                "\"matches\": null\n" +
            "},\n" +
            "{\n" +
                "\"assessment_question_id\": 95271433,\n" +
                "\"id\": 49835228,\n" +
                "\"position\": 3,\n" +
                "\"quiz_group_id\": null,\n" +
                "\"quiz_id\": 2565933,\n" +
                "\"question_name\": \"A tough one:\",\n" +
                "\"question_type\": \"essay_question\",\n" +
                "\"question_text\": \"<p><strong>Bold</strong></p>\\n<p><em>Italic</em></p>\\n<p><span style=\\\"text-decoration: underline;\\\">Underline</span></p>\\n<p><span style=\\\"color: #ff0000;\\\">Red</span></p>\",\n" +
                "\"matches\": null\n" +
            "}\n" +
            "]";
}

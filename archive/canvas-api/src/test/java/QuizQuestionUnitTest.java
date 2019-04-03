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
import com.instructure.canvasapi.model.QuizQuestion;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class QuizQuestionUnitTest extends Assert {


    @Test
    public void testQuizQuestion() {
        Gson gson = CanvasRestAdapter.getGSONParser();

        QuizQuestion quizQuestion = gson.fromJson(quizQuestionJSON, QuizQuestion.class);

        assertNotNull(quizQuestion);

        assertTrue(quizQuestion.getId() > 0);
        assertTrue(quizQuestion.getQuizId() > 0);
        assertTrue(quizQuestion.getPosition() > 0);
        assertNotNull(quizQuestion.getQuestionName());
        assertNotNull(quizQuestion.getQuestionType());
        assertNotNull(quizQuestion.getAnswers());
    }

    String quizQuestionJSON = "{\n" +
            "\"assessment_question_id\": 94459442,\n" +
            "\"id\": 49428890,\n" +
            "\"position\": 1,\n" +
            "\"quiz_group_id\": 1333825,\n" +
            "\"quiz_id\": 2539536,\n" +
            "\"question_name\": \"Multi Guess\",\n" +
            "\"question_type\": \"multiple_choice_question\",\n" +
            "\"question_text\": \"<p>Pick one</p>\",\n" +
            "\"points_possible\": 1,\n" +
            "\"correct_comments\": \"\",\n" +
            "\"incorrect_comments\": \"\",\n" +
            "\"neutral_comments\": \"\",\n" +
            "\"answers\": [\n" +
                "{\n" +
                "\"id\": 6266,\n" +
                "\"text\": \"A\",\n" +
                "\"html\": \"\",\n" +
                "\"comments\": \"\",\n" +
                "\"weight\": 100\n" +
                "},\n" +
                "{\n" +
                "\"id\": 8595,\n" +
                "\"text\": \"B\",\n" +
                "\"html\": \"\",\n" +
                "\"comments\": \"\",\n" +
                "\"weight\": 0\n" +
                "},\n" +
                "{\n" +
                "\"id\": 6695,\n" +
                "\"text\": \"C\",\n" +
                "\"html\": \"\",\n" +
                "\"comments\": \"\",\n" +
                "\"weight\": 0\n" +
                "},\n" +
                "{\n" +
                "\"id\": 9929,\n" +
                "\"text\": \"D\",\n" +
                "\"html\": \"\",\n" +
                "\"comments\": \"\",\n" +
                "\"weight\": 0\n" +
                "}\n" +
                "],\n" +
            "\"variables\": null,\n" +
            "\"formulas\": null,\n" +
            "\"matches\": null,\n" +
            "\"matching_answer_incorrect_matches\": null" +
            "}";
}

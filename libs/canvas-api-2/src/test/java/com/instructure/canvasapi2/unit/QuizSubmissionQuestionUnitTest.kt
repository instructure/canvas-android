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

import com.instructure.canvasapi2.models.QuizSubmissionQuestion
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class QuizSubmissionQuestionUnitTest : Assert() {

    @Test
    fun testQuizSubmissionQuestion() {

        val quizSubmissionQuestions: Array<QuizSubmissionQuestion> = quizSubmissionQuestionJSON.parse()

        Assert.assertNotNull(quizSubmissionQuestions)

        for (quizSubmissionQuestion in quizSubmissionQuestions) {
            Assert.assertTrue(quizSubmissionQuestion.id > 0)
            Assert.assertTrue(quizSubmissionQuestion.quizId > 0)
            Assert.assertTrue(quizSubmissionQuestion.position > 0)
            Assert.assertNotNull(quizSubmissionQuestion.questionName)
            Assert.assertNotNull(quizSubmissionQuestion.questionType)
            Assert.assertNotNull(quizSubmissionQuestion.questionText)
        }
    }

    @Language("JSON")
    private var quizSubmissionQuestionJSON = """
      [
        {
          "assessment_question_id": 95245838,
          "id": 49815255,
          "position": 1,
          "quiz_group_id": null,
          "quiz_id": 2565933,
          "question_name": "Question",
          "question_type": "essay_question",
          "question_text": "<p>Which of the Fast &amp; Furious movies is your favorite?</p>",
          "matches": null,
          "flagged": false,
          "correct": "undefined"
        },
        {
          "assessment_question_id": 95271409,
          "id": 49835224,
          "position": 2,
          "quiz_group_id": null,
          "quiz_id": 2565933,
          "question_name": "Question",
          "question_type": "essay_question",
          "question_text": "<p>Who is this:</p>\n<p><a href=\"https://secure.flickr.com/photos/45173781@N04/16927854371\"><img src=\"https://farm9.static.flickr.com/8718/16927854371_29371b2011.jpg\" alt=\"Furious 7 Photo Sequence: One Last Ride\" width=\"500\" height=\"211\"></a></p>",
          "matches": null
        },
        {
          "assessment_question_id": 95271433,
          "id": 49835228,
          "position": 3,
          "quiz_group_id": null,
          "quiz_id": 2565933,
          "question_name": "A tough one:",
          "question_type": "essay_question",
          "question_text": "<p><strong>Bold</strong></p>\n<p><em>Italic</em></p>\n<p><span style=\"text-decoration: underline;\">Underline</span></p>\n<p><span style=\"color: #ff0000;\">Red</span></p>",
          "matches": null
        }
      ]"""
}

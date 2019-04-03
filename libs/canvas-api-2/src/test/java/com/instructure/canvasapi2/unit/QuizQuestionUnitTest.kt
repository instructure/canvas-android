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

import com.instructure.canvasapi2.models.QuizQuestion
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class QuizQuestionUnitTest : Assert() {

    @Test
    fun testQuizQuestion() {
        val quizQuestion: QuizQuestion = quizQuestionJSON.parse()

        Assert.assertNotNull(quizQuestion)
        Assert.assertTrue(quizQuestion.id > 0)
        Assert.assertTrue(quizQuestion.quizId > 0)
        Assert.assertTrue(quizQuestion.position > 0)
        Assert.assertNotNull(quizQuestion.questionName)
        Assert.assertNotNull(quizQuestion.questionType)
        Assert.assertNotNull(quizQuestion.answers)
    }

    @Language("JSON")
    private var quizQuestionJSON = """
      {
        "assessment_question_id": 94459442,
        "id": 49428890,
        "position": 1,
        "quiz_group_id": 1333825,
        "quiz_id": 2539536,
        "question_name": "Multi Guess",
        "question_type": "multiple_choice_question",
        "question_text": "<p>Pick one</p>",
        "points_possible": 1,
        "correct_comments": "",
        "incorrect_comments": "",
        "neutral_comments": "",
        "answers": [
          {
            "id": 6266,
            "text": "A",
            "html": "",
            "comments": "",
            "weight": 100
          },
          {
            "id": 8595,
            "text": "B",
            "html": "",
            "comments": "",
            "weight": 0
          },
          {
            "id": 6695,
            "text": "C",
            "html": "",
            "comments": "",
            "weight": 0
          },
          {
            "id": 9929,
            "text": "D",
            "html": "",
            "comments": "",
            "weight": 0
          }
        ],
        "variables": null,
        "formulas": null,
        "matches": null,
        "matching_answer_incorrect_matches": null
      }"""
}

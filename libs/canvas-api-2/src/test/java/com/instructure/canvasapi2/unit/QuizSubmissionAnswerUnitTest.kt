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

import com.instructure.canvasapi2.models.QuizSubmissionAnswer
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class QuizSubmissionAnswerUnitTest : Assert() {

    @Test
    fun testQuizSubmissionAnswer() {

        val quizSubmissionAnswers: Array<QuizSubmissionAnswer> = quizSubmissionAnswerJSON.parse()

        Assert.assertNotNull(quizSubmissionAnswers)

        for (quizSubmissionAnswer in quizSubmissionAnswers) {
            Assert.assertTrue(quizSubmissionAnswer.id > 0)
            Assert.assertNotNull(quizSubmissionAnswer.text)
            Assert.assertNotNull(quizSubmissionAnswer.html)
            Assert.assertNotNull(quizSubmissionAnswer.comments)
            Assert.assertTrue(quizSubmissionAnswer.weight >= 0)
        }
    }

    @Language("JSON")
    private var quizSubmissionAnswerJSON = """
      [
        {
          "id": 7720,
          "text": "",
          "html": "<p>Mr. Ryan: Who was Joan of Arc?</p>\n<p>Ted: Noah's wife?</p>",
          "comments": "",
          "weight": 100
        },
        {
          "id": 8901,
          "text": "Daddy's got to go to work",
          "html": "",
          "comments": "",
          "weight": 0
        },
        {
          "id": 9650,
          "text": "Roads? Where we're going, we don't need roads.",
          "html": "",
          "comments": "",
          "weight": 0
        },
        {
          "id": 39,
          "text": "",
          "html": "",
          "comments": "",
          "weight": 0
        }
      ]"""

}

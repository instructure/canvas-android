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

import com.instructure.canvasapi2.models.QuizAnswer
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test


class QuizAnswerUnitTest : Assert() {

    @Test
    fun testQuizAnswers() {
        val quizAnswers: Array<QuizAnswer> = quizAnswerJSON.parse()

        Assert.assertNotNull(quizAnswers)

        for (quizAnswer in quizAnswers) {
            Assert.assertNotNull(quizAnswer)
            Assert.assertTrue(quizAnswer.id > 0)
            Assert.assertNotNull(quizAnswer.answerText)
            Assert.assertTrue(quizAnswer.answerWeight >= 0)
        }
    }

    @Language("JSON")
    private var quizAnswerJSON = """
      [
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
      ]"""
}

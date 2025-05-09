/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.CedarGraphQLClient
import com.instructure.canvasapi2.models.Page
import com.instructure.cedar.GenerateQuizMutation
import com.instructure.cedar.SayHelloQuery
import com.instructure.cedar.type.QuizInput
import javax.inject.Inject

data class GeneratedQuiz(
    val question: String,
    val options: List<String>,
    val result: Int
)

class CedarApiManager @Inject constructor(
    private val cedarClient: CedarGraphQLClient
) {
    suspend fun generateQuiz(
        context: String,
        numberOfQuestions: Int = 1,
        numberOfOptionsPerQuestion: Int = 4,
        maxLengthOfQuestions: Int = 100
    ): List<GeneratedQuiz> {
        val query = GenerateQuizMutation(
            QuizInput(context, numberOfQuestions.toDouble(), numberOfOptionsPerQuestion.toDouble(), maxLengthOfQuestions.toDouble())
        )
        val result = cedarClient.enqueueMutation(query).dataAssertNoErrors

        return result.generateQuiz.map {
            GeneratedQuiz(
                question = it.question,
                options = it.options,
                result = it.result.toInt()
            )
        }
    }

    suspend fun sayHello(): String {
        val query = SayHelloQuery()
        val result = cedarClient.enqueueQuery(query).dataAssertNoErrors.sayHello

        return result
    }
}

fun Page.toAiContext(): String {
    return """
        ${this.title}
        ${this.body}
    """.trimIndent()
}
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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.CedarGraphQLClientConfig
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.cedar.AnswerPromptMutation
import com.instructure.cedar.EvaluateTopicResponseMutation
import com.instructure.cedar.GenerateQuizMutation
import com.instructure.cedar.SayHelloQuery
import com.instructure.cedar.SummarizeContentMutation
import com.instructure.cedar.TranslateHTMLMutation
import com.instructure.cedar.TranslateTextMutation
import com.instructure.cedar.type.AIPrompt
import com.instructure.cedar.type.DocumentBlock
import com.instructure.cedar.type.EvaluateTopicResponseInput
import com.instructure.cedar.type.QuizInput
import com.instructure.cedar.type.SummarizeContentInput
import com.instructure.cedar.type.TranslateInput
import javax.inject.Inject

data class GeneratedQuiz(
    val question: String,
    val options: List<String>,
    val result: Int
)

data class EvaluatedTopic(
    val complianceStatus: String,
    val relevanceScore: Float,
    val qualityScore: Float,
    val finalLabel: String,
    val feedback: String
)

class CedarApiManager @Inject constructor(
    private val cedarClient: CedarGraphQLClientConfig
) {
    private val model: String = "anthropic.claude-3-sonnet-20240229-v1:0"
    suspend fun answerPrompt(
        prompt: String,
        documentBlock: DocumentBlock? = null
    ): String {
        val mutation = AnswerPromptMutation(
            AIPrompt(
                model = model,
                prompt = prompt,
                document = Optional.presentIfNotNull(documentBlock),
            )
        )
        val result = QLClientConfig
            .enqueueMutation(mutation, block = cedarClient.createClientConfigBlock())
            .dataAssertNoErrors.answerPrompt

        return result
    }

    suspend fun summarizeContent(
        content: String,
        numberOfParagraphs: Int = 1
    ): List<String> {
        val mutation = SummarizeContentMutation(
            SummarizeContentInput(
                content,
                numberOfParagraphs.toDouble(),
            )
        )
        val result = QLClientConfig
            .enqueueMutation(mutation, block = cedarClient.createClientConfigBlock())
            .dataAssertNoErrors.summarizeContent

        return result.summarization
    }

    suspend fun generateQuiz(
        context: String,
        numberOfQuestions: Int = 1,
        numberOfOptionsPerQuestion: Int = 4,
        maxLengthOfQuestions: Int = 100
    ): List<GeneratedQuiz> {
        val mutation = GenerateQuizMutation(
            QuizInput(context, numberOfQuestions.toDouble(), numberOfOptionsPerQuestion.toDouble(), maxLengthOfQuestions.toDouble())
        )
        val result = QLClientConfig
            .enqueueMutation(mutation, block = cedarClient.createClientConfigBlock())
            .dataAssertNoErrors

        return result.generateQuiz.map {
            GeneratedQuiz(
                question = it.question,
                options = it.options,
                result = it.result.toInt()
            )
        }
    }

    suspend fun evaluateTopicResponse(
        mainText: String,
        comparisonText: String,
    ): EvaluatedTopic {
        val mutation = EvaluateTopicResponseMutation(
            EvaluateTopicResponseInput(
                mainText = mainText,
                comparisonText = comparisonText,
            )
        )
        val result = QLClientConfig
            .enqueueMutation(mutation, block = cedarClient.createClientConfigBlock())
            .dataAssertNoErrors.evaluateTopicResponse

        return EvaluatedTopic(
            complianceStatus = result.complianceStatus,
            relevanceScore = result.relevanceScore.toFloat(),
            qualityScore = result.qualityScore.toFloat(),
            finalLabel = result.finalLabel,
            feedback = result.feedback
        )
    }

    suspend fun translateText(
        text: String,
        targetLanguage: String,
        sourceLanguage: String? = null
    ): String {
        val mutation = TranslateTextMutation(
            TranslateInput(
                content = text,
                targetLanguage = targetLanguage,
                sourceLanguage = Optional.presentIfNotNull(sourceLanguage)
            )
        )
        val result = QLClientConfig
            .enqueueMutation(mutation, block = cedarClient.createClientConfigBlock())
            .dataAssertNoErrors.translateText

        return result.translation
    }

    suspend fun translateHTML(
        text: String,
        targetLanguage: String,
        sourceLanguage: String? = null
    ): String {
        val mutation = TranslateHTMLMutation(
            TranslateInput(
                content = text,
                targetLanguage = targetLanguage,
                sourceLanguage = Optional.presentIfNotNull(sourceLanguage)
            )
        )
        val result = QLClientConfig
            .enqueueMutation(mutation, block = cedarClient.createClientConfigBlock())
            .dataAssertNoErrors.translateHTML
        return result.translation
    }

    suspend fun sayHello(): String {
        val query = SayHelloQuery()
        val result = QLClientConfig
            .enqueueQuery(query, block = cedarClient.createClientConfigBlock())
            .dataAssertNoErrors.sayHello

        return result
    }
}
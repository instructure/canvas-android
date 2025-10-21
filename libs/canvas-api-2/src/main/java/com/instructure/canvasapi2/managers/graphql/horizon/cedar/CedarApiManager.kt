package com.instructure.canvasapi2.managers.graphql.horizon.cedar

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.di.CedarApolloClient
import com.instructure.canvasapi2.enqueueMutation
import com.instructure.canvasapi2.enqueueQuery
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
    @CedarApolloClient private val cedarClient: ApolloClient
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
        val result = cedarClient
            .enqueueMutation(mutation)
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
        val result = cedarClient
            .enqueueMutation(mutation)
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
            QuizInput(
                context,
                numberOfQuestions.toDouble(),
                numberOfOptionsPerQuestion.toDouble(),
                maxLengthOfQuestions.toDouble()
            )
        )
        val result = cedarClient
            .enqueueMutation(mutation)
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
        val result = cedarClient
            .enqueueMutation(mutation)
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
        val result = cedarClient
            .enqueueMutation(mutation)
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
        val result = cedarClient
            .enqueueMutation(mutation)
            .dataAssertNoErrors.translateHTML
        return result.translation
    }

    suspend fun sayHello(): String {
        val query = SayHelloQuery()
        val result = cedarClient
            .enqueueQuery(query)
            .dataAssertNoErrors.sayHello

        return result
    }
}
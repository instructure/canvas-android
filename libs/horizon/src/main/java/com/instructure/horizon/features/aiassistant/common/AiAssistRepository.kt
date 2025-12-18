package com.instructure.horizon.features.aiassistant.common

import com.instructure.canvasapi2.apis.JourneyAssistAPI
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistRequestBody
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
import java.util.UUID
import javax.inject.Inject

data class AiAssistResponse(
    val message: JourneyAssistChatMessage,
    val state: JourneyAssistState?
)

class AiAssistRepository @Inject constructor(
    private val journeyAssistAPI: JourneyAssistAPI,
) {
    suspend fun answerPrompt(
        prompt: String,
        history: List<JourneyAssistChatMessage>,
        state: JourneyAssistState
    ): AiAssistResponse {
        val requestBody = JourneyAssistRequestBody(prompt, history, state)
        val response = journeyAssistAPI.answerPrompt(requestBody)
        val message = JourneyAssistChatMessage(
            id = UUID.randomUUID().toString(),
            prompt = response.response.orEmpty(),
            text = response.response.orEmpty(),
            role = JourneyAssistRole.Assistant,
            chipOptions = response.chips,
            flashCards = response.flashCards,
            quizItems = response.quizItems,
            citations = response.citations
        )
        return AiAssistResponse(message, response.state)
    }
}
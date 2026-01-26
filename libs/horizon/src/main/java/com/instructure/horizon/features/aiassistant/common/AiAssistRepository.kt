package com.instructure.horizon.features.aiassistant.common

import com.instructure.canvasapi2.apis.JourneyAssistAPI
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistRequestBody
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import javax.inject.Inject

data class AiAssistResponse(
    val message: AiAssistMessage,
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
        val message = AiAssistMessage(
            text = response.response.orEmpty(),
            role = JourneyAssistRole.Assistant,
            chipOptions = response.chips,
            flashCards = response.flashCards,
            quizItems = response.quizItems,
            citations = response.citations,
            errorMessage = response.error,
        )
        return AiAssistResponse(message, response.state)
    }
}
package com.instructure.horizon.features.aiassistant.common

import com.instructure.canvasapi2.apis.JourneyAssistAPI
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistRequestBody
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
import java.util.UUID
import javax.inject.Inject

class AiAssistRepository @Inject constructor(
    private val journeyAssistAPI: JourneyAssistAPI,
) {
    suspend fun answerPrompt(
        prompt: String,
        history: List<JourneyAssistChatMessage>,
        state: JourneyAssistState
    ): JourneyAssistChatMessage {
        val requestBody = JourneyAssistRequestBody(prompt, history, state)
        val response = journeyAssistAPI.answerPrompt(requestBody)
        return JourneyAssistChatMessage(
            id = UUID.randomUUID().toString(),
            displayText = response.response.orEmpty(),
            role = JourneyAssistRole.ASSISTANT,
            chipOptions = response.chips,
            flashcards = response.flashCards,
            quizItems = response.quizItems,
            citations = response.citations
        )
    }
}
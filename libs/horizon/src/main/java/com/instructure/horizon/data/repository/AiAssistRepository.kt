/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.models.journey.assist.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistState
import com.instructure.horizon.data.datasource.AiAssistNetworkDataSource
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import javax.inject.Inject

data class AiAssistResponse(
    val message: AiAssistMessage,
    val state: JourneyAssistState?,
)

class AiAssistRepository @Inject constructor(
    private val networkDataSource: AiAssistNetworkDataSource,
) {
    suspend fun answerPrompt(
        prompt: String,
        history: List<JourneyAssistChatMessage>,
        state: JourneyAssistState,
    ): AiAssistResponse {
        val response = networkDataSource.answerPrompt(prompt, history, state)
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

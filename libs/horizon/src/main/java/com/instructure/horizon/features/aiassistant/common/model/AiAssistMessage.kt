/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.aiassistant.common.model

import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistChipOption
import com.instructure.canvasapi2.models.journey.JourneyAssistCitation
import com.instructure.canvasapi2.models.journey.JourneyAssistFlashCard
import com.instructure.canvasapi2.models.journey.JourneyAssistQuizItem
import com.instructure.canvasapi2.models.journey.JourneyAssistRole

data class AiAssistMessage(
    val text: String = "",
    val role: JourneyAssistRole,
    val chipOptions: List<JourneyAssistChipOption> = emptyList(),
    val flashCards: List<JourneyAssistFlashCard> = emptyList(),
    val quizItems: List<JourneyAssistQuizItem> = emptyList(),
    val citations: List<JourneyAssistCitation> = emptyList(),
    val errorMessage: String? = null
)

fun List<JourneyAssistChatMessage>.toAiAssistMessages(): List<AiAssistMessage> {
    return this.map {
        AiAssistMessage(
            text = it.text,
            role = it.role,
        )
    }
}

fun List<AiAssistMessage>.toJourneyAssistChatMessages(): List<JourneyAssistChatMessage> {
    return this.map {
        JourneyAssistChatMessage(
            text = it.text,
            role = it.role,
        )
    }
}
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
package com.instructure.horizon.features.aiassistant.flashcard

import com.instructure.canvasapi2.managers.CedarApiManager
import com.instructure.cedar.type.DocumentBlock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AiAssistFlashcardRepository @Inject constructor(
    private val cedarApi: CedarApiManager,
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun generateFlashcards(
        contextString: String,
        numberOfCards: Int = 5
    ): List<GeneratedFlashcard> {
        val prompt = "I'm creating flash cards. Give me $numberOfCards questions with answers based on the content. Return the result in JSON format like: [{question: '', answer: ''}, {question: '', answer: ''}] without any further description or text. Your flash cards should not refer to the format of the content, but rather the content itself."
        val documentBlock = DocumentBlock(
            format = "txt",
            base64Source = Base64.encode(contextString.toByteArray())
        )
        val rawJsonResponse = cedarApi.answerPrompt(prompt, documentBlock)

        val parsedCards = try {
            Json.decodeFromString<List<GeneratedFlashcard>>(rawJsonResponse)
        } catch (e: Exception) {
            emptyList()
        }

        return parsedCards
    }
}
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
package com.instructure.horizon.features.aiassistant.chat

import com.instructure.canvasapi2.managers.CedarApiManager
import com.instructure.canvasapi2.managers.DocumentSource
import com.instructure.canvasapi2.managers.PineApiManager
import com.instructure.cedar.type.DocumentBlock
import com.instructure.pine.type.MessageInput
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class AiAssistChatRepository @Inject constructor(
    private val cedarApi: CedarApiManager,
    private val pineApi: PineApiManager
) {
    suspend fun answerPrompt(prompt: String, contextString: String? = null): String {
        val document = contextString?.let {
            DocumentBlock(
                format = "txt",
                base64Source = Base64.encode(it.toByteArray())
            )
        }
        return cedarApi.answerPrompt(prompt, document)
    }

    suspend fun answerPrompt(
        messages: List<MessageInput>,
        context: Map<String, String>
    ): String {
        return pineApi.queryDocument(
            messages,
            DocumentSource.canvas,
            context
        )
    }

    suspend fun summarizePrompt(contextString: String, numberOfParagraphs: Int = 1): String {
        return cedarApi.summarizeContent(contextString, numberOfParagraphs).joinToString("\n")
    }
}
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
import com.instructure.pine.type.MessageInput
import com.instructure.pine.type.Role
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AiAssistChatRepositoryTest {
    private val cedarApi: CedarApiManager = mockk(relaxed = true)
    private val pineApi: PineApiManager = mockk(relaxed = true)

    @Test
    fun `Test answer prompt without context`() = runTest {
        val prompt = "What is 2+2?"
        val expectedResponse = "4"

        coEvery { cedarApi.answerPrompt(prompt, null) } returns expectedResponse

        val result = getRepository().answerPrompt(prompt)

        assertEquals(expectedResponse, result)
        coVerify { cedarApi.answerPrompt(prompt, null) }
    }

    @Test
    fun `Test answer prompt with context`() = runTest {
        val prompt = "Summarize this content"
        val context = "This is test content to summarize"
        val expectedResponse = "Summary of test content"

        coEvery { cedarApi.answerPrompt(any(), any()) } returns expectedResponse

        val result = getRepository().answerPrompt(prompt, context)

        assertEquals(expectedResponse, result)
        coVerify { cedarApi.answerPrompt(prompt, match { it != null }) }
    }

    @Test
    fun `Test answer prompt with messages and context map`() = runTest {
        val messages = listOf(
            MessageInput(role = Role.User, text = "Test message")
        )
        val context = mapOf("courseId" to "123")
        val expectedResponse = "AI response"

        coEvery { pineApi.queryDocument(messages, DocumentSource.canvas, context) } returns expectedResponse

        val result = getRepository().answerPrompt(messages, context)

        assertEquals(expectedResponse, result)
    }

    @Test
    fun `Test summarize prompt with default paragraphs`() = runTest {
        val contextString = "Long content to summarize..."
        val summaryParagraphs = listOf("Summary paragraph 1", "Summary paragraph 2")
        val expectedResponse = summaryParagraphs.joinToString("\n")

        coEvery { cedarApi.summarizeContent(contextString, 1) } returns summaryParagraphs

        val result = getRepository().summarizePrompt(contextString)

        assertEquals(expectedResponse, result)
        coVerify { cedarApi.summarizeContent(contextString, 1) }
    }

    @Test
    fun `Test summarize prompt with custom paragraph count`() = runTest {
        val contextString = "Long content to summarize..."
        val numberOfParagraphs = 3
        val summaryParagraphs = listOf("Para 1", "Para 2", "Para 3")
        val expectedResponse = summaryParagraphs.joinToString("\n")

        coEvery { cedarApi.summarizeContent(contextString, numberOfParagraphs) } returns summaryParagraphs

        val result = getRepository().summarizePrompt(contextString, numberOfParagraphs)

        assertEquals(expectedResponse, result)
        coVerify { cedarApi.summarizeContent(contextString, numberOfParagraphs) }
    }

    @Test
    fun `Test empty summary returns empty string`() = runTest {
        val contextString = "Content"
        coEvery { cedarApi.summarizeContent(contextString, 1) } returns emptyList()

        val result = getRepository().summarizePrompt(contextString)

        assertEquals("", result)
    }

    @Test
    fun `Test prompt with empty context string`() = runTest {
        val prompt = "Test prompt"
        val expectedResponse = "Response"

        coEvery { cedarApi.answerPrompt(prompt, null) } returns expectedResponse

        val result = getRepository().answerPrompt(prompt, null)

        assertEquals(expectedResponse, result)
    }

    private fun getRepository(): AiAssistChatRepository {
        return AiAssistChatRepository(cedarApi, pineApi)
    }
}

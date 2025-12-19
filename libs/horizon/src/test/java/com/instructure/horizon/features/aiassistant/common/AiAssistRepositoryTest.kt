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
package com.instructure.horizon.features.aiassistant.common

import com.instructure.canvasapi2.apis.JourneyAssistAPI
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistChipOption
import com.instructure.canvasapi2.models.journey.JourneyAssistCitation
import com.instructure.canvasapi2.models.journey.JourneyAssistCitationType
import com.instructure.canvasapi2.models.journey.JourneyAssistFlashCard
import com.instructure.canvasapi2.models.journey.JourneyAssistQuizItem
import com.instructure.canvasapi2.models.journey.JourneyAssistRequestBody
import com.instructure.canvasapi2.models.journey.JourneyAssistResponse
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class AiAssistRepositoryTest {
    private val journeyAssistAPI: JourneyAssistAPI = mockk(relaxed = true)
    private lateinit var repository: AiAssistRepository

    private val testState = JourneyAssistState(
        courseID = "123",
        fileID = "456",
        pageID = "789"
    )

    private val testChips = listOf(
        JourneyAssistChipOption("Summarize", "Please summarize this"),
        JourneyAssistChipOption("Explain", "Explain this concept")
    )

    private val testFlashCards = listOf(
        JourneyAssistFlashCard("What is Kotlin?", "A programming language"),
        JourneyAssistFlashCard("What is Android?", "A mobile operating system")
    )

    private val testQuizItems = listOf(
        JourneyAssistQuizItem(
            question = "What is 2+2?",
            answers = listOf("3", "4", "5"),
            correctAnswerIndex = 1
        )
    )

    private val testCitations = listOf(
        JourneyAssistCitation("Source 1", "123", "page-1", JourneyAssistCitationType.WIKI_PAGE),
        JourneyAssistCitation("Source 2", "123", "file-1", JourneyAssistCitationType.ATTACHMENT)
    )

    @Before
    fun setup() {
        repository = AiAssistRepository(journeyAssistAPI)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `answerPrompt returns correct message with all fields`() = runTest {
        val apiResponse = JourneyAssistResponse(
            state = testState,
            response = "This is the AI response",
            chips = testChips,
            flashCards = testFlashCards,
            quizItems = testQuizItems,
            citations = testCitations
        )

        coEvery {
            journeyAssistAPI.answerPrompt(any())
        } returns apiResponse

        val result = repository.answerPrompt(
            prompt = "Test prompt",
            history = emptyList(),
            state = testState
        )

        assertNotNull(result.message.id)
        assertEquals("This is the AI response", result.message.text)
        assertEquals("This is the AI response", result.message.prompt)
        assertEquals(JourneyAssistRole.Assistant, result.message.role)
        assertEquals(testChips, result.message.chipOptions)
        assertEquals(testFlashCards, result.message.flashCards)
        assertEquals(testQuizItems, result.message.quizItems)
        assertEquals(testCitations, result.message.citations)
        assertEquals(testState, result.state)
    }

    @Test
    fun `answerPrompt handles empty response`() = runTest {
        val apiResponse = JourneyAssistResponse(
            state = testState,
            response = null
        )

        coEvery {
            journeyAssistAPI.answerPrompt(any())
        } returns apiResponse

        val result = repository.answerPrompt(
            prompt = "Test prompt",
            history = emptyList(),
            state = testState
        )

        assertEquals("", result.message.text)
        assertEquals("", result.message.prompt)
    }

    @Test
    fun `answerPrompt passes correct request body to API`() = runTest {
        val prompt = "What is Kotlin?"
        val history = listOf(
            JourneyAssistChatMessage(
                id = "1",
                text = "Previous question",
                prompt = "Previous question",
                role = JourneyAssistRole.User
            ),
            JourneyAssistChatMessage(
                id = "2",
                text = "Previous answer",
                prompt = "Previous answer",
                role = JourneyAssistRole.Assistant
            )
        )

        coEvery {
            journeyAssistAPI.answerPrompt(any())
        } returns JourneyAssistResponse(response = "Response")

        repository.answerPrompt(prompt, history, testState)

        coVerify {
            journeyAssistAPI.answerPrompt(
                match { requestBody ->
                    requestBody.prompt == prompt &&
                    requestBody.history == history &&
                    requestBody.state == testState
                }
            )
        }
    }

    @Test
    fun `answerPrompt returns state from API response`() = runTest {
        val updatedState = JourneyAssistState(
            courseID = "999",
            fileID = "888",
            pageID = "777"
        )

        coEvery {
            journeyAssistAPI.answerPrompt(any())
        } returns JourneyAssistResponse(
            response = "Response",
            state = updatedState
        )

        val result = repository.answerPrompt(
            prompt = "Test",
            history = emptyList(),
            state = testState
        )

        assertEquals(updatedState, result.state)
    }

    @Test
    fun `answerPrompt handles null state in response`() = runTest {
        coEvery {
            journeyAssistAPI.answerPrompt(any())
        } returns JourneyAssistResponse(
            response = "Response",
            state = null
        )

        val result = repository.answerPrompt(
            prompt = "Test",
            history = emptyList(),
            state = testState
        )

        assertEquals(null, result.state)
    }

    @Test
    fun `answerPrompt with empty history`() = runTest {
        coEvery {
            journeyAssistAPI.answerPrompt(any())
        } returns JourneyAssistResponse(response = "Response")

        repository.answerPrompt(
            prompt = "First question",
            history = emptyList(),
            state = testState
        )

        coVerify {
            journeyAssistAPI.answerPrompt(
                match { it.history.isEmpty() }
            )
        }
    }

    @Test
    fun `answerPrompt generates unique message ID`() = runTest {
        coEvery {
            journeyAssistAPI.answerPrompt(any())
        } returns JourneyAssistResponse(response = "Response")

        val result1 = repository.answerPrompt("Test 1", emptyList(), testState)
        val result2 = repository.answerPrompt("Test 2", emptyList(), testState)

        assert(result1.message.id != result2.message.id)
    }
}

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

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistChipOption
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistState
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import com.instructure.horizon.features.aiassistant.common.AiAssistResponse
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiAssistChatViewModelTest {
    private val repository: AiAssistRepository = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testContext = AiAssistContext(
        contextString = "Test context",
        contextSources = emptyList(),
        chatHistory = emptyList()
    )

    private val testState = JourneyAssistState(
        courseID = "123",
        fileID = null,
        pageID = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { aiAssistContextProvider.aiAssistContext } returns testContext

        val testMessage = AiAssistMessage(
            text = "Test response",
            role = JourneyAssistRole.Assistant
        )
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(testMessage, testState)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `ViewModel initializes with empty chat history`() = runTest {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `ViewModel initializes with existing chat history`() = runTest {
        val existingMessage = AiAssistMessage(
            text = "Existing message",
            role = JourneyAssistRole.User
        )
        val contextWithHistory = testContext.copy(chatHistory = listOf(existingMessage))
        every { aiAssistContextProvider.aiAssistContext } returns contextWithHistory

        val viewModel = getViewModel()

        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals("Existing message", viewModel.uiState.value.messages[0].text)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Text input change updates state`() = runTest {
        val viewModel = getViewModel()

        val newText = TextFieldValue("Test input")
        viewModel.uiState.value.onInputTextChanged(newText)

        assertEquals("Test input", viewModel.uiState.value.inputTextValue.text)
    }

    @Test
    fun `Text submission sends message and clears input`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any {
            it.role == JourneyAssistRole.User && it.text == "Test message"
        })
        assertEquals("", viewModel.uiState.value.inputTextValue.text)
    }

    @Test
    fun `Text submission receives response from repository`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.answerPrompt("Test message", any(), any()) }
        assertTrue(viewModel.uiState.value.messages.any {
            it.role == JourneyAssistRole.Assistant && it.text == "Test response"
        })
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Loading state is set during message submission`() = runTest {
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            val message = AiAssistMessage(
                text = "Response",
                role = JourneyAssistRole.Assistant
            )
            AiAssistResponse(message, testState)
        }

        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test message"))
        viewModel.uiState.value.onInputTextSubmitted()

        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Messages are appended in correct order`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("First message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Second message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.messages
        assertEquals(4, messages.size)
        assertEquals(JourneyAssistRole.User, messages[0].role)
        assertEquals("First message", messages[0].text)
        assertEquals(JourneyAssistRole.Assistant, messages[1].role)
        assertEquals(JourneyAssistRole.User, messages[2].role)
        assertEquals("Second message", messages[2].text)
        assertEquals(JourneyAssistRole.Assistant, messages[3].role)
    }

    @Test
    fun `Chip click sends prompt and receives response`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onChipClicked("Suggested prompt")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any {
            it.role == JourneyAssistRole.User && it.text == "Suggested prompt"
        })
        coVerify { repository.answerPrompt("Suggested prompt", any(), any()) }
        assertTrue(viewModel.uiState.value.messages.any {
            it.role == JourneyAssistRole.Assistant
        })
    }

    @Test
    fun `Clear chat history updates context provider`() = runTest {
        val existingMessage = AiAssistMessage(
            text = "Existing message",
            role = JourneyAssistRole.User
        )
        val contextWithHistory = testContext.copy(chatHistory = listOf(existingMessage))
        every { aiAssistContextProvider.aiAssistContext } returns contextWithHistory

        val viewModel = getViewModel()
        viewModel.uiState.value.onClearChatHistory()

        verify {
            aiAssistContextProvider.aiAssistContext = match {
                it.chatHistory.isEmpty()
            }
        }
    }

    @Test
    fun `Navigate to cards updates context and removes last message`() = runTest {
        val responseMessage = AiAssistMessage(
            text = "Response with cards",
            role = JourneyAssistRole.Assistant,
            chipOptions = listOf(JourneyAssistChipOption("Option 1", "prompt1"))
        )
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(responseMessage, testState)

        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Generate cards"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        val messageCountBefore = viewModel.uiState.value.messages.size
        viewModel.uiState.value.onNavigateToCards()

        assertEquals(messageCountBefore - 1, viewModel.uiState.value.messages.size)
        verify { aiAssistContextProvider.aiAssistContext = any() }
    }

    @Test
    fun `Repository is called with conversation history`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("First"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Second"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 2) {
            repository.answerPrompt(any(), any(), any())
        }

        coVerify {
            repository.answerPrompt(
                "Second",
                match { history ->
                    history.any { it.role == JourneyAssistRole.User && it.text == "First" } &&
                    history.any { it.role == JourneyAssistRole.Assistant } &&
                    history.any { it.role == JourneyAssistRole.User && it.text == "Second" }
                },
                any()
            )
        }
    }

    @Test
    fun `State is updated from repository response`() = runTest {
        val updatedState = JourneyAssistState(
            courseID = "456",
            fileID = "789",
            pageID = "101"
        )
        val message = AiAssistMessage(
            text = "Response",
            role = JourneyAssistRole.Assistant
        )
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(message, updatedState)

        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Second"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.answerPrompt(
                "Second",
                any(),
                match { state ->
                    state.courseID == "456" &&
                    state.fileID == "789" &&
                    state.pageID == "101"
                }
            )
        }
    }

    @Test
    fun `Error handling sets loading to false`() = runTest {
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } throws Exception("Network error")

        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun getViewModel(): AiAssistChatViewModel {
        return AiAssistChatViewModel(repository, aiAssistContextProvider)
    }
}

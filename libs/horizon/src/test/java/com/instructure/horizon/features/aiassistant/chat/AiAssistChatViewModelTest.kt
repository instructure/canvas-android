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

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessagePrompt
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessageRole
import com.instructure.pine.type.MessageInput
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
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
    private val context: Context = mockk(relaxed = true)
    private val repository: AiAssistChatRepository = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testContext = AiAssistContext(
        contextString = "Test context",
        contextSources = emptyList(),
        chatHistory = mutableListOf()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { aiAssistContextProvider.aiAssistContext } returns testContext
        coEvery { repository.answerPrompt(any<String>(), any()) } returns "Test response"
        coEvery { repository.answerPrompt(any<List<MessageInput>>(), any<Map<String, String>>()) } returns "Test response"
        coEvery { repository.summarizePrompt(any()) } returns "Summary response"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel initializes with empty chat history`() = runTest {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test ViewModel initializes with existing chat history`() = runTest {
        val existingMessage = AiAssistMessage(
            prompt = AiAssistMessagePrompt.Summarize,
            role = AiAssistMessageRole.User
        )
        val contextWithHistory = testContext.copy(chatHistory = mutableListOf(existingMessage))
        every { aiAssistContextProvider.aiAssistContext } returns contextWithHistory

        val viewModel = getViewModel()

        assertEquals(2, viewModel.uiState.value.messages.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test text input change updates state`() = runTest {
        val viewModel = getViewModel()

        val newText = TextFieldValue("Test input")
        viewModel.uiState.value.onInputTextChanged(newText)

        assertEquals("Test input", viewModel.uiState.value.inputTextValue.text)
    }

    @Test
    fun `Test text submission sends message`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any {
            it.role == AiAssistMessageRole.User &&
            it.prompt is AiAssistMessagePrompt.Custom &&
            (it.prompt as AiAssistMessagePrompt.Custom).message == "Test message"
        })
    }

    @Test
    fun `Test text submission receives response`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any {
            it.role == AiAssistMessageRole.Assistant
        })
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test text submission clears input field`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.inputTextValue.text)
    }

    @Test
    fun `Test existing summarize prompt is executed`() = runTest {
        val existingMessage = AiAssistMessage(
            prompt = AiAssistMessagePrompt.Summarize,
            role = AiAssistMessageRole.User
        )
        val contextWithHistory = testContext.copy(chatHistory = mutableListOf(existingMessage))
        every { aiAssistContextProvider.aiAssistContext } returns contextWithHistory

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.summarizePrompt(any()) }
        assertTrue(viewModel.uiState.value.messages.size >= 2)
    }

    @Test
    fun `Test custom prompt calls answer prompt`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Custom question"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.answerPrompt("Custom question", any()) }
    }

    @Test
    fun `Test loading state is set during message submission`() = runTest {
        coEvery { repository.answerPrompt(any<String>(), any()) } coAnswers {
            // Simulate a delay
            kotlinx.coroutines.delay(100)
            "Response"
        }

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Test message"))
        viewModel.uiState.value.onInputTextSubmitted()

        // The loading state should be set immediately
        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        // After completion, loading should be false
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test AI context is passed to UI state`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(testContext, viewModel.uiState.value.aiContext)
    }

    @Test
    fun `Test messages are appended in correct order`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("First message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onInputTextChanged(TextFieldValue("Second message"))
        viewModel.uiState.value.onInputTextSubmitted()
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.messages
        assertTrue(messages.size >= 4) // 2 user messages + 2 assistant responses

        // Check that messages alternate between user and assistant
        val userMessages = messages.filterIndexed { index, _ -> index % 2 == 0 }
        assertTrue(userMessages.all { it.role == AiAssistMessageRole.User })
    }

    private fun getViewModel(): AiAssistChatViewModel {
        return AiAssistChatViewModel(context, repository, aiAssistContextProvider)
    }
}

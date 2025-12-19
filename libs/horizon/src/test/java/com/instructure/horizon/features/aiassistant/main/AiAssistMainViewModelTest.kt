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
package com.instructure.horizon.features.aiassistant.main

import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import com.instructure.horizon.features.aiassistant.common.AiAssistResponse
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
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
class AiAssistMainViewModelTest {
    private val repository: AiAssistRepository = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testState = JourneyAssistState(
        courseID = "123",
        fileID = null,
        pageID = null
    )

    private val testContext = AiAssistContext(
        contextString = "Test context",
        contextSources = emptyList(),
        chatHistory = emptyList()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { aiAssistContextProvider.aiAssistContext } returns testContext

        val testMessage = JourneyAssistChatMessage(
            id = "init-1",
            text = "Welcome! How can I help?",
            prompt = "",
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
    fun `ViewModel initializes with loading state`() = runTest {
        val initialMessage = JourneyAssistChatMessage(
            id = "init-1",
            text = "Welcome!",
            prompt = "",
            role = JourneyAssistRole.Assistant
        )

        coEvery {
            repository.answerPrompt("", any(), any())
        } returns AiAssistResponse(initialMessage, testState)

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals("Welcome!", viewModel.uiState.value.messages[0].text)
    }

    @Test
    fun `ViewModel calls repository on initialization`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { repository.answerPrompt(any(), any(), any()) }
    }

    @Test
    fun `sendMessage adds user message to UI`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("Hello")

        assertTrue(viewModel.uiState.value.messages.any {
            it.role == JourneyAssistRole.User && it.text == "Hello"
        })
    }

    @Test
    fun `sendMessage calls repository and adds response`() = runTest {
        val responseMessage = JourneyAssistChatMessage(
            id = "response-1",
            text = "Hi there!",
            prompt = "Hi there!",
            role = JourneyAssistRole.Assistant
        )

        coEvery {
            repository.answerPrompt("Hello", any(), any())
        } returns AiAssistResponse(responseMessage, testState)

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("Hello")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.answerPrompt("Hello", any(), any()) }
        assertTrue(viewModel.uiState.value.messages.any {
            it.role == JourneyAssistRole.Assistant && it.text == "Hi there!"
        })
    }

    @Test
    fun `sendMessage shows loading state during API call`() = runTest {
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            val message = JourneyAssistChatMessage(
                id = "response-1",
                text = "Response",
                prompt = "Response",
                role = JourneyAssistRole.Assistant
            )
            AiAssistResponse(message, testState)
        }

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.uiState.value.sendMessage("Test")

        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `messages are appended in correct order`() = runTest {
        val response1 = JourneyAssistChatMessage(
            id = "response-1",
            text = "Response 1",
            prompt = "Response 1",
            role = JourneyAssistRole.Assistant
        )

        val response2 = JourneyAssistChatMessage(
            id = "response-2",
            text = "Response 2",
            prompt = "Response 2",
            role = JourneyAssistRole.Assistant
        )

        coEvery {
            repository.answerPrompt("", any(), any())
        } returns AiAssistResponse(response1, testState)

        coEvery {
            repository.answerPrompt("First", any(), any())
        } returns AiAssistResponse(response2, testState)

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("First")
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.messages
        assertEquals(3, messages.size)
        assertEquals(JourneyAssistRole.Assistant, messages[0].role)
        assertEquals("Response 1", messages[0].text)
        assertEquals(JourneyAssistRole.User, messages[1].role)
        assertEquals("First", messages[1].text)
        assertEquals(JourneyAssistRole.Assistant, messages[2].role)
        assertEquals("Response 2", messages[2].text)
    }

    @Test
    fun `sendMessage handles error gracefully`() = runTest {
        coEvery {
            repository.answerPrompt("Error", any(), any())
        } throws Exception("Network error")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val messageCountBefore = viewModel.uiState.value.messages.size

        viewModel.uiState.value.sendMessage("Error")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(messageCountBefore + 1, viewModel.uiState.value.messages.size)
    }

    @Test
    fun `onNavigateToDetails updates context provider`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("Test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.onNavigateToDetails()

        verify {
            aiAssistContextProvider.aiAssistContext = match {
                it.chatHistory.size == 3
            }
        }
    }

    @Test
    fun `onNavigateToDetails keeps only first message in UI`() = runTest {
        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("Test 1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("Test 2")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.size > 1)

        viewModel.uiState.value.onNavigateToDetails()

        assertEquals(1, viewModel.uiState.value.messages.size)
    }

    @Test
    fun `state is updated from repository response`() = runTest {
        val updatedState = JourneyAssistState(
            courseID = "456",
            fileID = "789",
            pageID = "101"
        )

        val message1 = JourneyAssistChatMessage(
            id = "msg-1",
            text = "First",
            prompt = "First",
            role = JourneyAssistRole.Assistant
        )

        val message2 = JourneyAssistChatMessage(
            id = "msg-2",
            text = "Second",
            prompt = "Second",
            role = JourneyAssistRole.Assistant
        )

        coEvery {
            repository.answerPrompt("", any(), any())
        } returns AiAssistResponse(message1, testState)

        coEvery {
            repository.answerPrompt("Test", any(), any())
        } returns AiAssistResponse(message2, updatedState)

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("Test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.sendMessage("Another")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.answerPrompt(
                "Another",
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
    fun `initialization error sets loading to false`() = runTest {
        coEvery {
            repository.answerPrompt("", any(), any())
        } throws Exception("Init error")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }

    private fun getViewModel(): AiAssistMainViewModel {
        return AiAssistMainViewModel(repository, aiAssistContextProvider)
    }
}

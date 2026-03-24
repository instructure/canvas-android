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

import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import com.instructure.horizon.features.aiassistant.common.AiAssistResponse
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import io.mockk.coEvery
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

    private fun getViewModel(): AiAssistChatViewModel {
        return AiAssistChatViewModel(repository, aiAssistContextProvider)
    }
}

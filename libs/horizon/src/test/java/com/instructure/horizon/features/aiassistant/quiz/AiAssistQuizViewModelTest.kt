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
package com.instructure.horizon.features.aiassistant.quiz

import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistQuizItem
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import com.instructure.horizon.features.aiassistant.common.AiAssistResponse
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizAnswerStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
class AiAssistQuizViewModelTest {
    private val repository: AiAssistRepository = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testQuizItems = listOf(
        JourneyAssistQuizItem(
            question = "What is 2+2?",
            answers = listOf("3", "4", "5", "6"),
            correctAnswerIndex = 1
        ),
        JourneyAssistQuizItem(
            question = "What is the capital of France?",
            answers = listOf("London", "Paris", "Berlin"),
            correctAnswerIndex = 1
        )
    )

    private val testMessage = JourneyAssistChatMessage(
        id = "test-1",
        text = "Here are some quiz questions",
        prompt = "Generate quiz",
        role = JourneyAssistRole.Assistant,
        quizItems = testQuizItems
    )

    private val testContext = AiAssistContext(
        contextString = "Test context",
        contextSources = emptyList(),
        chatHistory = listOf(testMessage)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { aiAssistContextProvider.aiAssistContext } returns testContext
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `ViewModel initializes with quiz items from context`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.quizList.size)
        assertEquals(0, viewModel.uiState.value.currentQuizIndex)
        assertEquals("What is 2+2?", viewModel.uiState.value.quizList[0].question)
        assertEquals(4, viewModel.uiState.value.quizList[0].options.size)
        assertEquals(1, viewModel.uiState.value.quizList[0].answerIndex)
    }

    @Test
    fun `ViewModel initializes with empty quiz list when no context`() = runTest {
        every { aiAssistContextProvider.aiAssistContext } returns AiAssistContext()

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.quizList.isEmpty())
    }

    @Test
    fun `setSelectedIndex updates selected option`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.setSelectedIndex(2)

        val currentQuiz = viewModel.uiState.value.quizList[0]
        assertEquals(2, currentQuiz.selectedOptionIndex)
        assertEquals(AiAssistQuizAnswerStatus.SELECTED, currentQuiz.options[2].status)
        assertEquals(AiAssistQuizAnswerStatus.UNSELECTED, currentQuiz.options[0].status)
        assertEquals(AiAssistQuizAnswerStatus.UNSELECTED, currentQuiz.options[1].status)
    }

    @Test
    fun `setSelectedIndex changes selection when called multiple times`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.setSelectedIndex(0)
        viewModel.uiState.value.setSelectedIndex(3)

        val currentQuiz = viewModel.uiState.value.quizList[0]
        assertEquals(3, currentQuiz.selectedOptionIndex)
        assertEquals(AiAssistQuizAnswerStatus.SELECTED, currentQuiz.options[3].status)
        assertEquals(AiAssistQuizAnswerStatus.UNSELECTED, currentQuiz.options[0].status)
    }

    @Test
    fun `checkQuiz marks correct and incorrect answers`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.setSelectedIndex(2)
        viewModel.uiState.value.checkQuiz()

        val currentQuiz = viewModel.uiState.value.quizList[0]
        assertTrue(currentQuiz.isChecked)
        assertEquals(AiAssistQuizAnswerStatus.CORRECT, currentQuiz.options[1].status)
        assertEquals(AiAssistQuizAnswerStatus.INCORRECT, currentQuiz.options[2].status)
    }

    @Test
    fun `checkQuiz with correct answer selected`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.setSelectedIndex(1)
        viewModel.uiState.value.checkQuiz()

        val currentQuiz = viewModel.uiState.value.quizList[0]
        assertTrue(currentQuiz.isChecked)
        assertEquals(AiAssistQuizAnswerStatus.CORRECT, currentQuiz.options[1].status)
    }

    @Test
    fun `regenerateQuiz moves to next quiz when available`() = runTest {
        val viewModel = getViewModel()

        assertEquals(0, viewModel.uiState.value.currentQuizIndex)

        viewModel.uiState.value.regenerateQuiz()

        assertEquals(1, viewModel.uiState.value.currentQuizIndex)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `regenerateQuiz calls repository when at last quiz`() = runTest {
        val newQuizItems = listOf(
            JourneyAssistQuizItem(
                question = "New question?",
                answers = listOf("A", "B", "C"),
                correctAnswerIndex = 0
            )
        )

        val responseMessage = JourneyAssistChatMessage(
            id = "response-1",
            text = "More quizzes",
            prompt = "More quizzes",
            role = JourneyAssistRole.Assistant,
            quizItems = newQuizItems
        )

        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(responseMessage, JourneyAssistState())

        val viewModel = getViewModel()

        viewModel.uiState.value.regenerateQuiz()
        viewModel.uiState.value.regenerateQuiz()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.answerPrompt(any(), any(), any()) }
        assertEquals(3, viewModel.uiState.value.quizList.size)
        assertEquals(2, viewModel.uiState.value.currentQuizIndex)
        assertEquals("New question?", viewModel.uiState.value.quizList[2].question)
    }

    @Test
    fun `regenerateQuiz shows loading state during API call`() = runTest {
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            val message = JourneyAssistChatMessage(
                id = "response-1",
                text = "Quiz",
                prompt = "Quiz",
                role = JourneyAssistRole.Assistant,
                quizItems = listOf(
                    JourneyAssistQuizItem("Q?", listOf("A", "B"), 0)
                )
            )
            AiAssistResponse(message, JourneyAssistState())
        }

        val viewModel = getViewModel()

        viewModel.uiState.value.regenerateQuiz()
        viewModel.uiState.value.regenerateQuiz()

        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `regenerateQuiz handles error gracefully`() = runTest {
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } throws Exception("Network error")

        val viewModel = getViewModel()

        viewModel.uiState.value.regenerateQuiz()
        viewModel.uiState.value.regenerateQuiz()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.quizList.size)
    }

    @Test
    fun `regenerateQuiz updates context with new message`() = runTest {
        val responseMessage = JourneyAssistChatMessage(
            id = "response-1",
            text = "More quizzes",
            prompt = "More quizzes",
            role = JourneyAssistRole.Assistant,
            quizItems = listOf(JourneyAssistQuizItem("Q?", listOf("A", "B"), 0))
        )

        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(responseMessage, JourneyAssistState())

        val viewModel = getViewModel()

        viewModel.uiState.value.regenerateQuiz()
        viewModel.uiState.value.regenerateQuiz()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { aiAssistContextProvider.addMessageToChatHistory(responseMessage) }
        verify { aiAssistContextProvider.updateContextFromState(any()) }
    }

    @Test
    fun `onClearChatHistory updates context provider`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onClearChatHistory()

        verify {
            aiAssistContextProvider.aiAssistContext = match {
                it.chatHistory.isEmpty()
            }
        }
    }

    @Test
    fun `quiz options are initialized with UNSELECTED status`() = runTest {
        val viewModel = getViewModel()

        val quiz = viewModel.uiState.value.quizList[0]
        quiz.options.forEach { option ->
            assertEquals(AiAssistQuizAnswerStatus.UNSELECTED, option.status)
        }
    }

    @Test
    fun `quiz is not checked initially`() = runTest {
        val viewModel = getViewModel()

        val quiz = viewModel.uiState.value.quizList[0]
        assertFalse(quiz.isChecked)
        assertNull(quiz.selectedOptionIndex)
    }

    @Test
    fun `multiple quizzes can be navigated independently`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.setSelectedIndex(1)
        assertEquals(1, viewModel.uiState.value.quizList[0].selectedOptionIndex)

        viewModel.uiState.value.regenerateQuiz()

        viewModel.uiState.value.setSelectedIndex(2)
        assertEquals(1, viewModel.uiState.value.quizList[0].selectedOptionIndex)
        assertEquals(2, viewModel.uiState.value.quizList[1].selectedOptionIndex)
    }

    private fun getViewModel(): AiAssistQuizViewModel {
        return AiAssistQuizViewModel(repository, aiAssistContextProvider)
    }
}

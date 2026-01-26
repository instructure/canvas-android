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

import com.instructure.canvasapi2.models.journey.JourneyAssistFlashCard
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.models.journey.JourneyAssistState
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
class AiAssistFlashcardViewModelTest {
    private val repository: AiAssistRepository = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testFlashCards = listOf(
        JourneyAssistFlashCard(
            question = "What is Kotlin?",
            answer = "A modern programming language"
        ),
        JourneyAssistFlashCard(
            question = "What is Android?",
            answer = "A mobile operating system"
        ),
        JourneyAssistFlashCard(
            question = "What is Jetpack Compose?",
            answer = "A modern UI toolkit for Android"
        )
    )

    private val testMessage = AiAssistMessage(
        text = "Here are some flashcards",
        role = JourneyAssistRole.Assistant,
        flashCards = testFlashCards
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
    fun `ViewModel initializes with flashcards from context`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(3, viewModel.uiState.value.flashcardList.size)
        assertEquals(0, viewModel.uiState.value.currentCardIndex)
        assertEquals("What is Kotlin?", viewModel.uiState.value.flashcardList[0].question)
        assertEquals("A modern programming language", viewModel.uiState.value.flashcardList[0].answer)
    }

    @Test
    fun `ViewModel initializes with empty list when no context`() = runTest {
        every { aiAssistContextProvider.aiAssistContext } returns AiAssistContext()

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.flashcardList.isEmpty())
    }

    @Test
    fun `flashcards are not flipped initially`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.flashcardList.forEach { card ->
            assertFalse(card.isFlippedToAnswer)
        }
    }

    @Test
    fun `onFlashcardClicked flips card to answer`() = runTest {
        val viewModel = getViewModel()

        val firstCard = viewModel.uiState.value.flashcardList[0]
        assertFalse(firstCard.isFlippedToAnswer)

        viewModel.uiState.value.onFlashcardClicked(firstCard)

        val updatedCard = viewModel.uiState.value.flashcardList[0]
        assertTrue(updatedCard.isFlippedToAnswer)
    }

    @Test
    fun `onFlashcardClicked toggles card state`() = runTest {
        val viewModel = getViewModel()

        val firstCard = viewModel.uiState.value.flashcardList[0]

        viewModel.uiState.value.onFlashcardClicked(firstCard)
        assertTrue(viewModel.uiState.value.flashcardList[0].isFlippedToAnswer)

        val flippedCard = viewModel.uiState.value.flashcardList[0]
        viewModel.uiState.value.onFlashcardClicked(flippedCard)
        assertFalse(viewModel.uiState.value.flashcardList[0].isFlippedToAnswer)
    }

    @Test
    fun `onFlashcardClicked only affects the clicked card`() = runTest {
        val viewModel = getViewModel()

        val secondCard = viewModel.uiState.value.flashcardList[1]
        viewModel.uiState.value.onFlashcardClicked(secondCard)

        assertFalse(viewModel.uiState.value.flashcardList[0].isFlippedToAnswer)
        assertTrue(viewModel.uiState.value.flashcardList[1].isFlippedToAnswer)
        assertFalse(viewModel.uiState.value.flashcardList[2].isFlippedToAnswer)
    }

    @Test
    fun `updateCurrentCardIndex changes current card`() = runTest {
        val viewModel = getViewModel()

        assertEquals(0, viewModel.uiState.value.currentCardIndex)

        viewModel.uiState.value.updateCurrentCardIndex(2)

        assertEquals(2, viewModel.uiState.value.currentCardIndex)
    }

    @Test
    fun `regenerateFlashcards calls repository`() = runTest {
        val newFlashCards = listOf(
            JourneyAssistFlashCard("New Q1", "New A1"),
            JourneyAssistFlashCard("New Q2", "New A2")
        )

        val responseMessage = AiAssistMessage(
            text = "More flashcards",
            role = JourneyAssistRole.Assistant,
            flashCards = newFlashCards
        )

        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(responseMessage, JourneyAssistState())

        val viewModel = getViewModel()

        viewModel.uiState.value.regenerateFlashcards()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.answerPrompt(any(), any(), any()) }
    }

    @Test
    fun `regenerateFlashcards updates flashcard list`() = runTest {
        val newFlashCards = listOf(
            JourneyAssistFlashCard("New Q1", "New A1"),
            JourneyAssistFlashCard("New Q2", "New A2")
        )

        val responseMessage = AiAssistMessage(
            text = "More flashcards",
            role = JourneyAssistRole.Assistant,
            flashCards = newFlashCards
        )

        val updatedContext = testContext.copy(
            chatHistory = testContext.chatHistory + responseMessage
        )

        every { aiAssistContextProvider.aiAssistContext } returns testContext andThen updatedContext

        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(responseMessage, JourneyAssistState())

        val viewModel = getViewModel()

        assertEquals(3, viewModel.uiState.value.flashcardList.size)

        viewModel.uiState.value.regenerateFlashcards()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.flashcardList.size)
        assertEquals("New Q1", viewModel.uiState.value.flashcardList[0].question)
        assertEquals("New A1", viewModel.uiState.value.flashcardList[0].answer)
    }

    @Test
    fun `regenerateFlashcards shows loading state`() = runTest {
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            val message = AiAssistMessage(
                text = "Flashcards",
                role = JourneyAssistRole.Assistant,
                flashCards = listOf(JourneyAssistFlashCard("Q", "A"))
            )
            AiAssistResponse(message, JourneyAssistState())
        }

        val viewModel = getViewModel()

        viewModel.uiState.value.regenerateFlashcards()

        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `regenerateFlashcards handles error gracefully`() = runTest {
        coEvery {
            repository.answerPrompt(any(), any(), any())
        } throws Exception("Network error")

        val viewModel = getViewModel()

        val originalFlashcards = viewModel.uiState.value.flashcardList

        viewModel.uiState.value.regenerateFlashcards()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(originalFlashcards.size, viewModel.uiState.value.flashcardList.size)
    }

    @Test
    fun `regenerateFlashcards updates context with new message`() = runTest {
        val responseMessage = AiAssistMessage(
            text = "More flashcards",
            role = JourneyAssistRole.Assistant,
            flashCards = listOf(JourneyAssistFlashCard("Q", "A"))
        )

        coEvery {
            repository.answerPrompt(any(), any(), any())
        } returns AiAssistResponse(responseMessage, JourneyAssistState())

        val viewModel = getViewModel()

        viewModel.uiState.value.regenerateFlashcards()
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
    fun `multiple flashcards can be flipped independently`() = runTest {
        val viewModel = getViewModel()

        val card0 = viewModel.uiState.value.flashcardList[0]
        val card1 = viewModel.uiState.value.flashcardList[1]
        val card2 = viewModel.uiState.value.flashcardList[2]

        viewModel.uiState.value.onFlashcardClicked(card0)
        viewModel.uiState.value.onFlashcardClicked(card2)

        assertTrue(viewModel.uiState.value.flashcardList[0].isFlippedToAnswer)
        assertFalse(viewModel.uiState.value.flashcardList[1].isFlippedToAnswer)
        assertTrue(viewModel.uiState.value.flashcardList[2].isFlippedToAnswer)
    }

    private fun getViewModel(): AiAssistFlashcardViewModel {
        return AiAssistFlashcardViewModel(repository, aiAssistContextProvider)
    }
}

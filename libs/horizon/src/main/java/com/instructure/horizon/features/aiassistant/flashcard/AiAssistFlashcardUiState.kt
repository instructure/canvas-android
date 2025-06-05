package com.instructure.horizon.features.aiassistant.flashcard

import kotlinx.serialization.Serializable

data class AiAssistFlashcardUiState(
    val isLoading: Boolean = false,
    val currentCardIndex: Int = 0,
    val flashcardList: List<FlashcardState> = emptyList(),
    val onFlashcardClicked: (FlashcardState) -> Unit = {},
    val updateCurrentCardIndex: (Int) -> Unit = {},
)

data class FlashcardState(
    val question: String,
    val answer: String,
    val isFlippedToAnswer: Boolean = false,
)

@Serializable
data class GeneratedFlashcard(
    val question: String,
    val answer: String,
)
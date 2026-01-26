package com.instructure.horizon.features.aiassistant.flashcard

data class AiAssistFlashcardUiState(
    val isLoading: Boolean = false,
    val currentCardIndex: Int = 0,
    val flashcardList: List<FlashcardState> = emptyList(),
    val onFlashcardClicked: (FlashcardState) -> Unit = {},
    val updateCurrentCardIndex: (Int) -> Unit = {},
    val onClearChatHistory: () -> Unit = {},
    val regenerateFlashcards: () -> Unit = {},
)

data class FlashcardState(
    val question: String,
    val answer: String,
    val isFlippedToAnswer: Boolean = false,
)
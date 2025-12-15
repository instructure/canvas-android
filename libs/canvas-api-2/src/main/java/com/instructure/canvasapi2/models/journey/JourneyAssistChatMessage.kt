package com.instructure.canvasapi2.models.journey

data class JourneyAssistChatMessage(
    val id: String,
    val prompt: String = "",
    val displayText: String = "",
    val role: JourneyAssistRole,
    val chipOptions: List<JourneyAssistChipOption> = emptyList(),
    val flashcards: List<JourneyAssistFlashCard> = emptyList(),
    val quizItems: List<JourneyAssistQuizItem> = emptyList(),
    val citations: List<JourneyAssistCitation> = emptyList(),
)


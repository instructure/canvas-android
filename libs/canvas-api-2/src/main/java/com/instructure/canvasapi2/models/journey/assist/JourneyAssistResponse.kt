package com.instructure.canvasapi2.models.journey.assist

data class JourneyAssistResponse(
    val state: JourneyAssistState? = null,
    val statusCode : Int? = null,
    val response: String? = null,
    val chips: List<JourneyAssistChipOption> = emptyList(),
    val flashCards: List<JourneyAssistFlashCard> = emptyList(),
    val quizItems: List<JourneyAssistQuizItem> = emptyList(),
    val citations: List<JourneyAssistCitation> = emptyList(),
    val error: String? = null,
)

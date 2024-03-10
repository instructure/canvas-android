package com.instructure.student.features.ai.model

data class PageSummary(
    val summary: String,
    val questions: List<SummaryQuestions>
)
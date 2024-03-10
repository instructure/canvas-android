package com.instructure.student.features.ai.model

data class SummaryQuestions(
    val question: String,
    val choices: List<String>,
    val answer: String
)
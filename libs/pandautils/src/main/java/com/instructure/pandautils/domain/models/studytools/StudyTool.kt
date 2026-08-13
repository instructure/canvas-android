/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.pandautils.domain.models.studytools

/**
 * The Study Tools the server can offer, identified by the stable `kind` value it returns.
 * Unknown kinds are dropped when mapping, so a tool added server-side cannot break older clients.
 */
enum class StudyTool(val kind: String) {
    SUMMARIZE("summarize"),
    QUIZ("quiz"),
    FLASHCARDS("flashcards");

    companion object {
        fun fromKind(kind: String?): StudyTool? = entries.firstOrNull { it.kind == kind }
    }
}

/**
 * An available tool plus the prompt string that must be echoed back verbatim to run it.
 */
data class StudyToolOption(
    val tool: StudyTool,
    val prompt: String
)

/**
 * The generated content for a tool run.
 */
sealed interface StudyToolResult {
    data class Summary(val text: String) : StudyToolResult
    data class Quiz(val questions: List<QuizQuestion>) : StudyToolResult
    data class Flashcards(val cards: List<Flashcard>) : StudyToolResult
}

/**
 * [correctAnswerIndex] indexes into [answers], which the server shuffles on every request.
 */
data class QuizQuestion(
    val question: String,
    val answers: List<String>,
    val correctAnswerIndex: Int
)

data class Flashcard(
    val question: String,
    val answer: String
)

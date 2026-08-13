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
package com.instructure.canvasapi2.models.studyassist

import com.google.gson.annotations.SerializedName

/**
 * Response for `POST /courses/:course_id/study_assist`. The server populates exactly one of
 * [chips], [response], [quizItems] or [flashCards] depending on the prompt.
 */
data class StudyAssistResponse(
    val chips: List<StudyAssistChip>? = null,
    val response: String? = null,
    val quizItems: List<StudyAssistQuizItem>? = null,
    val flashCards: List<StudyAssistFlashCard>? = null
)

/**
 * An available tool. [chip] is a server-localized display label that we deliberately do not render;
 * [prompt] must be echoed back verbatim to run the tool, and [kind] is the stable identifier.
 */
data class StudyAssistChip(
    val chip: String? = null,
    val prompt: String? = null,
    val kind: String? = null
)

/**
 * A single multiple-choice question. The server shuffles [answers] per request, so
 * [correctAnswerIndex] must always be read from the response.
 */
data class StudyAssistQuizItem(
    val question: String? = null,
    val answers: List<String>? = null,
    val correctAnswerIndex: Int? = null
)

data class StudyAssistFlashCard(
    val question: String? = null,
    val answer: String? = null
)

/**
 * Error payload returned alongside a non-2xx status. [errorHeader] is only sent for the
 * "page content cannot be read" case and is meant to be shown as a title above [error].
 */
data class StudyAssistErrorResponse(
    val error: String? = null,
    @SerializedName("error_header") val errorHeader: String? = null
)

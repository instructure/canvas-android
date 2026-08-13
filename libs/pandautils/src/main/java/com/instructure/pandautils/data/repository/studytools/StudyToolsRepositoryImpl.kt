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
package com.instructure.pandautils.data.repository.studytools

import com.instructure.canvasapi2.apis.StudyAssistAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.studyassist.StudyAssistRequest
import com.instructure.canvasapi2.models.studyassist.StudyAssistResponse
import com.instructure.canvasapi2.models.studyassist.StudyAssistState
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.domain.models.studytools.Flashcard
import com.instructure.pandautils.domain.models.studytools.QuizQuestion
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import kotlinx.coroutines.delay

class StudyToolsRepositoryImpl(
    private val studyAssistApi: StudyAssistAPI.StudyAssistInterface
) : StudyToolsRepository {

    override suspend fun getAvailableTools(
        courseId: Long,
        pageUrl: String
    ): DataResult<List<StudyToolOption>> {
        val request = StudyAssistRequest(state = state(courseId, pageUrl))
        return post(courseId, request).map { response ->
            response.chips.orEmpty().mapNotNull { chip ->
                val tool = StudyTool.fromKind(chip.kind)
                val prompt = chip.prompt
                if (tool == null || prompt.isNullOrBlank()) null else StudyToolOption(tool, prompt)
            }
        }
    }

    override suspend fun runTool(
        courseId: Long,
        pageUrl: String,
        tool: StudyTool,
        prompt: String,
        regenerate: Boolean
    ): DataResult<StudyToolResult> {
        val request = StudyAssistRequest(
            prompt = prompt,
            state = state(courseId, pageUrl),
            regenerate = regenerate
        )
        return post(courseId, request).then { response ->
            response.toResult(tool)?.let { DataResult.Success(it) }
                ?: DataResult.Fail(Failure.ParsingError)
        }
    }

    /**
     * Mirrors the web client: only a 502 is retried, at most [MAX_RETRIES] times, with a linear
     * backoff. Every other failure surfaces immediately.
     */
    private suspend fun post(
        courseId: Long,
        request: StudyAssistRequest
    ): DataResult<StudyAssistResponse> {
        var lastResult: DataResult<StudyAssistResponse> = DataResult.Fail()

        for (attempt in 0..MAX_RETRIES) {
            if (attempt > 0) delay(RETRY_BACKOFF_MS * attempt)

            val result = studyAssistApi.postStudyAssist(courseId, request, RestParams())
            if (result is DataResult.Success || !result.isRetryable()) return result
            lastResult = result
        }

        return lastResult
    }

    private fun DataResult<StudyAssistResponse>.isRetryable() =
        (this as? DataResult.Fail)?.response?.code == HTTP_BAD_GATEWAY

    private fun state(courseId: Long, pageUrl: String) =
        StudyAssistState(courseId = courseId.toString(), pageId = pageUrl)

    private fun StudyAssistResponse.toResult(tool: StudyTool): StudyToolResult? = when (tool) {
        StudyTool.SUMMARIZE -> response
            ?.takeIf { it.isNotBlank() }
            ?.let { StudyToolResult.Summary(it) }

        StudyTool.QUIZ -> quizItems
            ?.mapNotNull { item ->
                val question = item.question
                val answers = item.answers
                val index = item.correctAnswerIndex
                if (question.isNullOrBlank() || answers.isNullOrEmpty() || index == null) {
                    null
                } else if (index !in answers.indices) {
                    null
                } else {
                    QuizQuestion(question, answers, index)
                }
            }
            ?.takeIf { it.isNotEmpty() }
            ?.let { StudyToolResult.Quiz(it) }

        StudyTool.FLASHCARDS -> flashCards
            ?.mapNotNull { card ->
                val question = card.question
                val answer = card.answer
                if (question.isNullOrBlank() || answer.isNullOrBlank()) {
                    null
                } else {
                    Flashcard(question, answer)
                }
            }
            ?.takeIf { it.isNotEmpty() }
            ?.let { StudyToolResult.Flashcards(it) }
    }

    companion object {
        private const val MAX_RETRIES = 2
        private const val RETRY_BACKOFF_MS = 1000L
        private const val HTTP_BAD_GATEWAY = 502
    }
}

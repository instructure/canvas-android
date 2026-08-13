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
import com.instructure.canvasapi2.models.studyassist.StudyAssistChip
import com.instructure.canvasapi2.models.studyassist.StudyAssistFlashCard
import com.instructure.canvasapi2.models.studyassist.StudyAssistQuizItem
import com.instructure.canvasapi2.models.studyassist.StudyAssistRequest
import com.instructure.canvasapi2.models.studyassist.StudyAssistResponse
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyToolsRepositoryTest {

    private val studyAssistApi: StudyAssistAPI.StudyAssistInterface = mockk(relaxed = true)
    private val repository: StudyToolsRepository = StudyToolsRepositoryImpl(studyAssistApi)

    @After
    fun teardown() = unmockkAll()

    @Test
    fun `getAvailableTools maps chips to options`() = runTest {
        mockResponse(
            StudyAssistResponse(
                chips = listOf(
                    StudyAssistChip(chip = "Summarize", prompt = "Summarize", kind = "summarize"),
                    StudyAssistChip(chip = "Quiz me", prompt = "Quiz me", kind = "quiz"),
                    StudyAssistChip(chip = "Flashcards", prompt = "Flashcards", kind = "flashcards")
                )
            )
        )

        val result = repository.getAvailableTools(COURSE_ID, PAGE_URL).successData()

        assertEquals(
            listOf(StudyTool.SUMMARIZE, StudyTool.QUIZ, StudyTool.FLASHCARDS),
            result.map { it.tool }
        )
        assertEquals(listOf("Summarize", "Quiz me", "Flashcards"), result.map { it.prompt })
    }

    @Test
    fun `getAvailableTools drops unknown kinds so new server tools cannot break the client`() = runTest {
        mockResponse(
            StudyAssistResponse(
                chips = listOf(
                    StudyAssistChip(chip = "Summarize", prompt = "Summarize", kind = "summarize"),
                    StudyAssistChip(chip = "Mind map", prompt = "Mind map", kind = "mindmap")
                )
            )
        )

        val result = repository.getAvailableTools(COURSE_ID, PAGE_URL).successData()

        assertEquals(listOf(StudyTool.SUMMARIZE), result.map { it.tool })
    }

    @Test
    fun `getAvailableTools drops chips with a blank prompt`() = runTest {
        mockResponse(
            StudyAssistResponse(
                chips = listOf(StudyAssistChip(chip = "Summarize", prompt = " ", kind = "summarize"))
            )
        )

        assertTrue(repository.getAvailableTools(COURSE_ID, PAGE_URL).successData().isEmpty())
    }

    @Test
    fun `getAvailableTools omits the prompt so the server returns chips`() = runTest {
        mockResponse(StudyAssistResponse(chips = emptyList()))
        val request = slot<StudyAssistRequest>()

        repository.getAvailableTools(COURSE_ID, PAGE_URL)

        coVerify { studyAssistApi.postStudyAssist(COURSE_ID, capture(request), any()) }
        assertEquals(null, request.captured.prompt)
        assertEquals(COURSE_ID.toString(), request.captured.state.courseId)
        assertEquals(PAGE_URL, request.captured.state.pageId)
        assertEquals("canvas", request.captured.callerApp)
    }

    @Test
    fun `runTool maps a summary`() = runTest {
        mockResponse(StudyAssistResponse(response = "A summary."))

        val result = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.SUMMARIZE, "Summarize")

        assertEquals(StudyToolResult.Summary("A summary."), result.successData())
    }

    @Test
    fun `runTool maps quiz items and keeps the server's correct answer index`() = runTest {
        mockResponse(
            StudyAssistResponse(
                quizItems = listOf(
                    StudyAssistQuizItem(
                        question = "Who makes Canvas?",
                        answers = listOf("Parchment", "Blackboard", "Instructure", "UEN"),
                        correctAnswerIndex = 2
                    )
                )
            )
        )

        val quiz = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.QUIZ, "Quiz me")
            .successData() as StudyToolResult.Quiz

        assertEquals(1, quiz.questions.size)
        assertEquals(2, quiz.questions.first().correctAnswerIndex)
        assertEquals("Instructure", quiz.questions.first().answers[2])
    }

    @Test
    fun `runTool drops quiz items whose correct index is out of bounds`() = runTest {
        mockResponse(
            StudyAssistResponse(
                quizItems = listOf(
                    StudyAssistQuizItem(question = "Q", answers = listOf("A", "B"), correctAnswerIndex = 5)
                )
            )
        )

        val result = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.QUIZ, "Quiz me")

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `runTool maps flashcards`() = runTest {
        mockResponse(
            StudyAssistResponse(
                flashCards = listOf(StudyAssistFlashCard(question = "Q", answer = "A"))
            )
        )

        val cards = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.FLASHCARDS, "Flashcards")
            .successData() as StudyToolResult.Flashcards

        assertEquals(1, cards.cards.size)
        assertEquals("Q", cards.cards.first().question)
        assertEquals("A", cards.cards.first().answer)
    }

    @Test
    fun `runTool fails when the response has no content for the requested tool`() = runTest {
        mockResponse(StudyAssistResponse(response = "   "))

        val result = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.SUMMARIZE, "Summarize")

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `runTool sends regenerate true so the server bypasses its 24h cache`() = runTest {
        mockResponse(StudyAssistResponse(response = "Fresh."))
        val request = slot<StudyAssistRequest>()

        repository.runTool(COURSE_ID, PAGE_URL, StudyTool.SUMMARIZE, "Summarize", regenerate = true)

        coVerify { studyAssistApi.postStudyAssist(COURSE_ID, capture(request), any()) }
        assertTrue(request.captured.regenerate)
        assertEquals("Summarize", request.captured.prompt)
    }

    @Test
    fun `runTool defaults regenerate to false`() = runTest {
        mockResponse(StudyAssistResponse(response = "Cached."))
        val request = slot<StudyAssistRequest>()

        repository.runTool(COURSE_ID, PAGE_URL, StudyTool.SUMMARIZE, "Summarize")

        coVerify { studyAssistApi.postStudyAssist(COURSE_ID, capture(request), any()) }
        assertTrue(!request.captured.regenerate)
    }

    @Test
    fun `a 404 reports the feature as unavailable and is not retried`() = runTest {
        mockFailure(404, """{"error":"Study tools aren't enabled for this course."}""")

        val result = repository.getAvailableTools(COURSE_ID, PAGE_URL)

        assertTrue(result is DataResult.Fail)
        assertEquals(404, (result as DataResult.Fail).response?.code)
        coVerify(exactly = 1) { studyAssistApi.postStudyAssist(any(), any(), any()) }
    }

    @Test
    fun `a 429 reports rate limiting and is not retried`() = runTest {
        mockFailure(429, """{"error":"You've hit the study tools rate limit. Try again later."}""")

        val result = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.QUIZ, "Quiz me")

        assertTrue(result is DataResult.Fail)
        assertEquals(429, (result as DataResult.Fail).response?.code)
        coVerify(exactly = 1) { studyAssistApi.postStudyAssist(any(), any(), any()) }
    }

    @Test
    fun `a 422 surfaces both the error header and the message`() = runTest {
        mockFailure(
            422,
            """{"error_header":"This page content cannot be read","error":"The study tool cannot read embedded videos."}"""
        )

        val result = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.SUMMARIZE, "Summarize")

        assertTrue(result is DataResult.Fail)
        assertEquals(422, (result as DataResult.Fail).response?.code)
    }

    @Test
    fun `a 502 is retried twice before failing`() = runTest {
        mockFailure(502, """{"error":"Study tools are temporarily unavailable."}""")

        val result = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.QUIZ, "Quiz me")

        assertTrue(result is DataResult.Fail)
        coVerify(exactly = 3) { studyAssistApi.postStudyAssist(any(), any(), any()) }
    }

    @Test
    fun `a 502 that recovers on retry returns the eventual success`() = runTest {
        coEvery { studyAssistApi.postStudyAssist(any(), any(), any()) } returnsMany listOf(
            failResult(502, """{"error":"down"}"""),
            DataResult.Success(StudyAssistResponse(response = "Recovered."))
        )

        val result = repository.runTool(COURSE_ID, PAGE_URL, StudyTool.SUMMARIZE, "Summarize")

        assertEquals(StudyToolResult.Summary("Recovered."), result.successData())
        coVerify(exactly = 2) { studyAssistApi.postStudyAssist(any(), any(), any()) }
    }

    private fun mockResponse(response: StudyAssistResponse) {
        coEvery {
            studyAssistApi.postStudyAssist(any(), any(), any())
        } returns DataResult.Success(response)
    }

    private fun mockFailure(code: Int, body: String) {
        coEvery { studyAssistApi.postStudyAssist(any(), any(), any()) } returns failResult(code, body)
    }

    private fun failResult(code: Int, body: String) = DataResult.Fail(
        failure = Failure.Network("error", code),
        response = Response.Builder()
            .code(code)
            .message("error")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("https://canvas.test/api/v1/courses/1/study_assist").build())
            .build(),
        errorBody = body.toResponseBody("application/json".toMediaType())
    )

    private fun <T> DataResult<T>.successData(): T =
        (this as DataResult.Success<T>).data

    companion object {
        private const val COURSE_ID = 28713L
        private const val PAGE_URL = "html-parse-test"
    }
}

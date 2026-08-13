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
package com.instructure.pandautils.domain.usecase.studytools

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.domain.models.studytools.StudyToolsError
import com.instructure.pandautils.domain.models.studytools.StudyToolsOutcome
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyToolsErrorMapperTest {

    private val mapper = StudyToolsErrorMapper()

    @Test
    fun `404 means the feature is unavailable`() {
        val error = mapper.map(fail(404, """{"error":"Study tools aren't enabled for this course."}"""))

        assertTrue(error is StudyToolsError.Unavailable)
        assertEquals("Study tools aren't enabled for this course.", error.message)
    }

    @Test
    fun `403 means the tool is unavailable`() {
        assertTrue(mapper.map(fail(403, """{"error":"nope"}""")) is StudyToolsError.Unavailable)
    }

    @Test
    fun `401 means unavailable rather than a generic failure`() {
        assertTrue(mapper.map(fail(401, """{"error":"nope"}""")) is StudyToolsError.Unavailable)
    }

    @Test
    fun `429 is rate limiting`() {
        val error = mapper.map(fail(429, """{"error":"You've hit the study tools rate limit."}"""))

        assertTrue(error is StudyToolsError.RateLimited)
        assertEquals("You've hit the study tools rate limit.", error.message)
    }

    @Test
    fun `422 keeps both the header and the message`() {
        val error = mapper.map(
            fail(
                422,
                """{"error_header":"This page content cannot be read","error":"Cannot read embedded videos."}"""
            )
        ) as StudyToolsError.ContentProblem

        assertEquals("This page content cannot be read", error.header)
        assertEquals("Cannot read embedded videos.", error.message)
    }

    @Test
    fun `422 without a header still maps the message`() {
        val error = mapper.map(fail(422, """{"error":"Content is too long."}""")) as StudyToolsError.ContentProblem

        assertNull(error.header)
        assertEquals("Content is too long.", error.message)
    }

    @Test
    fun `502 and 503 are temporary`() {
        assertTrue(mapper.map(fail(502, """{"error":"down"}""")) is StudyToolsError.TemporarilyUnavailable)
        assertTrue(mapper.map(fail(503, """{"error":"down"}""")) is StudyToolsError.TemporarilyUnavailable)
    }

    @Test
    fun `a failure with no response falls back to temporary`() {
        val error = mapper.map(DataResult.Fail(Failure.Exception(RuntimeException("boom"), "boom")))

        assertTrue(error is StudyToolsError.TemporarilyUnavailable)
        assertNull(error.message)
    }

    @Test
    fun `a non-JSON error body does not crash the mapper`() {
        val error = mapper.map(fail(422, "<html>gateway</html>"))

        assertTrue(error is StudyToolsError.ContentProblem)
        assertNull(error.message)
    }

    @Test
    fun `toOutcome passes success data through`() {
        val outcome = mapper.toOutcome(DataResult.Success("data"))

        assertEquals("data", (outcome as StudyToolsOutcome.Success).data)
    }

    @Test
    fun `toOutcome converts a failure into a typed error`() {
        val outcome = mapper.toOutcome(fail(429, """{"error":"slow down"}"""))

        assertTrue((outcome as StudyToolsOutcome.Failure).error is StudyToolsError.RateLimited)
    }

    private fun fail(code: Int, body: String) = DataResult.Fail(
        failure = Failure.Network("error", code),
        response = Response.Builder()
            .code(code)
            .message("error")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("https://canvas.test/api/v1/courses/1/study_assist").build())
            .build(),
        errorBody = body.toResponseBody("application/json".toMediaType())
    )
}

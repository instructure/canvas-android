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
 */
package com.instructure.pandautils.data.repository.canvadocs

import com.instructure.canvasapi2.apis.CanvaDocsAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Covers the annotation write endpoints added for the NGC Student Annotation submission flow.
 *
 * The behaviour that actually matters here is the routing: annotations live on the CanvaDocs domain,
 * not the Canvas domain, so these must target the caller-supplied `canvaDocDomain` with an empty
 * `apiVersion` — sending them to `apiPrefs.fullDomain` (or with the default `/api/v1/` prefix) would
 * 404 against canvadocs.
 */
class CanvaDocsRepositoryImplTest {

    private val canvaDocsApi: CanvaDocsAPI.CanvaDocsInterFace = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private lateinit var repository: CanvaDocsRepositoryImpl

    private val sessionId = "session1"
    private val annotationId = "annotation1"
    private val canvaDocDomain = "https://canvadocs.example.com"

    private val annotation = CanvaDocAnnotation(
        annotationId = annotationId,
        page = 3,
        isEditable = true,
        userId = "42",
        userName = "Current User",
        createdAt = "2026-07-24T10:00:00.000Z",
        contents = "a reply",
        annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
        inReplyTo = "head1",
    )

    private fun emptyResponseBody(): ResponseBody = "".toResponseBody(null)

    @Before
    fun setUp() {
        every { apiPrefs.fullDomain } returns "https://canvas.example.com"
        repository = CanvaDocsRepositoryImpl(canvaDocsApi, apiPrefs)
    }

    @Test
    fun `putAnnotation forwards the session and annotation ids and the annotation body`() = runTest {
        val body = slot<CanvaDocAnnotation>()
        coEvery {
            canvaDocsApi.putAnnotation(eq(sessionId), eq(annotationId), capture(body), any())
        } returns DataResult.Success(annotation)

        val result = repository.putAnnotation(sessionId, annotationId, annotation, canvaDocDomain)

        assertTrue(result.isSuccess)
        assertEquals(annotation, body.captured)
    }

    @Test
    fun `putAnnotation targets the canvadocs domain with no api version`() = runTest {
        val params = slot<RestParams>()
        coEvery {
            canvaDocsApi.putAnnotation(any(), any(), any(), capture(params))
        } returns DataResult.Success(annotation)

        repository.putAnnotation(sessionId, annotationId, annotation, canvaDocDomain)

        assertEquals(canvaDocDomain, params.captured.domain)
        assertEquals("", params.captured.apiVersion)
    }

    @Test
    fun `putAnnotation propagates a failure`() = runTest {
        coEvery { canvaDocsApi.putAnnotation(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = repository.putAnnotation(sessionId, annotationId, annotation, canvaDocDomain)

        assertTrue(result.isFail)
    }

    @Test
    fun `deleteAnnotation forwards the session and annotation ids`() = runTest {
        val session = slot<String>()
        val id = slot<String>()
        coEvery {
            canvaDocsApi.deleteAnnotation(capture(session), capture(id), any())
        } returns DataResult.Success(emptyResponseBody())

        val result = repository.deleteAnnotation(sessionId, annotationId, canvaDocDomain)

        assertTrue(result.isSuccess)
        assertEquals(sessionId, session.captured)
        assertEquals(annotationId, id.captured)
    }

    @Test
    fun `deleteAnnotation targets the canvadocs domain with no api version`() = runTest {
        val params = slot<RestParams>()
        coEvery {
            canvaDocsApi.deleteAnnotation(any(), any(), capture(params))
        } returns DataResult.Success(emptyResponseBody())

        repository.deleteAnnotation(sessionId, annotationId, canvaDocDomain)

        assertEquals(canvaDocDomain, params.captured.domain)
        assertEquals("", params.captured.apiVersion)
    }

    @Test
    fun `deleteAnnotation propagates a failure`() = runTest {
        coEvery { canvaDocsApi.deleteAnnotation(any(), any(), any()) } returns DataResult.Fail()

        val result = repository.deleteAnnotation(sessionId, annotationId, canvaDocDomain)

        assertTrue(result.isFail)
    }
}

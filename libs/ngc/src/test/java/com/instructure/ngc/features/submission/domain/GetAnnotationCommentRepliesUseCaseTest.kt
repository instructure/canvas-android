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
package com.instructure.ngc.features.submission.domain

import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotationResponse
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAnnotationCommentRepliesUseCaseTest {

    private val canvaDocsRepository: CanvaDocsRepository = mockk()
    private val useCase = GetAnnotationCommentRepliesUseCase(canvaDocsRepository)

    private val sessionId = "session1"
    private val canvaDocsDomain = "https://canvadocs.example.com"
    private val headAnnotationId = "head1"

    private fun reply(
        id: String,
        createdAt: String? = "2026-07-24T10:00:00.000Z",
        inReplyTo: String? = headAnnotationId,
        deleted: Boolean = false,
        type: CanvaDocAnnotation.AnnotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
    ) = CanvaDocAnnotation(
        annotationId = id,
        page = 1,
        isEditable = true,
        userId = "42",
        userName = "Current User",
        createdAt = createdAt,
        contents = "body-$id",
        annotationType = type,
        inReplyTo = inReplyTo,
        deleted = deleted,
    )

    private suspend fun run(vararg annotations: CanvaDocAnnotation): List<CanvaDocAnnotation> {
        coEvery { canvaDocsRepository.getAnnotations(sessionId, canvaDocsDomain) } returns
            DataResult.Success(CanvaDocAnnotationResponse(data = annotations.toList()))
        return useCase(
            GetAnnotationCommentRepliesUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                headAnnotationId = headAnnotationId,
            )
        )
    }

    @Test
    fun `filters out annotations that are not comment replies`() = runTest {
        val result = run(
            reply("keep"),
            reply("drop", type = CanvaDocAnnotation.AnnotationType.TEXT),
        )

        assertEquals(listOf("keep"), result.map { it.annotationId })
    }

    @Test
    fun `filters out replies belonging to a different head annotation`() = runTest {
        val result = run(
            reply("keep"),
            reply("drop", inReplyTo = "someOtherHead"),
        )

        assertEquals(listOf("keep"), result.map { it.annotationId })
    }

    @Test
    fun `filters out deleted replies`() = runTest {
        val result = run(
            reply("keep"),
            reply("drop", deleted = true),
        )

        assertEquals(listOf("keep"), result.map { it.annotationId })
    }

    @Test
    fun `sorts replies oldest first`() = runTest {
        val result = run(
            reply("third", createdAt = "2026-07-24T12:00:00.000Z"),
            reply("first", createdAt = "2026-07-24T10:00:00.000Z"),
            reply("second", createdAt = "2026-07-24T11:00:00.000Z"),
        )

        assertEquals(listOf("first", "second", "third"), result.map { it.annotationId })
    }

    /**
     * Guards the fix for the review finding that a lexicographic compare of the raw `createdAt` strings
     * only happens to work for fixed-width UTC stamps. Here the chronologically EARLIER reply sorts
     * LATER as a string ("2026-07-24T12..." > "2026-07-24T09...") but is 09:30Z once the +03:00 offset
     * is applied, so a string sort would invert the thread.
     */
    @Test
    fun `sorts by parsed instant rather than raw string when offsets differ`() = runTest {
        val result = run(
            reply("second", createdAt = "2026-07-24T10:00:00.000Z"),
            reply("first", createdAt = "2026-07-24T12:30:00.000+03:00"),
        )

        assertEquals(listOf("first", "second"), result.map { it.annotationId })
    }

    @Test
    fun `replies with an unparseable timestamp sort first and are still returned`() = runTest {
        val result = run(
            reply("dated", createdAt = "2026-07-24T10:00:00.000Z"),
            reply("undated", createdAt = null),
        )

        assertEquals(listOf("undated", "dated"), result.map { it.annotationId })
    }

    @Test
    fun `returns an empty list when nothing matches`() = runTest {
        val result = run(reply("drop", inReplyTo = "someOtherHead"))

        assertEquals(emptyList<String>(), result.map { it.annotationId })
    }
}

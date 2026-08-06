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
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Covers the three mutating canvadocs reply use cases (add / edit / delete). */
class AnnotationCommentReplyUseCasesTest {

    private val canvaDocsRepository: CanvaDocsRepository = mockk()

    private val sessionId = "session1"
    private val canvaDocsDomain = "https://canvadocs.example.com"
    private val documentId = "doc1"
    private val headAnnotationId = "head1"

    private fun reply(id: String, contents: String, userId: String = "42") = CanvaDocAnnotation(
        annotationId = id,
        page = 3,
        isEditable = true,
        userId = userId,
        userName = "Current User",
        createdAt = "2026-07-24T10:00:00.000Z",
        contents = contents,
        annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
        inReplyTo = headAnnotationId,
    )

    // region Add

    @Test
    fun `add - authors the reply as the supplied session user id, not ApiPrefs`() = runTest {
        val useCase = AddAnnotationCommentReplyUseCase(canvaDocsRepository)
        val sent = slot<CanvaDocAnnotation>()
        coEvery {
            canvaDocsRepository.putAnnotation(eq(sessionId), any(), capture(sent), eq(canvaDocsDomain))
        } answers { DataResult.Success(reply("generated", "new comment")) }

        useCase(
            AddAnnotationCommentReplyUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                documentId = documentId,
                headAnnotationId = headAnnotationId,
                page = 3,
                contents = "new comment",
                userId = "session-scoped-7",
            )
        )

        assertEquals("session-scoped-7", sent.captured.userId)
        assertEquals("new comment", sent.captured.contents)
        assertEquals(headAnnotationId, sent.captured.inReplyTo)
        assertEquals(3, sent.captured.page)
        assertEquals(CanvaDocAnnotation.AnnotationType.COMMENT_REPLY, sent.captured.annotationType)
    }

    @Test
    fun `add - patches isEditable on the returned reply because the PUT response omits it`() = runTest {
        val useCase = AddAnnotationCommentReplyUseCase(canvaDocsRepository)
        coEvery { canvaDocsRepository.putAnnotation(any(), any(), any(), any()) } returns
            DataResult.Success(reply("r1", "new comment").apply { isEditable = false })

        val result = useCase(
            AddAnnotationCommentReplyUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                documentId = documentId,
                headAnnotationId = headAnnotationId,
                page = 3,
                contents = "new comment",
                userId = "42",
            )
        )

        assertTrue(result.isEditable)
    }

    @Test(expected = IllegalStateException::class)
    fun `add - propagates a repository failure`() = runTest {
        val useCase = AddAnnotationCommentReplyUseCase(canvaDocsRepository)
        coEvery { canvaDocsRepository.putAnnotation(any(), any(), any(), any()) } returns DataResult.Fail()

        useCase(
            AddAnnotationCommentReplyUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                documentId = documentId,
                headAnnotationId = headAnnotationId,
                page = 3,
                contents = "new comment",
                userId = "42",
            )
        )
    }

    // endregion

    // region Edit

    @Test
    fun `edit - PUTs the same annotation id with only the contents changed`() = runTest {
        val useCase = EditAnnotationCommentReplyUseCase(canvaDocsRepository)
        val existing = reply("r1", "original")
        val sent = slot<CanvaDocAnnotation>()
        coEvery {
            canvaDocsRepository.putAnnotation(eq(sessionId), eq("r1"), capture(sent), eq(canvaDocsDomain))
        } answers { DataResult.Success(reply("r1", "edited")) }

        useCase(
            EditAnnotationCommentReplyUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                reply = existing,
                newContents = "edited",
            )
        )

        assertEquals("r1", sent.captured.annotationId)
        assertEquals("edited", sent.captured.contents)
        // Everything else must survive the edit — there is no partial-update endpoint.
        assertEquals(existing.userId, sent.captured.userId)
        assertEquals(existing.page, sent.captured.page)
        assertEquals(existing.inReplyTo, sent.captured.inReplyTo)
        // The source annotation must not be mutated in place.
        assertEquals("original", existing.contents)
    }

    @Test(expected = IllegalStateException::class)
    fun `edit - propagates a repository failure`() = runTest {
        val useCase = EditAnnotationCommentReplyUseCase(canvaDocsRepository)
        coEvery { canvaDocsRepository.putAnnotation(any(), any(), any(), any()) } returns DataResult.Fail()

        useCase(
            EditAnnotationCommentReplyUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                reply = reply("r1", "original"),
                newContents = "edited",
            )
        )
    }

    // endregion

    // region Delete

    @Test
    fun `delete - deletes by id against the canvadocs domain`() = runTest {
        val useCase = DeleteAnnotationCommentReplyUseCase(canvaDocsRepository)
        coEvery { canvaDocsRepository.deleteAnnotation(any(), any(), any()) } returns DataResult.Success(Unit)

        useCase(
            DeleteAnnotationCommentReplyUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                annotationId = "r1",
            )
        )

        coVerify { canvaDocsRepository.deleteAnnotation(sessionId, "r1", canvaDocsDomain) }
    }

    @Test(expected = IllegalStateException::class)
    fun `delete - propagates a repository failure`() = runTest {
        val useCase = DeleteAnnotationCommentReplyUseCase(canvaDocsRepository)
        coEvery { canvaDocsRepository.deleteAnnotation(any(), any(), any()) } returns DataResult.Fail()

        useCase(
            DeleteAnnotationCommentReplyUseCase.Params(
                sessionId = sessionId,
                canvaDocsDomain = canvaDocsDomain,
                annotationId = "r1",
            )
        )
    }

    // endregion
}

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
package com.instructure.ngc.features.assignmentdetails

import android.content.Context
import android.net.Uri
import com.instructure.annotations.convertCanvaDocAnnotationToPDF
import com.instructure.annotations.getCanvaDocsRedirect
import com.instructure.canvasapi2.models.AnnotationMetadata
import com.instructure.canvasapi2.models.AnnotationUrls
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotationResponse
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.extractCanvaDocsDomain
import com.instructure.canvasapi2.utils.extractSessionId
import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import com.pspdfkit.annotations.Annotation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class NgcAnnotatedPdfUseCaseTest {

    private val context: Context = mockk(relaxed = true)
    private val canvaDocsRepository: CanvaDocsRepository = mockk(relaxed = true)
    private val fileCache: FileCache = mockk(relaxed = true)

    private lateinit var useCase: NgcAnnotatedPdfUseCase

    @Before
    fun setUp() {
        useCase = NgcAnnotatedPdfUseCase(context, canvaDocsRepository, fileCache)
        mockkStatic("com.instructure.annotations.CanvaDocsExtensionsKt")
        mockkStatic("com.instructure.canvasapi2.utils.CanvaDocExtensionsKt")
        mockkStatic("com.instructure.pandautils.utils.filecache.FileCacheKt")
        mockkStatic("com.instructure.annotations.AnnotationConverterKt")
        mockkStatic(Uri::class)
        mockkObject(ApiPrefs)
        every { ApiPrefs.overrideDomains } returns mutableMapOf()
        coEvery { getCanvaDocsRedirect(any(), any()) } returns "https://canvadocs.example.com/redirect"
        every { extractCanvaDocsDomain(any()) } returns "https://canvadocs.example.com"
        every { extractSessionId(any()) } returns "session-1"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `returns null when session creation fails`() = runTest {
        coEvery { canvaDocsRepository.createCanvaDocSession(any(), any()) } returns DataResult.Fail()

        assertNull(useCase(NgcAnnotatedPdfUseCase.Params(1L, 1L, 1L)))
    }

    @Test
    fun `returns null when the session url is blank`() = runTest {
        stubSession("")

        assertNull(useCase(NgcAnnotatedPdfUseCase.Params(1L, 1L, 1L)))
    }

    @Test
    fun `returns null when the pdf download fails`() = runTest {
        stubSession("https://host/canvadoc")
        coEvery { canvaDocsRepository.getCanvaDoc(any()) } returns DataResult.Success(docSession(canRead = true))
        coEvery { fileCache.awaitFileDownload(any(), any()) } returns null

        assertNull(useCase(NgcAnnotatedPdfUseCase.Params(1L, 1L, 1L)))
    }

    @Test
    fun `returns the pdf and only the non-deleted converted annotations on success`() = runTest {
        stubSession("https://host/canvadoc")
        coEvery { canvaDocsRepository.getCanvaDoc(any()) } returns DataResult.Success(docSession(canRead = true))
        val file = mockk<File>(relaxed = true)
        coEvery { fileCache.awaitFileDownload(any(), any()) } returns file
        val uri = mockk<Uri>()
        every { Uri.fromFile(file) } returns uri
        coEvery { canvaDocsRepository.getAnnotations(any(), any()) } returns DataResult.Success(
            CanvaDocAnnotationResponse(
                data = listOf(
                    CanvaDocAnnotation(annotationId = "kept", page = 0, isEditable = false, deleted = false),
                    CanvaDocAnnotation(annotationId = "dropped", page = 0, isEditable = false, deleted = true),
                ),
            )
        )
        val converted = mockk<Annotation>()
        every { any<CanvaDocAnnotation>().convertCanvaDocAnnotationToPDF(any()) } returns converted

        val result = useCase(NgcAnnotatedPdfUseCase.Params(10L, 2L, 3L))

        assertEquals(uri, result?.uri)
        // The deleted annotation is filtered out before conversion, leaving one.
        assertEquals(listOf(converted), result?.annotations)
    }

    @Test
    fun `does not fetch annotations when the session is not readable`() = runTest {
        stubSession("https://host/canvadoc")
        coEvery { canvaDocsRepository.getCanvaDoc(any()) } returns DataResult.Success(docSession(canRead = false))
        val file = mockk<File>(relaxed = true)
        coEvery { fileCache.awaitFileDownload(any(), any()) } returns file
        every { Uri.fromFile(file) } returns mockk()

        val result = useCase(NgcAnnotatedPdfUseCase.Params(10L, 2L, 3L))

        assertEquals(emptyList<Annotation>(), result?.annotations)
        coVerify(exactly = 0) { canvaDocsRepository.getAnnotations(any(), any()) }
    }

    private fun stubSession(sessionUrl: String) {
        coEvery { canvaDocsRepository.createCanvaDocSession(any(), any()) } returns
            DataResult.Success(CanvaDocSessionResponseBody(canvadocsSessionUrl = sessionUrl))
    }

    private fun docSession(canRead: Boolean) = DocSession(
        documentId = "doc",
        annotationUrls = AnnotationUrls(pdfDownload = "/pdf", annotatedPdfDownload = "/annotated.pdf"),
        annotationMetadata = AnnotationMetadata(
            enabled = true,
            userName = "student",
            userId = "1",
            permissions = if (canRead) "read" else null,
        ),
    )
}

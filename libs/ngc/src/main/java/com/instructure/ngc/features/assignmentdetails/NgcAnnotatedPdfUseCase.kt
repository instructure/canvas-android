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
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.extractCanvaDocsDomain
import com.instructure.canvasapi2.utils.extractSessionId
import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import com.pspdfkit.annotations.Annotation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** The downloaded (raw) submission PDF plus the annotations to draw on top of it. */
data class NgcAnnotatedPdf(val uri: Uri, val annotations: List<Annotation>)

/**
 * Resolves a `student_annotation` submission to a local PDF plus its annotations, so the NGC preview
 * can render it with [com.instructure.annotations.compose.PdfPreview]. Mirrors the data half of the
 * Academic annotation viewer ([com.instructure.annotations.PdfSubmissionView]) without its heavy,
 * editing-oriented PSPDFKit fragment stack:
 *
 * 1. create a CanvaDoc session for the submission attempt,
 * 2. follow the session redirect and fetch the [DocSession],
 * 3. download the raw PDF ([DocSession.annotationUrls]'s `pdf_download`),
 * 4. fetch the annotations for the session and convert them to PSPDFKit annotations.
 *
 * The annotations-baked-in `annotated_pdf_download` isn't served for a student session, so we draw
 * the annotations ourselves instead.
 */
class NgcAnnotatedPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canvaDocsRepository: CanvaDocsRepository,
    private val fileCache: FileCache,
) : BaseUseCase<NgcAnnotatedPdfUseCase.Params, NgcAnnotatedPdf?>() {

    data class Params(val submissionId: Long, val attempt: Long, val courseId: Long)

    /** Returns the raw PDF + its annotations, or null when the session/PDF can't be resolved. */
    override suspend fun execute(params: Params): NgcAnnotatedPdf? {
        val sessionUrl = canvaDocsRepository.createCanvaDocSession(params.submissionId, params.attempt.toString())
            .dataOrNull
            ?.canvadocsSessionUrl
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val redirectUrl = getCanvaDocsRedirect(sessionUrl, ApiPrefs.overrideDomains[params.courseId])
        val docSession = canvaDocsRepository.getCanvaDoc(redirectUrl).dataOrThrow
        val domain = extractCanvaDocsDomain(redirectUrl)

        val pdfUrl = domain + docSession.annotationUrls.pdfDownload
        val file = fileCache.awaitFileDownload(pdfUrl) ?: return null
        val sessionId = extractSessionId(pdfUrl)

        val annotations = if (docSession.annotationMetadata?.canRead() == true) {
            canvaDocsRepository.getAnnotations(sessionId, domain)
                .dataOrThrow
                .data
                .filterNot { it.deleted }
                .mapNotNull { it.convertCanvaDocAnnotationToPDF(context) }
        } else {
            emptyList()
        }

        return NgcAnnotatedPdf(Uri.fromFile(file), annotations)
    }
}

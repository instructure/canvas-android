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
import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Loads every non-deleted `COMMENT_REPLY` annotation belonging to a single head/parent annotation
 * (i.e. the reply thread shown by the Compose-native comment-thread screen), sorted oldest-first —
 * mirrors the sort order the classic `AnnotationCommentListRecyclerAdapter` renders replies in.
 *
 * The canvadocs `getAnnotations` endpoint returns every annotation in the session (not just replies for
 * one head annotation), so this filters client-side by [Params.headAnnotationId], the same way
 * [com.instructure.annotations.PdfSubmissionView.loadAnnotations] groups replies into its
 * `commentRepliesHashMap`.
 */
class GetAnnotationCommentRepliesUseCase @Inject constructor(
    private val canvaDocsRepository: CanvaDocsRepository,
) : BaseUseCase<GetAnnotationCommentRepliesUseCase.Params, List<CanvaDocAnnotation>>() {

    data class Params(
        val sessionId: String,
        val canvaDocsDomain: String,
        val headAnnotationId: String,
    )

    override suspend fun execute(params: Params): List<CanvaDocAnnotation> {
        val annotations = canvaDocsRepository.getAnnotations(params.sessionId, params.canvaDocsDomain).dataOrThrow

        return annotations.data
            .filter {
                it.annotationType == CanvaDocAnnotation.AnnotationType.COMMENT_REPLY &&
                    it.inReplyTo == params.headAnnotationId &&
                    !it.deleted
            }
            // Sort on the PARSED instant, not the raw string: a lexicographic compare of createdAt is
            // only correct while every value is a fixed-width UTC ISO-8601 stamp, and would silently
            // mis-order the thread given a numeric offset (+02:00) or varying fractional-second
            // precision. Unparseable values sort first, keeping the ordering total and stable.
            .sortedBy { parseCanvaDocsTimestamp(it.createdAt)?.time ?: Long.MIN_VALUE }
    }
}

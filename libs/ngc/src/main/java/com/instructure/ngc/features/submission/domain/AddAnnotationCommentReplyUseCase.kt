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

import com.instructure.annotations.createCommentReplyAnnotation
import com.instructure.annotations.generateAnnotationId
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Adds a new `COMMENT_REPLY` annotation to an existing thread. Mirrors classic
 * `AnnotationCommentListFragment.sendComment`/[com.instructure.annotations.PdfSubmissionView.createCommentAnnotation]:
 * builds the reply via [createCommentReplyAnnotation] with a freshly generated id, then PUTs it — the
 * canvadocs annotations endpoint upserts, so a PUT with a new id creates the annotation.
 */
class AddAnnotationCommentReplyUseCase @Inject constructor(
    private val canvaDocsRepository: CanvaDocsRepository,
) : BaseUseCase<AddAnnotationCommentReplyUseCase.Params, CanvaDocAnnotation>() {

    data class Params(
        val sessionId: String,
        val canvaDocsDomain: String,
        val documentId: String,
        val headAnnotationId: String,
        val page: Int,
        val contents: String,
        /**
         * The canvadocs-session-scoped `AnnotationMetadata.userId` to author the reply as — the same id
         * `AnnotationCommentsViewModel.canEdit`/`canDelete` compare against. Passed in rather than read
         * from [com.instructure.canvasapi2.utils.ApiPrefs] here so the write side is keyed on the same
         * identity as the read side (classic has the same latent mismatch, but masked it by re-reading
         * the thread from the server); it also keeps this use case free of singleton state for tests.
         */
        val userId: String,
    )

    override suspend fun execute(params: Params): CanvaDocAnnotation {
        val newReply = createCommentReplyAnnotation(
            contents = params.contents,
            inReplyTo = params.headAnnotationId,
            canvaDocId = params.documentId,
            userId = params.userId,
            page = params.page,
        )

        val persisted = canvaDocsRepository.putAnnotation(
            params.sessionId,
            generateAnnotationId(),
            newReply,
            params.canvaDocsDomain,
        ).dataOrThrow

        // The PUT response omits this field (classic patches it client-side too — see
        // PdfSubmissionView.createCommentAnnotation).
        persisted.isEditable = true
        return persisted
    }
}

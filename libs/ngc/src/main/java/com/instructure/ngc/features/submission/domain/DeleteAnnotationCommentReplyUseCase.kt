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

import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/** Deletes a single `COMMENT_REPLY` annotation by id. Mirrors classic `AnnotationCommentListFragment.deleteComment`. */
class DeleteAnnotationCommentReplyUseCase @Inject constructor(
    private val canvaDocsRepository: CanvaDocsRepository,
) : BaseUseCase<DeleteAnnotationCommentReplyUseCase.Params, Unit>() {

    data class Params(
        val sessionId: String,
        val canvaDocsDomain: String,
        val annotationId: String,
    )

    override suspend fun execute(params: Params) {
        canvaDocsRepository.deleteAnnotation(params.sessionId, params.annotationId, params.canvaDocsDomain).dataOrThrow
    }
}

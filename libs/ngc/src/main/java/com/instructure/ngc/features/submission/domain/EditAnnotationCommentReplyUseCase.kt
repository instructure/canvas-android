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
 * Edits an existing `COMMENT_REPLY` annotation's text. There is no separate edit endpoint — mirrors
 * classic `AnnotationCommentListFragment.editComment`: mutate `contents` on the existing annotation and
 * PUT it back to the same id.
 */
class EditAnnotationCommentReplyUseCase @Inject constructor(
    private val canvaDocsRepository: CanvaDocsRepository,
) : BaseUseCase<EditAnnotationCommentReplyUseCase.Params, CanvaDocAnnotation>() {

    data class Params(
        val sessionId: String,
        val canvaDocsDomain: String,
        val reply: CanvaDocAnnotation,
        val newContents: String,
    )

    override suspend fun execute(params: Params): CanvaDocAnnotation {
        val updated = params.reply.copy(contents = params.newContents)

        return canvaDocsRepository.putAnnotation(
            params.sessionId,
            updated.annotationId,
            updated,
            params.canvaDocsDomain,
        ).dataOrThrow.apply { isEditable = true }
    }
}

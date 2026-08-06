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

/** The canvadocs "draft" session for a not-yet-submitted `student_annotation` attempt. */
data class CanvaDocSessionData(
    val sessionUrl: String,
)

/**
 * Creates the canvadocs "draft" session used to render+annotate a `student_annotation` assignment's
 * document inline, before the attempt has been submitted.
 *
 * Mirrors classic [com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionViewModel]:
 * the session is keyed by the assignment's placeholder submission id (present as soon as the
 * assignment exists, even before any attempt), requested with the fixed `"draft"` attempt marker. The
 * submission id is supplied by the caller (sourced from [SubmissionData], which already fetches the
 * assignment once via `GetSubmissionDataUseCase`) rather than re-fetched here, avoiding a redundant
 * assignment network call. Uses the coroutine [CanvaDocsRepository] — not the deprecated
 * `CanvaDocsRedirectAsyncTask`.
 */
class GetCanvaDocSessionUseCase @Inject constructor(
    private val canvaDocsRepository: CanvaDocsRepository,
) : BaseUseCase<GetCanvaDocSessionUseCase.Params, CanvaDocSessionData>() {

    data class Params(
        val submissionId: Long?,
    )

    override suspend fun execute(params: Params): CanvaDocSessionData {
        val submissionId = params.submissionId
            ?: error("No submission id available to create a canvadocs draft session")

        val session = canvaDocsRepository.createCanvaDocSession(submissionId, DRAFT_ATTEMPT).dataOrThrow

        val sessionUrl = session.canvadocsSessionUrl.orEmpty()
            .ifBlank { error("Blank canvadocs session url") }

        return CanvaDocSessionData(sessionUrl = sessionUrl)
    }

    companion object {
        private const val DRAFT_ATTEMPT = "draft"
    }
}

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
package com.instructure.ngc.features.comments.domain

import com.instructure.pandautils.data.repository.submissioncomments.SubmissionCommentsRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Marks the given submission comments read via GraphQL. No-op when there are no unread comments.
 */
class MarkSubmissionCommentsReadUseCase @Inject constructor(
    private val submissionCommentsRepository: SubmissionCommentsRepository,
) : BaseUseCase<MarkSubmissionCommentsReadUseCase.Params, Unit>() {

    data class Params(
        val submissionId: Long,
        val commentIds: List<Long>,
    )

    override suspend fun execute(params: Params) {
        if (params.commentIds.isEmpty()) return
        submissionCommentsRepository.markSubmissionCommentsRead(params.submissionId, params.commentIds)
    }
}

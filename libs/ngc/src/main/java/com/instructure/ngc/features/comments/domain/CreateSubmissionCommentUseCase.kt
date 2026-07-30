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
 * Posts a text submission comment via GraphQL and returns the created comment's id and timestamp so the
 * caller can reconcile its optimistic (pending) copy.
 */
class CreateSubmissionCommentUseCase @Inject constructor(
    private val submissionCommentsRepository: SubmissionCommentsRepository,
) : BaseUseCase<CreateSubmissionCommentUseCase.Params, CreatedSubmissionComment>() {

    data class Params(
        val submissionId: Long,
        val comment: String,
        val attempt: Int? = null,
    )

    override suspend fun execute(params: Params): CreatedSubmissionComment {
        val result = submissionCommentsRepository.createSubmissionComment(
            submissionId = params.submissionId,
            comment = params.comment,
            attempt = params.attempt
        )
        val created = result.createSubmissionComment?.submissionComment
        return CreatedSubmissionComment(
            id = created?._id.orEmpty(),
            createdAt = created?.createdAt
        )
    }
}

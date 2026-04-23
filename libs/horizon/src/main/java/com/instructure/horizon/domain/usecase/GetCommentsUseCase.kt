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
package com.instructure.horizon.domain.usecase

import com.instructure.canvasapi2.managers.graphql.horizon.CommentsData
import com.instructure.horizon.features.moduleitemsequence.content.assignment.comments.CommentsRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val repository: CommentsRepository,
) : BaseUseCase<GetCommentsUseCase.Params, CommentsData>() {

    data class Params(
        val assignmentId: Long,
        val userId: Long,
        val attempt: Int,
        val forceNetwork: Boolean = false,
        val startCursor: String? = null,
        val endCursor: String? = null,
        val nextPage: Boolean = false,
    )

    override suspend fun execute(params: Params): CommentsData {
        return repository.getComments(
            assignmentId = params.assignmentId,
            userId = params.userId,
            attempt = params.attempt,
            forceNetwork = params.forceNetwork,
            startCursor = params.startCursor,
            endCursor = params.endCursor,
            nextPage = params.nextPage,
        )
    }
}
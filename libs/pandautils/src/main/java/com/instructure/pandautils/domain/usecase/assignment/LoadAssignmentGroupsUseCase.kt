/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.domain.usecase.assignment

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoadAssignmentGroupsUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : BaseUseCase<LoadAssignmentGroupsUseCase.Params, List<AssignmentGroup>>() {

    override suspend fun execute(params: Params): List<AssignmentGroup> {
        return assignmentRepository.getAssignmentGroups(params.courseId, params.forceNetwork).dataOrThrow
    }

    data class Params(
        val courseId: Long,
        val forceNetwork: Boolean = false
    )
}
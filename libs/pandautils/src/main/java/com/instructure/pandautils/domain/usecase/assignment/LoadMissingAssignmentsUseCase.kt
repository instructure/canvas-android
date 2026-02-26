/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.assignment

import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadMissingAssignmentsParams(
    val forceRefresh: Boolean = false
)

class LoadMissingAssignmentsUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : BaseUseCase<LoadMissingAssignmentsParams, List<Assignment>>() {

    override suspend fun execute(params: LoadMissingAssignmentsParams): List<Assignment> {
        return assignmentRepository.getMissingAssignments(params.forceRefresh).dataOrThrow
    }
}
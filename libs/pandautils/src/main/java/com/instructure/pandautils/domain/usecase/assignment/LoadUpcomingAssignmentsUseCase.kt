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

import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.data.repository.planner.PlannerRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadUpcomingAssignmentsParams(
    val startDate: String,
    val endDate: String,
    val contextCodes: List<String> = emptyList(),
    val forceRefresh: Boolean = false
)

class LoadUpcomingAssignmentsUseCase @Inject constructor(
    private val plannerRepository: PlannerRepository
) : BaseUseCase<LoadUpcomingAssignmentsParams, List<PlannerItem>>() {

    override suspend fun execute(params: LoadUpcomingAssignmentsParams): List<PlannerItem> {
        return plannerRepository.getPlannerItems(
            params.startDate,
            params.endDate,
            params.contextCodes,
            params.forceRefresh
        ).dataOrThrow
    }
}
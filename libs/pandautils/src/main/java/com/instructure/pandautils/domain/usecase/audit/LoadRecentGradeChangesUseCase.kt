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

package com.instructure.pandautils.domain.usecase.audit

import com.instructure.canvasapi2.models.GradeChange
import com.instructure.pandautils.data.repository.audit.AuditRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadRecentGradeChangesParams(
    val studentId: Long,
    val startTime: String? = null,
    val endTime: String? = null,
    val forceRefresh: Boolean = false
)

class LoadRecentGradeChangesUseCase @Inject constructor(
    private val auditRepository: AuditRepository
) : BaseUseCase<LoadRecentGradeChangesParams, List<GradeChange>>() {

    override suspend fun execute(params: LoadRecentGradeChangesParams): List<GradeChange> {
        val gradeChanges = auditRepository.getGradeChanges(
            params.studentId,
            params.startTime,
            params.endTime,
            params.forceRefresh
        ).dataOrThrow

        return gradeChanges
            .groupBy { it.links?.assignment }
            .mapNotNull { (_, changes) -> changes.maxByOrNull { it.versionNumber } }
            .sortedByDescending { it.createdAt }
    }
}
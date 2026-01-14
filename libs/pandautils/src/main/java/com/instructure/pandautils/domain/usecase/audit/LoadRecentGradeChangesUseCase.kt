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

import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.data.model.GradedSubmission
import com.instructure.pandautils.data.repository.submission.SubmissionRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadRecentGradeChangesParams(
    val studentId: Long,
    val startTime: String? = null,
    val endTime: String? = null,
    val forceRefresh: Boolean = false
)

class LoadRecentGradeChangesUseCase @Inject constructor(
    private val submissionRepository: SubmissionRepository
) : BaseUseCase<LoadRecentGradeChangesParams, List<GradedSubmission>>() {

    override suspend fun execute(params: LoadRecentGradeChangesParams): List<GradedSubmission> {
        val gradedSince = params.startTime ?: return emptyList()

        val submissions = submissionRepository.getRecentGradedSubmissions(
            studentId = params.studentId,
            gradedSince = gradedSince,
            forceRefresh = params.forceRefresh
        ).dataOrThrow

        val filtered = if (params.endTime != null) {
            val endDate = params.endTime.toDate()
            submissions.filter { submission ->
                val gradedAt = submission.gradedAt
                gradedAt == null || endDate == null || !gradedAt.after(endDate)
            }
        } else {
            submissions
        }

        return filtered.sortedByDescending { it.gradedAt }
    }
}
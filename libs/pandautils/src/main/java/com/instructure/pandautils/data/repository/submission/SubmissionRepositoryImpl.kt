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

package com.instructure.pandautils.data.repository.submission

import com.instructure.canvasapi2.managers.graphql.RecentGradedSubmissionsManager
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.model.GradedSubmission

class SubmissionRepositoryImpl(
    private val recentGradedSubmissionsManager: RecentGradedSubmissionsManager
) : SubmissionRepository {

    override suspend fun getRecentGradedSubmissions(
        studentId: Long,
        gradedSince: String,
        forceRefresh: Boolean
    ): DataResult<List<GradedSubmission>> {
        return try {
            val data = recentGradedSubmissionsManager.getRecentGradedSubmissions(
                studentId = studentId,
                gradedSince = gradedSince,
                pageSize = 20,
                forceNetwork = forceRefresh
            )

            val submissions = data.allCourses
                ?.flatMap { course ->
                    course.submissions?.edges
                        ?.mapNotNull { edge ->
                            val submission = edge?.node ?: return@mapNotNull null
                            val assignment = submission.assignment ?: return@mapNotNull null

                            if (submission.gradeHidden) {
                                return@mapNotNull null
                            }

                            GradedSubmission(
                                submissionId = submission._id.toLongOrNull() ?: 0,
                                assignmentId = assignment._id.toLongOrNull() ?: 0,
                                assignmentName = assignment.name ?: "",
                                courseId = course._id.toLongOrNull() ?: 0,
                                courseName = course.name,
                                score = submission.score,
                                grade = submission.grade,
                                gradedAt = submission.gradedAt,
                                excused = submission.excused == true,
                                assignmentUrl = assignment.htmlUrl,
                                pointsPossible = assignment.pointsPossible,
                                gradingType = assignment.gradingType
                            )
                        } ?: emptyList()
                } ?: emptyList()

            DataResult.Success(submissions)
        } catch (e: Exception) {
            DataResult.Fail()
        }
    }
}
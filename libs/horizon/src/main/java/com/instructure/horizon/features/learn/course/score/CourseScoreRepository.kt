/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.learn.course.score

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class CourseScoreRepository @Inject constructor(
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val apiPrefs: ApiPrefs
) {
    suspend fun getAssignmentGroups(courseId: Long, forceRefresh: Boolean = false): List<AssignmentGroup> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams)
            .depaginate { assignmentApi.getNextPageAssignmentGroupListWithAssignments(it, restParams) }
            .dataOrThrow
    }

    suspend fun getEnrollments(courseId: Long, forceNetwork: Boolean): List<Enrollment> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return enrollmentApi.getEnrollmentsForUserInCourse(courseId, apiPrefs.user?.id ?: -1, restParams)
            .depaginate { enrollmentApi.getNextPage(it, restParams) }
            .dataOrThrow
    }
}
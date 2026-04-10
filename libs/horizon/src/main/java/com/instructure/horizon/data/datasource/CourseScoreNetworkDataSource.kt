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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class CourseScoreNetworkDataSource @Inject constructor(
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val apiPrefs: ApiPrefs,
) {

    suspend fun getAssignmentGroups(courseId: Long, forceRefresh: Boolean): List<AssignmentGroup> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams)
            .depaginate { assignmentApi.getNextPageAssignmentGroupListWithAssignments(it, restParams) }
            .dataOrThrow
    }

    suspend fun getEnrollments(courseId: Long, forceRefresh: Boolean): List<Enrollment> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return enrollmentApi.getEnrollmentsForUserInCourse(courseId, apiPrefs.user?.id ?: -1, restParams)
            .depaginate { enrollmentApi.getNextPage(it, restParams) }
            .dataOrThrow
    }
}

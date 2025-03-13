/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.assignments.list.datasource

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class AssignmentListNetworkDataSource(
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val courseApi: CourseAPI.CoursesInterface
) : AssignmentListDataSource {

    override suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean
    ): DataResult<List<AssignmentGroup>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
            courseId = courseId,
            gradingPeriodId = gradingPeriodId,
            scopeToStudent = scopeToStudent,
            restParams = params
        ).depaginate {
            assignmentApi.getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(it, params)
        }
    }

    override suspend fun getAssignmentGroupsWithAssignments(courseId: Long, forceNetwork: Boolean): DataResult<List<AssignmentGroup>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, params).depaginate {
            assignmentApi.getNextPageAssignmentGroupListWithAssignments(it, params)
        }
    }

    override suspend fun getGradingPeriodsForCourse(courseId: Long, forceNetwork: Boolean): DataResult<List<GradingPeriod>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        val gradingPeriods = courseApi.getGradingPeriodsForCourse(courseId, params).dataOrNull?.gradingPeriodList
        if (gradingPeriods != null) {
            return DataResult.Success(gradingPeriods)
        } else {
            return DataResult.Fail()
        }
    }

    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        return courseApi.getCourseWithGrade(courseId, params)
    }
}

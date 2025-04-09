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

package com.instructure.student.features.grades.datasource

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.depaginate

class GradesListNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
) : GradesListDataSource {

    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        return courseApi.getCourseWithGrade(courseId, params).dataOrThrow
    }

    override suspend fun getObserveeEnrollments(forceNetwork: Boolean): List<Enrollment> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return enrollmentApi.firstPageObserveeEnrollments(params).depaginate {
            enrollmentApi.getNextPage(it, params)
        }.dataOrThrow
    }

    override suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean
    ): List<AssignmentGroup> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
            courseId = courseId,
            gradingPeriodId = gradingPeriodId,
            scopeToStudent = scopeToStudent,
            restParams = params
        ).depaginate {
            assignmentApi.getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(it, params)
        }.dataOrThrow
    }

    override suspend fun getSubmissionsForMultipleAssignments(
        studentId: Long,
        courseId: Long,
        assignmentIds: List<Long>,
        forceNetwork: Boolean
    ): List<Submission> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return submissionApi.getSubmissionsForMultipleAssignments(
            courseId, studentId, assignmentIds, params
        ).depaginate {
            submissionApi.getNextPageSubmissions(it, params)
        }.dataOrThrow
    }

    override suspend fun getCoursesWithSyllabus(forceNetwork: Boolean): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return courseApi.firstPageCoursesWithSyllabus(params).depaginate {
            courseApi.next(it, params)
        }.dataOrThrow
    }

    override suspend fun getGradingPeriodsForCourse(courseId: Long, forceNetwork: Boolean): List<GradingPeriod> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        return courseApi.getGradingPeriodsForCourse(courseId, params).dataOrThrow.gradingPeriodList
    }

    override suspend fun getUserEnrollmentsForGradingPeriod(
        courseId: Long,
        userId: Long,
        gradingPeriodId: Long?,
        forceNetwork: Boolean
    ): List<Enrollment> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        return courseApi.getUserEnrollmentsForGradingPeriod(courseId, userId, gradingPeriodId, params).dataOrThrow
    }

    override suspend fun getAssignmentGroupsWithAssignments(courseId: Long, forceNetwork: Boolean): List<AssignmentGroup> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, params).depaginate {
            assignmentApi.getNextPageAssignmentGroupListWithAssignments(it, params)
        }.dataOrThrow
    }
}

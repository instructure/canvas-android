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

package com.instructure.student.features.grades

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.filterHiddenAssignments
import com.instructure.student.features.grades.datasource.GradesListDataSource
import com.instructure.student.features.grades.datasource.GradesListLocalDataSource
import com.instructure.student.features.grades.datasource.GradesListNetworkDataSource

class GradesListRepository(
    localDataSource: GradesListLocalDataSource,
    networkDataSource: GradesListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<GradesListDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        return dataSource().getCourseWithGrade(courseId, forceNetwork)
    }

    suspend fun getObserveeEnrollments(forceNetwork: Boolean): List<Enrollment> {
        return dataSource().getObserveeEnrollments(forceNetwork)
    }

    suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean
    ): List<AssignmentGroup> {
        return dataSource().getAssignmentGroupsWithAssignmentsForGradingPeriod(courseId, gradingPeriodId, scopeToStudent, forceNetwork).filterHiddenAssignments()
    }

    suspend fun getSubmissionsForMultipleAssignments(
        studentId: Long,
        courseId: Long,
        assignmentIds: List<Long>,
        forceNetwork: Boolean
    ): List<Submission> {
        return dataSource().getSubmissionsForMultipleAssignments(studentId, courseId, assignmentIds, forceNetwork)
    }

    suspend fun getCoursesWithSyllabus(forceNetwork: Boolean): List<Course> {
        return dataSource().getCoursesWithSyllabus(forceNetwork)
    }

    suspend fun getGradingPeriodsForCourse(courseId: Long, forceNetwork: Boolean): List<GradingPeriod> {
        return dataSource().getGradingPeriodsForCourse(courseId, forceNetwork)
    }

    suspend fun getUserEnrollmentsForGradingPeriod(
        courseId: Long,
        userId: Long,
        gradingPeriodId: Long,
        forceNetwork: Boolean
    ): List<Enrollment> {
        return dataSource().getUserEnrollmentsForGradingPeriod(courseId, userId, gradingPeriodId, forceNetwork)
    }

    suspend fun getAssignmentGroupsWithAssignments(
        courseId: Long,
        forceNetwork: Boolean,
    ): List<AssignmentGroup> {
        return dataSource().getAssignmentGroupsWithAssignments(courseId, forceNetwork).filterHiddenAssignments()
    }
}

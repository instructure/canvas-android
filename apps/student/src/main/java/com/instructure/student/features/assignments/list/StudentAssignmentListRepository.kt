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

package com.instructure.student.features.assignments.list

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.assignments.list.AssignmentListRepository
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.list.datasource.AssignmentListDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListLocalDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListNetworkDataSource

class StudentAssignmentListRepository(
    localDataSource: AssignmentListLocalDataSource,
    networkDataSource: AssignmentListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<AssignmentListDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider), AssignmentListRepository {

    suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean
    ): DataResult<List<AssignmentGroup>> {
        return dataSource().getAssignmentGroupsWithAssignmentsForGradingPeriod(
            courseId,
            gradingPeriodId,
            scopeToStudent,
            forceNetwork
        )
    }

    override suspend fun getAssignments(
        courseId: Long,
        forceRefresh: Boolean
    ): DataResult<List<AssignmentGroup>> {
        return dataSource().getAssignmentGroupsWithAssignments(courseId, forceRefresh)
    }

    suspend fun getGradingPeriodsForCourse(
        courseId: Long,
        isRefresh: Boolean
    ): DataResult<List<GradingPeriod>> {
        return dataSource().getGradingPeriodsForCourse(courseId, isRefresh)
    }

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        return dataSource().getCourseWithGrade(courseId, forceRefresh)
    }
}

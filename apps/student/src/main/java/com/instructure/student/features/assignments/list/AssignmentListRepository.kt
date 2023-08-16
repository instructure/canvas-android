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
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.list.datasource.AssignmentListDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListLocalDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListNetworkDataSource

class AssignmentListRepository(
    localDataSource: AssignmentListLocalDataSource,
    networkDataSource: AssignmentListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<AssignmentListDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean
    ): List<AssignmentGroup> {
        return dataSource().getAssignmentGroupsWithAssignmentsForGradingPeriod(
            courseId,
            gradingPeriodId,
            scopeToStudent,
            forceNetwork
        )
    }

    suspend fun getAssignmentGroupsWithAssignments(
        courseId: Long,
        isRefresh: Boolean
    ): List<AssignmentGroup> {
        return dataSource().getAssignmentGroupsWithAssignments(courseId, isRefresh)
    }

    suspend fun getGradingPeriodsForCourse(
        courseId: Long,
        isRefresh: Boolean
    ): List<GradingPeriod> {
        return dataSource().getGradingPeriodsForCourse(courseId, isRefresh)
    }

    suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return dataSource().loadCourseSettings(courseId, forceNetwork)
    }
}

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
import com.instructure.pandautils.features.assignments.list.AssignmentListRepository
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListSelectedFiltersEntityDao
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListSelectedFiltersEntity
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.list.datasource.AssignmentListDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListLocalDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListNetworkDataSource

class StudentAssignmentListRepository(
    localDataSource: AssignmentListLocalDataSource,
    networkDataSource: AssignmentListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    private val assignmentListSelectedFiltersEntityDao: AssignmentListSelectedFiltersEntityDao
) : Repository<AssignmentListDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider), AssignmentListRepository {

    override suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        forceRefresh: Boolean
    ): List<AssignmentGroup> {
        return dataSource().getAssignmentGroupsWithAssignmentsForGradingPeriod(
            courseId,
            gradingPeriodId,
            true,
            forceRefresh
        )
    }

    override suspend fun getAssignments(
        courseId: Long,
        forceRefresh: Boolean
    ): List<AssignmentGroup> {
        return dataSource().getAssignmentGroupsWithAssignments(courseId, forceRefresh)
    }

    override suspend fun getGradingPeriodsForCourse(
        courseId: Long,
        forceRefresh: Boolean
    ): List<GradingPeriod> {
        return dataSource().getGradingPeriodsForCourse(courseId, forceRefresh)
    }

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): Course {
        return dataSource().getCourseWithGrade(courseId, forceRefresh)
    }

    override suspend fun getSelectedOptions(
        userDomain: String,
        userId: Long,
        contextId: Long,
    ): AssignmentListSelectedFiltersEntity? {
        return assignmentListSelectedFiltersEntityDao.findAssignmentListSelectedFiltersEntity(userDomain, userId, contextId)
    }

    override suspend fun updateSelectedOptions(
        entity: AssignmentListSelectedFiltersEntity
    ) {
        val databaseEntity = assignmentListSelectedFiltersEntityDao.findAssignmentListSelectedFiltersEntity(
            entity.userDomain,
            entity.userId,
            entity.contextId
        )?.copy(
            selectedAssignmentFilters = entity.selectedAssignmentFilters,
            selectedAssignmentStatusFilter = entity.selectedAssignmentStatusFilter,
            selectedGroupByOption = entity.selectedGroupByOption
        ) ?: entity
        assignmentListSelectedFiltersEntityDao.insertOrUpdate(databaseEntity)
    }
}

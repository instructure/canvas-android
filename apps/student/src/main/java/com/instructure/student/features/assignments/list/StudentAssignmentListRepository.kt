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
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterState
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListFilterDao
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListFilterEntity
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
    private val assignmentListFilterDao: AssignmentListFilterDao
) : Repository<AssignmentListDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider), AssignmentListRepository {

    override suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        forceRefresh: Boolean
    ): DataResult<List<AssignmentGroup>> {
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
    ): DataResult<List<AssignmentGroup>> {
        return dataSource().getAssignmentGroupsWithAssignments(courseId, forceRefresh)
    }

    override suspend fun getGradingPeriodsForCourse(
        courseId: Long,
        forceRefresh: Boolean
    ): DataResult<List<GradingPeriod>> {
        return dataSource().getGradingPeriodsForCourse(courseId, forceRefresh)
    }

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        return dataSource().getCourseWithGrade(courseId, forceRefresh)
    }

    override suspend fun getSelectedOptions(
        userDomain: String,
        userId: Long,
        contextId: Long,
        groupId: Int
    ): List<Int>? {
        return assignmentListFilterDao.findAssignmentListFilter(userDomain, userId, contextId, groupId)?.selectedIndexes
    }

    override suspend fun updateSelectedOptions(
        userDomain: String,
        userId: Long,
        contextId: Long,
        state: AssignmentListFilterState
    ) {
        state.filterGroups.forEach { group ->
            if (group.options.any { it is AssignmentListFilterOption.GradingPeriod }) {
                return@forEach
            }
            val entity = assignmentListFilterDao.findAssignmentListFilter(
                userDomain,
                userId,
                contextId,
                group.groupId
            )
            if (entity != null) {
                assignmentListFilterDao.update(
                    entity.copy(selectedIndexes = group.selectedOptionIndexes)
                )
            } else {
                assignmentListFilterDao.insert(
                    entity = AssignmentListFilterEntity(
                        userDomain = userDomain,
                        userId = userId,
                        contextId = contextId,
                        groupId = group.groupId,
                        selectedIndexes = group.selectedOptionIndexes
                    )
                )
            }
        }
    }
}

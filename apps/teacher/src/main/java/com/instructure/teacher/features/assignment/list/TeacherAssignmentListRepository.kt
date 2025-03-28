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
package com.instructure.teacher.features.assignment.list

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.assignments.list.AssignmentListRepository
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListSelectedFiltersEntityDao
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListSelectedFiltersEntity

class TeacherAssignmentListRepository(
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val assignmentListSelectedFiltersEntityDao: AssignmentListSelectedFiltersEntityDao
): AssignmentListRepository {
    override suspend fun getAssignments(
        courseId: Long,
        forceRefresh: Boolean
    ): DataResult<List<AssignmentGroup>> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams).depaginate {
             assignmentApi.getNextPageAssignmentGroupListWithAssignments(it, restParams)
        }
    }

    override suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        forceRefresh: Boolean
    ): DataResult<List<AssignmentGroup>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)

        return assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
            courseId = courseId,
            gradingPeriodId = gradingPeriodId,
            scopeToStudent = false,
            restParams = params
        ).depaginate {
            assignmentApi.getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(it, params)
        }
    }

    override suspend fun getGradingPeriodsForCourse(
        courseId: Long,
        forceRefresh: Boolean
    ): DataResult<List<GradingPeriod>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)

        val gradingPeriods = courseApi.getGradingPeriodsForCourse(courseId, params).dataOrNull?.gradingPeriodList
        if (gradingPeriods != null) {
            return DataResult.Success(gradingPeriods)
        } else {
            return DataResult.Fail()
        }
    }

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        val restParams = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourseWithGrade(courseId, restParams)
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
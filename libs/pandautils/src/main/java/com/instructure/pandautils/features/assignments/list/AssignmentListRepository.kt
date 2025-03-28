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
package com.instructure.pandautils.features.assignments.list

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListSelectedFiltersEntity

interface AssignmentListRepository {
    suspend fun getAssignments(courseId: Long, forceRefresh: Boolean = false): DataResult<List<AssignmentGroup>>

    suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        forceRefresh: Boolean
    ): DataResult<List<AssignmentGroup>>

    suspend fun getGradingPeriodsForCourse(courseId: Long, forceRefresh: Boolean): DataResult<List<GradingPeriod>>

    suspend fun getCourse(courseId: Long, forceRefresh: Boolean = false): DataResult<Course>

    suspend fun getSelectedOptions(userDomain: String, userId: Long, contextId: Long): AssignmentListSelectedFiltersEntity?

    suspend fun updateSelectedOptions(entity: AssignmentListSelectedFiltersEntity)
}
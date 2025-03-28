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

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade

class AssignmentListLocalDataSource(
    private val assignmentFacade: AssignmentFacade,
    private val courseFacade: CourseFacade,
    private val courseSettingsDao: CourseSettingsDao
) : AssignmentListDataSource {

    override suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean
    ): DataResult<List<AssignmentGroup>> {
        return DataResult.Success(assignmentFacade.getAssignmentGroupsWithAssignmentsForGradingPeriod(courseId, gradingPeriodId))
    }

    override suspend fun getAssignmentGroupsWithAssignments(courseId: Long, forceNetwork: Boolean): DataResult<List<AssignmentGroup>> {
        return DataResult.Success(assignmentFacade.getAssignmentGroupsWithAssignments(courseId))
    }

    override suspend fun getGradingPeriodsForCourse(courseId: Long, forceNetwork: Boolean): DataResult<List<GradingPeriod>> {
        return DataResult.Success(courseFacade.getGradingPeriodsByCourseId(courseId))
    }

    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        val course = courseFacade.getCourseById(courseId)
        return if (course != null) {
            DataResult.Success(course)
        } else {
            DataResult.Fail()
        }
    }
}

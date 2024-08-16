/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.grades

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.grades.GradesRepository
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.util.ParentPrefs


class ParentGradesRepository(
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    parentPrefs: ParentPrefs
) : GradesRepository {

    override val studentId = parentPrefs.currentStudent?.id.orDefault()

    override suspend fun loadAssignmentGroups(courseId: Long, forceRefresh: Boolean): List<AssignmentGroup> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)

        return assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, params).depaginate {
            assignmentApi.getNextPageAssignmentGroupListWithAssignments(it, params)
        }.map {
            it.map { group ->
                val filteredAssignments = group.assignments.filter { assignment -> assignment.published }
                group.copy(assignments = filteredAssignments)
            }
        }.dataOrThrow
    }

    override suspend fun loadGradingPeriods(courseId: Long, forceRefresh: Boolean): List<GradingPeriod> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)

        return courseApi.getGradingPeriodsForCourse(courseId, params).dataOrThrow.gradingPeriodList
    }

    override suspend fun loadEnrollments(courseId: Long, gradingPeriodId: Long, forceRefresh: Boolean): List<Enrollment> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)

        return courseApi.getUserEnrollmentsForGradingPeriod(courseId, studentId, gradingPeriodId, params).dataOrThrow
    }
}

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

package com.instructure.pandautils.features.grades

import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.grades.gradepreferences.SortBy


interface GradesRepository {

    val studentId: Long
    suspend fun loadAssignmentGroups(courseId: Long, gradingPeriodId: Long?, forceRefresh: Boolean): List<AssignmentGroup>
    suspend fun loadGradingPeriods(courseId: Long, forceRefresh: Boolean): List<GradingPeriod>
    suspend fun loadEnrollments(courseId: Long, gradingPeriodId: Long?, forceRefresh: Boolean): List<Enrollment>
    suspend fun loadCourse(courseId: Long, forceRefresh: Boolean): Course
    fun getCourseGrade(course: Course, studentId: Long, enrollments: List<Enrollment>, gradingPeriodId: Long?): CourseGrade?
    fun getSortBy(): SortBy?
    fun setSortBy(sortBy: SortBy)
    suspend fun getCustomGradeStatuses(courseId: Long, forceNetwork: Boolean): List<CustomGradeStatusesQuery.Node>
}

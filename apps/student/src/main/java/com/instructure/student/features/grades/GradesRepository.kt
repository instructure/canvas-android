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
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.grades.GradesRepository
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.filterHiddenAssignments
import com.instructure.pandautils.utils.orDefault
import com.instructure.student.features.grades.datasource.GradesListDataSource
import com.instructure.student.features.grades.datasource.GradesListLocalDataSource
import com.instructure.student.features.grades.datasource.GradesListNetworkDataSource

class GradesListRepository(
    localDataSource: GradesListLocalDataSource,
    networkDataSource: GradesListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    private val apiPrefs: ApiPrefs
) : Repository<GradesListDataSource>(
    localDataSource,
    networkDataSource,
    networkStateProvider,
    featureFlagProvider
), GradesRepository {

    override val studentId: Long
        get() = apiPrefs.user?.id.orDefault()

    override suspend fun loadAssignmentGroups(courseId: Long, gradingPeriodId: Long?, forceRefresh: Boolean): List<AssignmentGroup> {
        return gradingPeriodId?.let {
            dataSource().getAssignmentGroupsWithAssignmentsForGradingPeriod(courseId, it, true, forceRefresh)
        } ?: run {
            dataSource().getAssignmentGroupsWithAssignments(courseId, forceRefresh)
        }.filterHiddenAssignments()
    }

    override suspend fun loadGradingPeriods(courseId: Long, forceRefresh: Boolean): List<GradingPeriod> {
        return dataSource().getGradingPeriodsForCourse(courseId, forceRefresh)
    }

    override suspend fun loadEnrollments(courseId: Long, gradingPeriodId: Long?, forceRefresh: Boolean): List<Enrollment> {
        return dataSource().getUserEnrollmentsForGradingPeriod(courseId, studentId, gradingPeriodId, forceRefresh)
    }

    override suspend fun loadCourse(courseId: Long, forceRefresh: Boolean): Course {
        return dataSource().getCourseWithGrade(courseId, forceRefresh)
    }

    override fun getCourseGrade(course: Course, studentId: Long, enrollments: List<Enrollment>, gradingPeriodId: Long?): CourseGrade? {
        val enrollment = enrollments.find { it.userId == studentId }
        return enrollment?.let {
            course.getCourseGradeForGradingPeriodSpecificEnrollment(it)
        } ?: run {
            course.getCourseGrade(true)
        }
    }
}

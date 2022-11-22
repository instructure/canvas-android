/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.edit

import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isNotDeleted
import com.instructure.canvasapi2.utils.isPublished
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRepository
import kotlinx.coroutines.awaitAll

class StudentEditDashboardRepository(
    val courseManager: CourseManager,
    val groupManager: GroupManager
) : EditDashboardRepository {

    override suspend fun getCurses(): List<List<Course>> {
        val (currentCoursesDeferred, pastCoursesDeferred, futureCoursesDeferred) = listOf(
            courseManager.getCoursesByEnrollmentStateAsync("active", true),
            courseManager.getCoursesByEnrollmentStateAsync("completed", true),
            courseManager.getCoursesByEnrollmentStateAsync("invited_or_pending", true)
        ).awaitAll()

        val currentCourses = currentCoursesDeferred.dataOrThrow.filter { it.isStudent }
        val pastCourses = pastCoursesDeferred.dataOrThrow.filter { it.isStudent }
        val futureCourses = futureCoursesDeferred.dataOrThrow.filter { it.isStudent }

        return listOf(currentCourses, pastCourses, futureCourses)
    }

    override suspend fun getGroups(): List<Group> = groupManager.getAllGroupsAsync(true).await().dataOrThrow

    override fun isOpenable(course: Course) = course.isNotDeleted() && course.isPublished()

    override fun isFavoriteable(course: Course) = course.isValidTerm() && course.isNotDeleted() && course.isPublished() && course.hasActiveEnrollment()
}

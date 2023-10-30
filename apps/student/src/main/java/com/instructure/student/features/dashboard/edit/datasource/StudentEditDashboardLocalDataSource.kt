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
package com.instructure.student.features.dashboard.edit.datasource

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.room.offline.daos.EditDashboardItemDao
import com.instructure.pandautils.room.offline.entities.EditDashboardItemEntity
import com.instructure.pandautils.room.offline.entities.EnrollmentState
import com.instructure.pandautils.room.offline.facade.CourseFacade

class StudentEditDashboardLocalDataSource(
    private val courseFacade: CourseFacade,
    private val editDashboardItemDao: EditDashboardItemDao
) : StudentEditDashboardDataSource {

    override suspend fun getCourses(): List<List<Course>> {
        val courseMapper: suspend (EditDashboardItemEntity) -> Course = {
            courseFacade.getCourseById(it.courseId)?.copy(isFavorite = it.isFavorite) ?: it.toCourse()
        }

        val currentCourses = editDashboardItemDao.findByEnrollmentState(EnrollmentState.CURRENT).map { courseMapper(it) }
        val pastCourses = editDashboardItemDao.findByEnrollmentState(EnrollmentState.PAST).map { courseMapper(it) }
        val futureCourses = editDashboardItemDao.findByEnrollmentState(EnrollmentState.FUTURE).map { courseMapper(it) }

        return listOf(currentCourses, pastCourses, futureCourses)
    }

    override suspend fun getGroups(): List<Group> {
        return emptyList()
    }
}
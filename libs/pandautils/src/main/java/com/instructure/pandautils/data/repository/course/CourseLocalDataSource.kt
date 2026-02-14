/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.facade.CourseFacade

class CourseLocalDataSource(
    private val courseFacade: CourseFacade,
    private val dashboardCardDao: DashboardCardDao
) : CourseDataSource {

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        val course = courseFacade.getCourseById(courseId)
        return if (course != null) DataResult.Success(course) else DataResult.Fail()
    }

    override suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val syncedCourses = courseFacade.getAllCourses()
        val syncedCourseIds = syncedCourses.map { it.id }.toSet()

        val dashboardCards = dashboardCardDao.findAll()
        val unsyncedCourses = dashboardCards
            .filter { it.id !in syncedCourseIds }
            .map { card ->
                Course(
                    id = card.id,
                    name = card.shortName ?: card.originalName.orEmpty(),
                    originalName = card.originalName,
                    courseCode = card.courseCode,
                    isFavorite = true
                )
            }

        return DataResult.Success(syncedCourses + unsyncedCourses)
    }

    override suspend fun getFavoriteCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val courses = courseFacade.getAllCourses().filter { it.isFavorite }
        return DataResult.Success(courses)
    }

    override suspend fun getDashboardCards(forceRefresh: Boolean): DataResult<List<DashboardCard>> {
        val cards = dashboardCardDao.findAll().map { it.toApiModel() }
        return DataResult.Success(cards)
    }
}
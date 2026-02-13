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
import com.instructure.pandautils.room.offline.entities.DashboardCardEntity
import com.instructure.pandautils.room.offline.facade.CourseFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseLocalDataSourceTest {

    private val courseFacade: CourseFacade = mockk(relaxed = true)
    private val dashboardCardDao: DashboardCardDao = mockk(relaxed = true)

    private val dataSource = CourseLocalDataSource(courseFacade, dashboardCardDao)

    @Test
    fun `getCourse returns course when found in facade`() = runTest {
        val course = Course(id = 1, name = "Course 1")
        coEvery { courseFacade.getCourseById(1) } returns course

        val result = dataSource.getCourse(1, false)

        assertTrue(result is DataResult.Success)
        assertEquals(course, (result as DataResult.Success).data)
    }

    @Test
    fun `getCourse returns Fail when course not found`() = runTest {
        coEvery { courseFacade.getCourseById(1) } returns null

        val result = dataSource.getCourse(1, false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getCourses returns synced courses from facade`() = runTest {
        val courses = listOf(Course(id = 1, name = "Course 1"), Course(id = 2, name = "Course 2"))
        coEvery { courseFacade.getAllCourses() } returns courses
        coEvery { dashboardCardDao.findAll() } returns courses.map { DashboardCardEntity(DashboardCard(it.id)) }

        val result = dataSource.getCourses(false)

        assertTrue(result is DataResult.Success)
        assertEquals(2, (result as DataResult.Success).data.size)
    }

    @Test
    fun `getCourses includes unsynced courses from dashboard cards`() = runTest {
        val syncedCourse = Course(id = 1, name = "Synced Course", isFavorite = true)
        coEvery { courseFacade.getAllCourses() } returns listOf(syncedCourse)
        coEvery { dashboardCardDao.findAll() } returns listOf(
            DashboardCardEntity(DashboardCard(id = 1, shortName = "Synced Course")),
            DashboardCardEntity(DashboardCard(id = 2, shortName = "Unsynced Course", courseCode = "UC101")),
            DashboardCardEntity(DashboardCard(id = 3, shortName = "Another Unsynced", originalName = "Original Name"))
        )

        val result = dataSource.getCourses(false)

        assertTrue(result is DataResult.Success)
        val data = (result as DataResult.Success).data
        assertEquals(3, data.size)
        assertEquals(syncedCourse, data[0])
        assertEquals(2L, data[1].id)
        assertEquals("Unsynced Course", data[1].name)
        assertEquals("UC101", data[1].courseCode)
        assertTrue(data[1].isFavorite)
        assertEquals(3L, data[2].id)
        assertEquals("Another Unsynced", data[2].name)
        assertEquals("Original Name", data[2].originalName)
    }

    @Test
    fun `getCourses uses originalName when shortName is null for unsynced courses`() = runTest {
        coEvery { courseFacade.getAllCourses() } returns emptyList()
        coEvery { dashboardCardDao.findAll() } returns listOf(
            DashboardCardEntity(DashboardCard(id = 1, shortName = null, originalName = "Original Name"))
        )

        val result = dataSource.getCourses(false)

        assertTrue(result is DataResult.Success)
        val data = (result as DataResult.Success).data
        assertEquals("Original Name", data[0].name)
    }

    @Test
    fun `getCourses returns empty when no synced courses and no dashboard cards`() = runTest {
        coEvery { courseFacade.getAllCourses() } returns emptyList()
        coEvery { dashboardCardDao.findAll() } returns emptyList()

        val result = dataSource.getCourses(false)

        assertTrue(result is DataResult.Success)
        assertEquals(emptyList<Course>(), (result as DataResult.Success).data)
    }

    @Test
    fun `getFavoriteCourses returns only favorite courses from facade`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Fav", isFavorite = true),
            Course(id = 2, name = "Not Fav", isFavorite = false)
        )
        coEvery { courseFacade.getAllCourses() } returns courses

        val result = dataSource.getFavoriteCourses(false)

        assertTrue(result is DataResult.Success)
        val data = (result as DataResult.Success).data
        assertEquals(1, data.size)
        assertEquals(1L, data[0].id)
    }

    @Test
    fun `getDashboardCards returns cards from dao`() = runTest {
        val entities = listOf(
            DashboardCardEntity(DashboardCard(id = 1, position = 0)),
            DashboardCardEntity(DashboardCard(id = 2, position = 1))
        )
        coEvery { dashboardCardDao.findAll() } returns entities

        val result = dataSource.getDashboardCards(false)

        assertTrue(result is DataResult.Success)
        val data = (result as DataResult.Success).data
        assertEquals(2, data.size)
        assertEquals(1L, data[0].id)
        assertEquals(2L, data[1].id)
    }
}
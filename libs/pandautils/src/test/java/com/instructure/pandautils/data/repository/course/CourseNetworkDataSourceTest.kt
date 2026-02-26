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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val dataSource = CourseNetworkDataSource(courseApi)

    @Test
    fun `getCourse returns course from api`() = runTest {
        val course = Course(id = 1, name = "Course 1")
        coEvery { courseApi.getCourse(1, any()) } returns DataResult.Success(course)

        val result = dataSource.getCourse(1, false)

        assertTrue(result is DataResult.Success)
        assertEquals(course, (result as DataResult.Success).data)
    }

    @Test
    fun `getCourse returns Fail when api fails`() = runTest {
        coEvery { courseApi.getCourse(1, any()) } returns DataResult.Fail()

        val result = dataSource.getCourse(1, false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getCourses returns courses from api`() = runTest {
        val courses = listOf(Course(id = 1), Course(id = 2))
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)

        val result = dataSource.getCourses(false)

        assertTrue(result is DataResult.Success)
        assertEquals(courses, (result as DataResult.Success).data)
    }

    @Test
    fun `getFavoriteCourses returns favorites from api`() = runTest {
        val courses = listOf(Course(id = 1, isFavorite = true))
        coEvery { courseApi.getFavoriteCourses(any()) } returns DataResult.Success(courses)

        val result = dataSource.getFavoriteCourses(false)

        assertTrue(result is DataResult.Success)
        assertEquals(courses, (result as DataResult.Success).data)
    }

    @Test
    fun `getDashboardCards returns cards from api`() = runTest {
        val cards = listOf(DashboardCard(id = 1), DashboardCard(id = 2))
        coEvery { courseApi.getDashboardCourses(any()) } returns DataResult.Success(cards)

        val result = dataSource.getDashboardCards(false)

        assertTrue(result is DataResult.Success)
        assertEquals(cards, (result as DataResult.Success).data)
    }

    @Test
    fun `getDashboardCards returns Fail when api fails`() = runTest {
        coEvery { courseApi.getDashboardCourses(any()) } returns DataResult.Fail()

        val result = dataSource.getDashboardCards(false)

        assertTrue(result is DataResult.Fail)
    }
}
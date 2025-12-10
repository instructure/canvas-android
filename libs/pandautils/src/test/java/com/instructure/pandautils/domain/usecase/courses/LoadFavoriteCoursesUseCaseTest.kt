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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.course.CourseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadFavoriteCoursesUseCaseTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private lateinit var useCase: LoadFavoriteCoursesUseCase

    @Before
    fun setup() {
        useCase = LoadFavoriteCoursesUseCase(courseRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns only favorite courses`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Favorite Course", isFavorite = true),
            Course(id = 2, name = "Non-Favorite Course", isFavorite = false),
            Course(id = 3, name = "Another Favorite", isFavorite = true)
        )
        val dashboardCards = listOf(
            DashboardCard(id = 1, position = 0),
            DashboardCard(id = 3, position = 1)
        )

        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { courseRepository.getDashboardCards(any()) } returns DataResult.Success(dashboardCards)

        val result = useCase(LoadFavoriteCoursesParams())

        assertEquals(2, result.size)
        assertTrue(result.all { it.isFavorite })
        assertEquals(1L, result[0].id)
        assertEquals(3L, result[1].id)
    }

    @Test
    fun `execute sorts courses by dashboard card position`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course A", isFavorite = true),
            Course(id = 2, name = "Course B", isFavorite = true),
            Course(id = 3, name = "Course C", isFavorite = true)
        )
        val dashboardCards = listOf(
            DashboardCard(id = 1, position = 2),
            DashboardCard(id = 2, position = 0),
            DashboardCard(id = 3, position = 1)
        )

        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { courseRepository.getDashboardCards(any()) } returns DataResult.Success(dashboardCards)

        val result = useCase(LoadFavoriteCoursesParams())

        assertEquals(3, result.size)
        assertEquals(2L, result[0].id)
        assertEquals(3L, result[1].id)
        assertEquals(1L, result[2].id)
    }

    @Test
    fun `execute excludes courses without dashboard card`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course A", isFavorite = true),
            Course(id = 2, name = "Course B", isFavorite = true),
            Course(id = 3, name = "Course C", isFavorite = true)
        )
        val dashboardCards = listOf(
            DashboardCard(id = 2, position = 0)
        )

        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { courseRepository.getDashboardCards(any()) } returns DataResult.Success(dashboardCards)

        val result = useCase(LoadFavoriteCoursesParams())

        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
    }

    @Test
    fun `execute returns empty list when no favorite courses exist`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course A", isFavorite = false),
            Course(id = 2, name = "Course B", isFavorite = false)
        )

        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { courseRepository.getDashboardCards(any()) } returns DataResult.Success(emptyList())

        val result = useCase(LoadFavoriteCoursesParams())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute passes forceRefresh parameter to repository`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { courseRepository.getDashboardCards(any()) } returns DataResult.Success(emptyList())

        useCase(LoadFavoriteCoursesParams(forceRefresh = true))

        coVerify { courseRepository.getCourses(true) }
        coVerify { courseRepository.getDashboardCards(true) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws when repository returns failure for courses`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Fail()
        coEvery { courseRepository.getDashboardCards(any()) } returns DataResult.Success(emptyList())

        useCase(LoadFavoriteCoursesParams())
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws when repository returns failure for dashboard cards`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { courseRepository.getDashboardCards(any()) } returns DataResult.Fail()

        useCase(LoadFavoriteCoursesParams())
    }
}
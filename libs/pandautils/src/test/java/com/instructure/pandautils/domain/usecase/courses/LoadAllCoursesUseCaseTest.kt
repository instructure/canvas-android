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

class LoadAllCoursesUseCaseTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private lateinit var useCase: LoadAllCoursesUseCase

    @Before
    fun setup() {
        useCase = LoadAllCoursesUseCase(courseRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns all courses`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course A"),
            Course(id = 2, name = "Course B"),
            Course(id = 3, name = "Course C")
        )

        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(courses)

        val result = useCase(LoadAllCoursesUseCase.Params())

        assertEquals(3, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        assertEquals(3L, result[2].id)
    }

    @Test
    fun `execute returns empty list when repository returns empty list`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(emptyList())

        val result = useCase(LoadAllCoursesUseCase.Params())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute passes forceRefresh parameter to repository`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(emptyList())

        useCase(LoadAllCoursesUseCase.Params(forceRefresh = true))

        coVerify { courseRepository.getCourses(true) }
    }

    @Test
    fun `execute passes false forceRefresh by default`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(emptyList())

        useCase(LoadAllCoursesUseCase.Params())

        coVerify { courseRepository.getCourses(false) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws when repository returns failure`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Fail()

        useCase(LoadAllCoursesUseCase.Params())
    }

    @Test
    fun `execute returns all courses from repository`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course A"),
            Course(id = 2, name = "Course B"),
            Course(id = 3, name = "Course C")
        )

        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(courses)

        val result = useCase(LoadAllCoursesUseCase.Params())

        assertEquals(3, result.size)
        assertEquals(courses, result)
    }
}
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
import org.junit.Before
import org.junit.Test

class LoadCourseUseCaseTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private lateinit var useCase: LoadCourseUseCase

    @Before
    fun setup() {
        useCase = LoadCourseUseCase(courseRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns course successfully`() = runTest {
        val course = Course(id = 123L, name = "Test Course")
        val expected = DataResult.Success(course)
        coEvery { courseRepository.getCourse(any(), any()) } returns expected

        val result = useCase(LoadCourseUseCaseParams(courseId = 123L))

        assertEquals(course, result)
        coVerify { courseRepository.getCourse(123L, false) }
    }

    @Test
    fun `execute with forceNetwork passes correct params`() = runTest {
        val course = Course(id = 456L, name = "Another Course")
        val expected = DataResult.Success(course)
        coEvery { courseRepository.getCourse(any(), any()) } returns expected

        val result = useCase(LoadCourseUseCaseParams(courseId = 456L, forceNetwork = true))

        assertEquals(course, result)
        coVerify { courseRepository.getCourse(456L, true) }
    }

    @Test(expected = Exception::class)
    fun `execute throws exception on failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery { courseRepository.getCourse(any(), any()) } returns expected

        useCase(LoadCourseUseCaseParams(courseId = 123L))
    }
}

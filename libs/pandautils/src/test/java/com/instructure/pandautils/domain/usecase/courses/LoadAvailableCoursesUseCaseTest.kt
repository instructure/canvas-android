/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
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

class LoadAvailableCoursesUseCaseTest {

    private val courseRepository: CourseRepository = mockk()
    private lateinit var useCase: LoadAvailableCoursesUseCase

    @Before
    fun setup() {
        useCase = LoadAvailableCoursesUseCase(courseRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns available courses excluding access restricted`() = runTest {
        val courses = listOf(
            createCourse(id = 1L, accessRestrictedByDate = false),
            createCourse(id = 2L, accessRestrictedByDate = true),
            createCourse(id = 3L, accessRestrictedByDate = false)
        )
        coEvery { courseRepository.getCourses(false) } returns DataResult.Success(courses)

        val params = LoadAvailableCoursesUseCase.Params(forceRefresh = false)
        val result = useCase(params)

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(3L, result[1].id)
        coVerify { courseRepository.getCourses(false) }
    }

    @Test
    fun `execute returns available courses excluding invited`() = runTest {
        val courses = listOf(
            createCourse(id = 1L, accessRestrictedByDate = false, enrollmentState = EnrollmentAPI.STATE_ACTIVE),
            createCourse(id = 2L, accessRestrictedByDate = false, enrollmentState = EnrollmentAPI.STATE_INVITED),
            createCourse(id = 3L, accessRestrictedByDate = false, enrollmentState = EnrollmentAPI.STATE_ACTIVE)
        )
        coEvery { courseRepository.getCourses(false) } returns DataResult.Success(courses)

        val params = LoadAvailableCoursesUseCase.Params(forceRefresh = false)
        val result = useCase(params)

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(3L, result[1].id)
        coVerify { courseRepository.getCourses(false) }
    }

    @Test
    fun `execute excludes both access restricted and invited courses`() = runTest {
        val courses = listOf(
            createCourse(id = 1L, accessRestrictedByDate = false, enrollmentState = EnrollmentAPI.STATE_ACTIVE),
            createCourse(id = 2L, accessRestrictedByDate = true, enrollmentState = EnrollmentAPI.STATE_ACTIVE),
            createCourse(id = 3L, accessRestrictedByDate = false, enrollmentState = EnrollmentAPI.STATE_INVITED),
            createCourse(id = 4L, accessRestrictedByDate = true, enrollmentState = EnrollmentAPI.STATE_INVITED),
            createCourse(id = 5L, accessRestrictedByDate = false, enrollmentState = EnrollmentAPI.STATE_ACTIVE)
        )
        coEvery { courseRepository.getCourses(false) } returns DataResult.Success(courses)

        val params = LoadAvailableCoursesUseCase.Params(forceRefresh = false)
        val result = useCase(params)

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(5L, result[1].id)
        coVerify { courseRepository.getCourses(false) }
    }

    @Test
    fun `execute returns empty list when all courses are filtered out`() = runTest {
        val courses = listOf(
            createCourse(id = 1L, accessRestrictedByDate = true, enrollmentState = EnrollmentAPI.STATE_ACTIVE),
            createCourse(id = 2L, accessRestrictedByDate = false, enrollmentState = EnrollmentAPI.STATE_INVITED),
            createCourse(id = 3L, accessRestrictedByDate = true, enrollmentState = EnrollmentAPI.STATE_INVITED)
        )
        coEvery { courseRepository.getCourses(false) } returns DataResult.Success(courses)

        val params = LoadAvailableCoursesUseCase.Params(forceRefresh = false)
        val result = useCase(params)

        assertEquals(0, result.size)
        coVerify { courseRepository.getCourses(false) }
    }

    @Test
    fun `execute returns empty list when repository returns empty list`() = runTest {
        coEvery { courseRepository.getCourses(false) } returns DataResult.Success(emptyList())

        val params = LoadAvailableCoursesUseCase.Params(forceRefresh = false)
        val result = useCase(params)

        assertEquals(0, result.size)
        coVerify { courseRepository.getCourses(false) }
    }

    @Test
    fun `execute passes forceRefresh parameter to repository`() = runTest {
        val courses = listOf(
            createCourse(id = 1L, accessRestrictedByDate = false)
        )
        coEvery { courseRepository.getCourses(true) } returns DataResult.Success(courses)

        val params = LoadAvailableCoursesUseCase.Params(forceRefresh = true)
        val result = useCase(params)

        assertEquals(1, result.size)
        coVerify { courseRepository.getCourses(true) }
    }

    @Test
    fun `execute returns all courses when none are restricted or invited`() = runTest {
        val courses = listOf(
            createCourse(id = 1L, accessRestrictedByDate = false),
            createCourse(id = 2L, accessRestrictedByDate = false),
            createCourse(id = 3L, accessRestrictedByDate = false)
        )
        coEvery { courseRepository.getCourses(false) } returns DataResult.Success(courses)

        val params = LoadAvailableCoursesUseCase.Params(forceRefresh = false)
        val result = useCase(params)

        assertEquals(3, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        assertEquals(3L, result[2].id)
        coVerify { courseRepository.getCourses(false) }
    }

    private fun createCourse(
        id: Long,
        accessRestrictedByDate: Boolean = false,
        enrollmentState: String = EnrollmentAPI.STATE_ACTIVE
    ): Course {
        val enrollment = Enrollment(
            enrollmentState = enrollmentState
        )
        return Course(
            id = id,
            accessRestrictedByDate = accessRestrictedByDate,
            enrollments = mutableListOf(enrollment)
        )
    }
}
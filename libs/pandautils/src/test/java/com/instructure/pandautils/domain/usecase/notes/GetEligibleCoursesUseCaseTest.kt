/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.domain.usecase.notes

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
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

class GetEligibleCoursesUseCaseTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private lateinit var useCase: GetEligibleCoursesUseCase

    @Before
    fun setup() {
        useCase = GetEligibleCoursesUseCase(courseRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns only courses the user is a student in`() = runTest {
        val studentCourse = Course(id = 1L, name = "Course 1", enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        val teacherCourse = Course(id = 2L, name = "Course 2", enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Teacher)))
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(listOf(studentCourse, teacherCourse))

        val result = useCase(Unit)

        assertEquals(listOf(studentCourse), result)
    }

    @Test
    fun `execute always reads cache-first from the repository`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Success(emptyList())

        useCase(Unit)

        coVerify { courseRepository.getCourses(forceRefresh = false) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws when the repository fails`() = runTest {
        coEvery { courseRepository.getCourses(any()) } returns DataResult.Fail(Failure.Network())

        useCase(Unit)
    }
}

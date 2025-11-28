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
 *
 */
package com.instructure.pandautils.domain.usecase.enrollment

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoadCourseInvitationsUseCaseTest {

    private val enrollmentRepository: EnrollmentRepository = mockk(relaxed = true)
    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private lateinit var useCase: LoadCourseInvitationsUseCase

    @Before
    fun setup() {
        useCase = LoadCourseInvitationsUseCase(enrollmentRepository, courseRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns course invitations successfully`() = runTest {
        val enrollments = listOf(
            Enrollment(id = 1L, courseId = 100L, userId = 10L),
            Enrollment(id = 2L, courseId = 200L, userId = 10L)
        )
        val course1 = Course(id = 100L, name = "Course 1")
        val course2 = Course(id = 200L, name = "Course 2")

        coEvery {
            enrollmentRepository.getSelfEnrollments(null, listOf(EnrollmentAPI.STATE_INVITED), false)
        } returns DataResult.Success(enrollments)
        coEvery { courseRepository.getCourse(100L, false) } returns DataResult.Success(course1)
        coEvery { courseRepository.getCourse(200L, false) } returns DataResult.Success(course2)

        val result = useCase(LoadCourseInvitationsParams(forceRefresh = false))

        assertEquals(2, result.size)
        assertEquals(1L, result[0].enrollmentId)
        assertEquals(100L, result[0].courseId)
        assertEquals("Course 1", result[0].courseName)
        assertEquals(10L, result[0].userId)
        assertEquals(2L, result[1].enrollmentId)
        assertEquals(200L, result[1].courseId)
        assertEquals("Course 2", result[1].courseName)
        assertEquals(10L, result[1].userId)
    }

    @Test
    fun `execute with forceRefresh true passes correct params`() = runTest {
        val enrollments = listOf(
            Enrollment(id = 1L, courseId = 100L, userId = 10L)
        )
        val course = Course(id = 100L, name = "Course 1")

        coEvery {
            enrollmentRepository.getSelfEnrollments(null, listOf(EnrollmentAPI.STATE_INVITED), true)
        } returns DataResult.Success(enrollments)
        coEvery { courseRepository.getCourse(100L, true) } returns DataResult.Success(course)

        val result = useCase(LoadCourseInvitationsParams(forceRefresh = true))

        assertEquals(1, result.size)
    }

    @Test
    fun `execute returns empty list when no enrollments`() = runTest {
        coEvery {
            enrollmentRepository.getSelfEnrollments(null, listOf(EnrollmentAPI.STATE_INVITED), false)
        } returns DataResult.Success(emptyList())

        val result = useCase(LoadCourseInvitationsParams(forceRefresh = false))

        assertEquals(0, result.size)
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when enrollment repository fails`() = runTest {
        coEvery {
            enrollmentRepository.getSelfEnrollments(any(), any(), any())
        } returns DataResult.Fail()

        useCase(LoadCourseInvitationsParams(forceRefresh = false))
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when course repository fails`() = runTest {
        val enrollments = listOf(
            Enrollment(id = 1L, courseId = 100L, userId = 10L)
        )

        coEvery {
            enrollmentRepository.getSelfEnrollments(null, listOf(EnrollmentAPI.STATE_INVITED), false)
        } returns DataResult.Success(enrollments)
        coEvery { courseRepository.getCourse(100L, false) } returns DataResult.Fail()

        useCase(LoadCourseInvitationsParams(forceRefresh = false))
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when any course repository fails`() = runTest {
        val enrollments = listOf(
            Enrollment(id = 1L, courseId = 100L, userId = 10L),
            Enrollment(id = 2L, courseId = 200L, userId = 10L)
        )
        val course1 = Course(id = 100L, name = "Course 1")

        coEvery {
            enrollmentRepository.getSelfEnrollments(null, listOf(EnrollmentAPI.STATE_INVITED), false)
        } returns DataResult.Success(enrollments)
        coEvery { courseRepository.getCourse(100L, false) } returns DataResult.Success(course1)
        coEvery { courseRepository.getCourse(200L, false) } returns DataResult.Fail()

        useCase(LoadCourseInvitationsParams(forceRefresh = false))
    }
}
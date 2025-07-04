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
package com.instructure.student.widget.grades

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GradesWidgetRepositoryTest {
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private lateinit var repository: GradesWidgetRepository

    @Before
    fun setup() {
        repository = GradesWidgetRepository(coursesApi)
    }

    @Test
    fun `returns only favorite, current, non-invited courses`() = runTest {
        val favoriteCourse = Course(id = 1L, isFavorite = true, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)))
        val nonFavoriteCourse = Course(id = 2L, isFavorite = false, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)))
        val invitedFavoriteCourse = Course(id = 3L, isFavorite = true, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_INVITED)))
        val invitedNotFavoriteCourse = Course(id = 4L, isFavorite = false, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_INVITED)))
        coEvery { coursesApi.getFirstPageCoursesWithGradingScheme(any()) } returns DataResult.Success(listOf(favoriteCourse, nonFavoriteCourse, invitedFavoriteCourse, invitedNotFavoriteCourse))
        coEvery { coursesApi.next(any(), any()) } returns DataResult.Success(emptyList())

        val result = repository.getCoursesWithGradingScheme()
        assertEquals(DataResult.Success(listOf(favoriteCourse)), result)
    }

    @Test
    fun `returns all valid courses if no favorites`() = runTest {
        val activeCourse = Course(id = 1L, isFavorite = false, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)))
        val invitedCourse = Course(id = 2L, isFavorite = false, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_INVITED)))
        coEvery { coursesApi.getFirstPageCoursesWithGradingScheme(any()) } returns DataResult.Success(listOf(activeCourse, invitedCourse))
        coEvery { coursesApi.next(any(), any()) } returns DataResult.Success(emptyList())

        val result = repository.getCoursesWithGradingScheme()
        assertEquals(DataResult.Success(listOf(activeCourse)), result)
    }

    @Test
    fun `returns empty list if no valid courses`() = runTest {
        val course1 = Course(id = 1L, isFavorite = false, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_INVITED)))
        val course2 = Course(id = 2L, isFavorite = false, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_INVITED)))
        coEvery { coursesApi.getFirstPageCoursesWithGradingScheme(any()) } returns DataResult.Success(listOf(course1, course2))
        coEvery { coursesApi.next(any(), any()) } returns DataResult.Success(emptyList())

        val result = repository.getCoursesWithGradingScheme()
        assertEquals(DataResult.Success(emptyList<Course>()), result)
    }

    @Test
    fun `returns failed result if api call fails`() = runTest {
        coEvery { coursesApi.getFirstPageCoursesWithGradingScheme(any()) } returns DataResult.Fail()
        coEvery { coursesApi.next(any(), any()) } returns DataResult.Fail()
        val result = repository.getCoursesWithGradingScheme()
        assertEquals(DataResult.Fail(), result)
    }
}

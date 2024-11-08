/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.features.inbox.coursepicker

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Term
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import java.util.Date

class ParentInboxCoursePickerRepositoryTest {
    private val courseAPI: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val enrollmentAPI: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)
    private val repository = ParentInboxCoursePickerRepository(courseAPI, enrollmentAPI)

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test get Courses successfully`() = runTest {
        val expectedCourses = listOf(
            Course(id = 1L, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)), term = Term(endAt = Date(Date().time + 10000).toString())),
            Course(id = 2L, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)), term = Term(endAt = Date(Date().time + 10000).toString())),
        )
        coEvery { courseAPI.getCoursesByEnrollmentType(any(), any()) } returns DataResult.Success(expectedCourses)

        val result = repository.getCourses()

        assertEquals(expectedCourses, result.dataOrNull)
    }

    @Test
    fun `Test get Courses successfully with pagination`() = runTest {
        val expectedCourses = listOf(
            Course(id = 1L, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)), term = Term(endAt = Date(Date().time + 10000).toString())),
            Course(id = 2L, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)), term = Term(endAt = Date(Date().time + 10000).toString())),
            Course(id = 3L, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)), term = Term(endAt = Date(Date().time + 10000).toString())),
        )
        coEvery { courseAPI.getCoursesByEnrollmentType(any(), any()) } returns DataResult.Success(expectedCourses.take(2), LinkHeaders(nextUrl = "nextUrl"))
        coEvery { courseAPI.next(any(), any()) } returns DataResult.Success(expectedCourses.takeLast(1))

        val result = repository.getCourses()

        assertEquals(expectedCourses, result.dataOrNull)
    }

    @Test
    fun `Test get Courses successfully with filtering`() = runTest {
        val expectedCourses = listOf(
            Course(id = 1L, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)), term = Term(endAt = Date(Date().time + 10000).toString())),
            Course(id = 2L, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
            Course(id = 3L, term = Term(endAt = Date(Date().time + 10000).toString())),
            Course(id = 5L),
        )
        coEvery { courseAPI.getCoursesByEnrollmentType(any(), any()) } returns DataResult.Success(expectedCourses)

        val result = repository.getCourses()

        assertEquals(expectedCourses.take(2), result.dataOrNull)
    }

    @Test
    fun `Test get Courses failed`() = runTest {
        coEvery { courseAPI.getCoursesByEnrollmentType(any(), any()) } returns DataResult.Fail()

        val result = repository.getCourses()

        assertEquals(result, DataResult.Fail())
    }

    @Test
    fun `Test get Enrollments successfully`() = runTest {
        val expectedEnrollments = listOf(
            Enrollment(id = 1L),
            Enrollment(id = 2L),
        )
        coEvery { enrollmentAPI.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Success(expectedEnrollments)

        val result = repository.getEnrollments()

        assertEquals(expectedEnrollments, result.dataOrNull)
    }

    @Test
    fun `Test get Enrollments successfully with pagination`() = runTest {
        val expectedEnrollments = listOf(
            Enrollment(id = 1L),
            Enrollment(id = 2L),
            Enrollment(id = 3L),
        )
        coEvery { enrollmentAPI.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Success(expectedEnrollments.take(2), LinkHeaders(nextUrl = "nextUrl"))
        coEvery { enrollmentAPI.getNextPage(any(), any()) } returns DataResult.Success(expectedEnrollments.takeLast(1))

        val result = repository.getEnrollments()

        assertEquals(expectedEnrollments, result.dataOrNull)
    }

    @Test
    fun `Test get Enrollments failed`() = runTest {
        coEvery { enrollmentAPI.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Fail()

        val result = repository.getEnrollments()

        assertEquals(result, DataResult.Fail())
    }
}
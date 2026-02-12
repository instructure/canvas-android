/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.course.details.score

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CourseScoreRepositoryTest {
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val testUser = User(id = 999L)
    private val testAssignmentGroups = listOf(
        AssignmentGroup(
            id = 1L,
            name = "Homework",
            groupWeight = 40.0,
            assignments = listOf(
                Assignment(id = 101L, name = "Assignment 1"),
                Assignment(id = 102L, name = "Assignment 2")
            )
        ),
        AssignmentGroup(
            id = 2L,
            name = "Exams",
            groupWeight = 60.0,
            assignments = listOf(
                Assignment(id = 201L, name = "Midterm Exam")
            )
        )
    )
    private val testEnrollments = listOf(
        Enrollment(
            id = 1L,
            enrollmentState = EnrollmentAPI.STATE_ACTIVE,
            grades = Grades(currentScore = 85.5)
        )
    )

    @Before
    fun setup() {
        every { apiPrefs.user } returns testUser
        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Success(
            testAssignmentGroups,
            linkHeaders = LinkHeaders()
        )
        coEvery { enrollmentApi.getEnrollmentsForUserInCourse(any(), any(), any()) } returns DataResult.Success(
            testEnrollments,
            linkHeaders = LinkHeaders()
        )
    }

    @Test
    fun `getAssignmentGroups returns list of assignment groups with assignments`() = runTest {
        val repository = getRepository()
        val result = repository.getAssignmentGroups(1L, false)

        assertEquals(2, result.size)
        assertEquals("Homework", result[0].name)
        assertEquals(2, result[0].assignments.size)
        assertEquals("Exams", result[1].name)
        assertEquals(1, result[1].assignments.size)
        coVerify { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(1L, any()) }
    }

    @Test
    fun `getAssignmentGroups with forceRefresh true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getAssignmentGroups(1L, true)

        coVerify {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(
                any(),
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getEnrollments returns list of enrollments`() = runTest {
        val repository = getRepository()
        val result = repository.getEnrollments(1L, false)

        assertEquals(1, result.size)
        assertEquals(EnrollmentAPI.STATE_ACTIVE, result[0].enrollmentState)
        assertEquals(85.5, result[0].grades?.currentScore)
        coVerify { enrollmentApi.getEnrollmentsForUserInCourse(1L, 999L, any()) }
    }

    @Test
    fun `getEnrollments with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getEnrollments(1L, true)

        coVerify {
            enrollmentApi.getEnrollmentsForUserInCourse(
                any(),
                any(),
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getEnrollments uses -1 when user is null`() = runTest {
        every { apiPrefs.user } returns null
        val repository = getRepository()
        repository.getEnrollments(1L, false)

        coVerify { enrollmentApi.getEnrollmentsForUserInCourse(1L, -1L, any()) }
    }

    @Test
    fun `getAssignmentGroups returns empty list when no groups`() = runTest {
        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Success(
            emptyList(),
            linkHeaders = LinkHeaders()
        )
        val repository = getRepository()
        val result = repository.getAssignmentGroups(1L, false)

        assertEquals(0, result.size)
    }

    private fun getRepository(): CourseScoreRepository {
        return CourseScoreRepository(assignmentApi, enrollmentApi, apiPrefs)
    }
}

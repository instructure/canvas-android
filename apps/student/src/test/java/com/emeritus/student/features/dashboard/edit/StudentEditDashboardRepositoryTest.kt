/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.emeritus.student.features.dashboard.edit

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class StudentEditDashboardRepositoryTest {

    private val courseManager: CourseManager = mockk(relaxed = true)
    private val groupManager: GroupManager = mockk(relaxed = true)
    private val repository = StudentEditDashboardRepository(courseManager, groupManager)

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.AwaitKt")
    }

    @Test
    fun `Returns courses when fetching courses`() = runBlockingTest {
        // Given
        val coursesActive = listOf(Course(id = 1L, name = "Course"))
        val coursesCompleted = listOf(Course(id = 2L, name = "Course"))
        val coursesInvitedOrPending = listOf(Course(id = 3L, name = "Course"))

        val coursesDeferred: Deferred<DataResult<List<Course>>> = mockk()
        every { courseManager.getCoursesByEnrollmentStateAsync(any(), any()) } returns coursesDeferred
        coEvery { listOf(coursesDeferred, coursesDeferred, coursesDeferred).awaitAll() } returns listOf(
            DataResult.Success(coursesActive),
            DataResult.Success(coursesCompleted),
            DataResult.Success(coursesInvitedOrPending)
        )

        // When
        val result = repository.getCurses()

        // Then
        val expected = listOf(coursesActive, coursesCompleted, coursesInvitedOrPending)
        assertEquals(expected, result)
    }

    @Test
    fun `Returns groups when fetching groups`() = runBlockingTest {
        // Given
        val groups = listOf(Group(id = 1L, name = "Group1"))
        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        // When
        val result = repository.getGroups()

        // Then
        assertEquals(groups, result)
    }

    @Test
    fun `Returns isOpenable true when course is not deleted and is published`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.AVAILABLE)

        // When
        val result = repository.isOpenable(course)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Returns isOpenable false when course is deleted`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.DELETED)

        // When
        val result = repository.isOpenable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isOpenable false when course is not published`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.UNPUBLISHED)

        // When
        val result = repository.isOpenable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isFavoriteable true when course is validTerm and not deleted and published and has active enrollment`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.AVAILABLE,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Returns isFavoriteable false when course is deleted`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.DELETED,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isFavoriteable false when course is not published`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.UNPUBLISHED,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isFavoriteable false when course doesn't have active enrollment`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.AVAILABLE,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_DELETED))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertFalse(result)
    }
}
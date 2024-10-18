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

package com.instructure.teacher.features.dashboard.edit

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TeacherEditDashboardRepositoryTest {

    private val courseManager: CourseManager = mockk(relaxed = true)
    private val repository = TeacherEditDashboardRepository(courseManager)

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.AwaitKt")
    }

    @Test
    fun `Returns courses when fetching courses`() = runTest {
        // Given
        val courseActive = Course(
            id = 1L,
            name = "Course",
            enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Teacher, enrollmentState = EnrollmentAPI.STATE_ACTIVE)),
            startAt = "2022-01-01T08:00:00Z",
            endAt = "2100-01-01T07:00:00Z",
            restrictEnrollmentsToCourseDate = true
        )
        val courseCompleted = Course(
            id = 2L,
            name = "Course",
            workflowState = Course.WorkflowState.COMPLETED,
            enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Teacher, enrollmentState = EnrollmentAPI.STATE_ACTIVE))
        )
        val courseInvitedOrPending = Course(
            id = 3L, name = "Course",
            enrollments = mutableListOf(
                Enrollment(type = Enrollment.EnrollmentType.Teacher, enrollmentState = EnrollmentAPI.STATE_ACTIVE),
                Enrollment(type = Enrollment.EnrollmentType.Teacher, enrollmentState = EnrollmentAPI.STATE_CREATION_PENDING)
            ),
            startAt = "2100-01-01T08:00:00Z",
            restrictEnrollmentsToCourseDate = true
        )

        every { courseManager.getCoursesTeacherAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                listOf(courseActive, courseCompleted, courseInvitedOrPending)
            )
        }

        // When
        val result = repository.getCourses()

        // Then
        val expected = listOf(listOf(courseActive), listOf(courseCompleted), listOf(courseInvitedOrPending))
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Returns isOpenable true when course is not deleted and is published`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.AVAILABLE)

        // When
        val result = repository.isOpenable(course)

        // Then
        Assert.assertTrue(result)
    }

    @Test
    fun `Returns isOpenable false when course is deleted`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.DELETED)

        // When
        val result = repository.isOpenable(course)

        // Then
        Assert.assertFalse(result)
    }

    @Test
    fun `Returns isFavoriteable true when course is validTerm and not deleted and published and has active enrollment`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.AVAILABLE
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        Assert.assertTrue(result)
    }

    @Test
    fun `Returns isFavoriteable false when course is deleted`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.DELETED
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        Assert.assertFalse(result)
    }

    @Test
    fun `Synced course ids always returns empty set`() = runTest {
        // When
        val result = repository.getSyncedCourseIds()

        // Then
        Assert.assertEquals(emptySet<Long>(), result)
    }

    @Test
    fun `Offline enabled always returns false`() = runTest {
        // When
        val result = repository.offlineEnabled()

        // Then
        Assert.assertFalse(result)
    }
}
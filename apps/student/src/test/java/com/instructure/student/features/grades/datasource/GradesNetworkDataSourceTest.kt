/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.grades.datasource

import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.GradingPeriodResponse
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GradesNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)
    private val customGradeStatusesManager: CustomGradeStatusesManager = mockk(relaxed = true)

    private val dataSource = GradesNetworkDataSource(courseApi, assignmentApi, submissionApi, customGradeStatusesManager)

    @Test
    fun `Get course with grade successfully returns data`() = runTest {
        val expected = Course(1)

        coEvery { courseApi.getCourseWithGrade(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getCourseWithGrade(1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course with grade failure throws exception`() = runTest {
        coEvery { courseApi.getCourseWithGrade(any(), any()) } returns DataResult.Fail()

        dataSource.getCourseWithGrade(1, true)
    }

    @Test
    fun `Get assignment groups with assignments for grading period successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(expected)

        val result = dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(
            courseId = 1,
            gradingPeriodId = 1,
            scopeToStudent = true,
            forceNetwork = true
        )

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment groups with assignments for grading period failure throws exception`() = runTest {
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)
    }

    @Test
    fun `Get submissions for multiple assignments successfully returns data`() = runTest {
        val expected = listOf(Submission(1))

        coEvery {
            submissionApi.getSubmissionsForMultipleAssignments(any(), any(), any(), any())
        } returns DataResult.Success(expected)

        val result = dataSource.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get submissions for multiple assignments failure throws exception`() = runTest {
        coEvery { submissionApi.getSubmissionsForMultipleAssignments(any(), any(), any(), any()) } returns DataResult.Fail()

        dataSource.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true)
    }

    @Test
    fun `Get grading period for course successfully returns data`() = runTest {
        val expected = listOf(GradingPeriod(1))

        coEvery { courseApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Success(GradingPeriodResponse(expected))

        val result = dataSource.getGradingPeriodsForCourse(1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get grading period for course failure throws exception`() = runTest {
        coEvery { courseApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Fail()

        dataSource.getGradingPeriodsForCourse(1, true)
    }

    @Test
    fun `Get user enrollments for grading period successfully returns data`() = runTest {
        val expected = listOf(Enrollment(1))

        coEvery { courseApi.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getUserEnrollmentsForGradingPeriod(1, 1, 1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get user enrollments for grading period failure throws exception`() = runTest {
        coEvery { courseApi.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns DataResult.Fail()

        dataSource.getUserEnrollmentsForGradingPeriod(1, 1, 1, true)
    }

    @Test
    fun `Get assignment groups with assignments successfully returns data`() = runTest {
        val expected = listOf(AssignmentGroup(1))

        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any())
        } returns DataResult.Success(expected)

        val result = dataSource.getAssignmentGroupsWithAssignments(1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment groups with assignments failure throws exception`() = runTest {
        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Fail()

        dataSource.getAssignmentGroupsWithAssignments(1, true)
    }

    @Test
    fun `Get custom grade statuses returns data`() = runTest {
        val node1 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { name } returns "Custom Status 1"
            every { _id } returns "123"
        }

        val node2 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { name } returns "Custom Status 2"
            every { _id } returns "456"
        }

        val connection = mockk<CustomGradeStatusesQuery.CustomGradeStatusesConnection> {
            every { nodes } returns listOf(node1, node2, null)
        }

        val course = mockk<CustomGradeStatusesQuery.Course> {
            every { customGradeStatusesConnection } returns connection
        }

        val data = mockk<CustomGradeStatusesQuery.Data> {
            every { this@mockk.course } returns course
        }

        coEvery { customGradeStatusesManager.getCustomGradeStatuses(1L, true) } returns data

        val result = dataSource.getCustomGradeStatuses(1L, true)

        assertEquals(listOf(node1, node2), result)
    }

    @Test(expected = Exception::class)
    fun `Get custom grade statuses throws exception when fetch fails`() = runTest {
        coEvery { customGradeStatusesManager.getCustomGradeStatuses(1L, true) } throws Exception("Network error")

        dataSource.getCustomGradeStatuses(1L, true)
    }
}

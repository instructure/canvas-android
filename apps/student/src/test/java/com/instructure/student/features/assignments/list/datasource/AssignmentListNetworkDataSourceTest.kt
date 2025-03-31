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

package com.instructure.student.features.assignments.list.datasource

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.GradingPeriodResponse
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class AssignmentListNetworkDataSourceTest {

    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val dataSource = AssignmentListNetworkDataSource(assignmentApi, coursesApi)

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

        val result = dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        Assert.assertEquals(expected, result)
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
    fun `Get assignment groups with assignments successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getAssignmentGroupsWithAssignments(1, true)

        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment groups with assignments failure throws exception`() = runTest {
        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Fail()

        dataSource.getAssignmentGroupsWithAssignments(1, true)
    }

    @Test
    fun `Get grading periods successfully returns api model`() = runTest {
        val expected = GradingPeriodResponse(listOf(GradingPeriod(1L), GradingPeriod(2L)))

        coEvery { coursesApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getGradingPeriodsForCourse(1, true)

        Assert.assertEquals(expected.gradingPeriodList, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get grading periods failure throws exception`() = runTest {
        coEvery { coursesApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Fail()

        dataSource.getGradingPeriodsForCourse(1, true)
    }

    @Test
    fun `Get course returns succesful api model`() = runTest {
        val expected = Course(id = 1L, name = "Course 1")

        coEvery { coursesApi.getCourseWithGrade(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getCourseWithGrade(1, true)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Get course failure returns null`() = runTest {
        coEvery { coursesApi.getCourseWithGrade(any(), any()) } returns DataResult.Fail()

        val result = dataSource.getCourseWithGrade(1, true)

        Assert.assertNull(result)
    }
}
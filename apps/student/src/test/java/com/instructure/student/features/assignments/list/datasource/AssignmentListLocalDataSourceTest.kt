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

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AssignmentListLocalDataSourceTest {

    private val assignmentFacade: AssignmentFacade = mockk(relaxed = true)
    private val courseFacade: CourseFacade = mockk(relaxed = true)

    private val dataSource = AssignmentListLocalDataSource(assignmentFacade, courseFacade)

    @Test
    fun `Get assignment groups with assignments for grading period successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery { assignmentFacade.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any()) } returns expected

        val result = dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery { assignmentFacade.getAssignmentGroupsWithAssignments(any()) } returns expected

        val result = dataSource.getAssignmentGroupsWithAssignments(1, true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods successfully returns api model`() = runTest {
        val expected = listOf(GradingPeriod(1L), GradingPeriod(2L))

        coEvery { courseFacade.getGradingPeriodsByCourseId(any()) } returns expected

        val result = dataSource.getGradingPeriodsForCourse(1, true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get course with grade successfully returns api model`() = runTest {
        val expected = Course(1L)

        coEvery { courseFacade.getCourseById(any()) } returns expected

        val result = dataSource.getCourseWithGrade(1, true)

        assertEquals(expected, result)
    }
}
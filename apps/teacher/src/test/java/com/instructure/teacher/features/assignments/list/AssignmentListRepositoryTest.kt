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
package com.instructure.teacher.features.assignments.list

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.GradingPeriodResponse
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListSelectedFiltersEntityDao
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListSelectedFiltersEntity
import com.instructure.teacher.features.assignment.list.TeacherAssignmentListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AssignmentListRepositoryTest {

    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val assignmentListSelectedFiltersEntityDao: AssignmentListSelectedFiltersEntityDao = mockk(relaxed = true)

    private val repository = TeacherAssignmentListRepository(assignmentApi, courseApi, assignmentListSelectedFiltersEntityDao)

    @Test
    fun `Get assignment groups with assignments for grading period`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, forceRefresh = true)

        coVerify { assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(1, 1, false, any(), any()) }
        assertEquals(expected, result)
    }


    @Test
    fun `Get assignment groups with assignments`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getAssignments(1, true)

        coVerify { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(1, any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods`() = runTest {
        val expected = listOf(GradingPeriod(id = 1L), GradingPeriod(id = 2L))

        coEvery { courseApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Success(GradingPeriodResponse(gradingPeriodList = expected))

        val result = repository.getGradingPeriodsForCourse(1, true)

        coVerify { courseApi.getGradingPeriodsForCourse(1, any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get course from network`() = runTest {
        coEvery { courseApi.getCourseWithGrade(any(), any()) } returns DataResult.Success(
            Course(id = 1L, name = "Course 1")
        )

        val result = repository.getCourse(1, true)

        assertEquals(1L, result!!.id)
    }

    @Test
    fun `Returns saved filters from database`() = runTest {
        val expected = AssignmentListSelectedFiltersEntity(
            userDomain = "domain",
            userId = 1,
            contextId = 2,
            selectedAssignmentFilters = listOf(AssignmentFilter.All),
            selectedAssignmentStatusFilter = null,
            selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup
        )
        coEvery { assignmentListSelectedFiltersEntityDao.findAssignmentListSelectedFiltersEntity(any(), any(), any()) } returns expected

        val result = repository.getSelectedOptions("domain", 1, 2)
        assertEquals(expected, result)
    }

    @Test
    fun `Updates filters in database`() = runTest {
        val entity = AssignmentListSelectedFiltersEntity(
            userDomain = "domain",
            userId = 1,
            contextId = 2,
            selectedAssignmentFilters = listOf(AssignmentFilter.All),
            selectedAssignmentStatusFilter = null,
            selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup
        )
        coEvery { assignmentListSelectedFiltersEntityDao.findAssignmentListSelectedFiltersEntity(any(), any(), any()) } returns null
        repository.updateSelectedOptions(entity)

        coVerify { assignmentListSelectedFiltersEntityDao.insertOrUpdate(entity) }
    }
}
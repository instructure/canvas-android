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
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroup
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroupType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListFilterDao
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListFilterEntity
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
    private val assignmentListFilterDao: AssignmentListFilterDao = mockk(relaxed = true)

    private val repository = TeacherAssignmentListRepository(assignmentApi, courseApi, assignmentListFilterDao)

    @Test
    fun `Get assignment groups with assignments for grading period`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, forceRefresh = true).dataOrNull

        coVerify { assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(1, 1, false, any(), any()) }
        assertEquals(expected, result)
    }


    @Test
    fun `Get assignment groups with assignments`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getAssignments(1, true).dataOrNull

        coVerify { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(1, any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods`() = runTest {
        val expected = listOf(GradingPeriod(id = 1L), GradingPeriod(id = 2L))

        coEvery { courseApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Success(GradingPeriodResponse(gradingPeriodList = expected))

        val result = repository.getGradingPeriodsForCourse(1, true).dataOrNull

        coVerify { courseApi.getGradingPeriodsForCourse(1, any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get course from network`() = runTest {
        coEvery { courseApi.getCourseWithGrade(any(), any()) } returns DataResult.Success(
            Course(id = 1L, name = "Course 1")
        )

        val result = repository.getCourse(1, true).dataOrNull

        assertEquals(1L, result!!.id)
    }

    @Test
    fun `Returns saved filters from database`() = runTest {
        val expected = AssignmentListFilterEntity(
            userDomain = "domain",
            userId = 1,
            contextId = 2,
            groupId = 3,
            selectedIndexes = listOf(1, 2, 3)
        )
        coEvery { assignmentListFilterDao.findAssignmentListFilter(any(), any(), any(), any()) } returns expected

        val result = repository.getSelectedOptions("domain", 1, 2, 3)
        assertEquals(expected.selectedIndexes, result)
    }

    @Test
    fun `Updates filters in database`() = runTest {
        val expected = AssignmentListFilterEntity(
            userDomain = "domain",
            userId = 1,
            contextId = 2,
            groupId = 3,
            selectedIndexes = listOf(1, 2, 3)
        )
        val filterState = AssignmentListFilterState(
            filterGroups = listOf(
                AssignmentListFilterGroup(
                    groupId = 3,
                    title = "title",
                    options = emptyList(),
                    selectedOptionIndexes = listOf(1),
                    groupType = AssignmentListFilterGroupType.SingleChoice,
                    filterType = AssignmentListFilterType.Filter
                )
            )
        )
        coEvery { assignmentListFilterDao.findAssignmentListFilter(any(), any(), any(), any()) } returns expected

        repository.updateSelectedOptions("domain", 1, 2, filterState)

        coVerify { assignmentListFilterDao.update(expected.copy(selectedIndexes = listOf(1))) }
    }
}
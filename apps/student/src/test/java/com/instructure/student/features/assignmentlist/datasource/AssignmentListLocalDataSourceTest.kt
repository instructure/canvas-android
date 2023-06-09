package com.instructure.student.features.assignmentlist.datasource

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class AssignmentListLocalDataSourceTest {

    private val assignmentFacade: AssignmentFacade = mockk(relaxed = true)
    private val courseFacade: CourseFacade = mockk(relaxed = true)

    private val dataSource = AssignmentListLocalDataSource(assignmentFacade, courseFacade)

    @Test
    fun `Get assignment groups with assignments for grading period successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery { assignmentFacade.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any()) } returns expected

        val course = dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        assertEquals(expected, course)
    }

    @Test
    fun `Get assignment groups with assignments successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery { assignmentFacade.getAssignmentGroupsWithAssignments(any()) } returns expected

        val course = dataSource.getAssignmentGroupsWithAssignments(1, true)

        assertEquals(expected, course)
    }

    @Test
    fun `Get grading periods successfully returns api model`() = runTest {
        val expected = listOf(GradingPeriod(1L), GradingPeriod(2L))

        coEvery { courseFacade.getGradingPeriodsByCourseId(any()) } returns expected

        val course = dataSource.getGradingPeriodsForCourse(1, true)

        assertEquals(expected, course)
    }
}
package com.instructure.student.features.assignmentlist

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignmentlist.datasource.AssignmentListLocalDataSource
import com.instructure.student.features.assignmentlist.datasource.AssignmentListNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class AssignmentListRepositoryTest {

    private val networkDataSource: AssignmentListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: AssignmentListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)

    private val repository = AssignmentListRepository(localDataSource, networkDataSource, networkStateProvider)

    @Test
    fun `Get assignment groups with assignments for grading period if device is online`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments for grading period if device is offline`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments if device is online`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignments(any(), any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments if device is offline`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignments(any(), any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods if device is online`() = runTest {
        val expected = listOf(GradingPeriod(id = 1L), GradingPeriod(id = 2L))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getGradingPeriodsForCourse(any(), any()) } returns expected

        val result = repository.getGradingPeriodsForCourse(1, true)

        coVerify { networkDataSource.getGradingPeriodsForCourse(any(), any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods if device is offline`() = runTest {
        val expected = listOf(GradingPeriod(id = 1L), GradingPeriod(id = 2L))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getGradingPeriodsForCourse(any(), any()) } returns expected

        val result = repository.getGradingPeriodsForCourse(1, true)

        coVerify { localDataSource.getGradingPeriodsForCourse(any(), any()) }
        assertEquals(expected, result)
    }
}
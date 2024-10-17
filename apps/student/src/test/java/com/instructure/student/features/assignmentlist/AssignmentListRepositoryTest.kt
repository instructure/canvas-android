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

package com.instructure.student.features.assignmentlist

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.list.AssignmentListRepository
import com.instructure.student.features.assignments.list.datasource.AssignmentListLocalDataSource
import com.instructure.student.features.assignments.list.datasource.AssignmentListNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssignmentListRepositoryTest {

    private val networkDataSource: AssignmentListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: AssignmentListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = AssignmentListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get assignment groups with assignments for grading period if device is online`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments for grading period if device is offline`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments if device is online`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignments(1, true) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments if device is offline`() = runTest {
        val expected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns expected

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignments(1, true) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods if device is online`() = runTest {
        val expected = listOf(GradingPeriod(id = 1L), GradingPeriod(id = 2L))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getGradingPeriodsForCourse(any(), any()) } returns expected

        val result = repository.getGradingPeriodsForCourse(1, true)

        coVerify { networkDataSource.getGradingPeriodsForCourse(1, true) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods if device is offline`() = runTest {
        val expected = listOf(GradingPeriod(id = 1L), GradingPeriod(id = 2L))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getGradingPeriodsForCourse(any(), any()) } returns expected

        val result = repository.getGradingPeriodsForCourse(1, true)

        coVerify { localDataSource.getGradingPeriodsForCourse(1, true) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get course from local storage when device is offline`() = runTest {
        coEvery { networkDataSource.getCourseWithGrade(any(), any()) } returns Course(id = 1L, name = "Course 1")
        coEvery { localDataSource.getCourseWithGrade(any(), any()) } returns Course(id = 2L, name = "Course 2")
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getCourseWithGrade(1, true)

        Assert.assertEquals(2L, result!!.id)
    }

    @Test
    fun `Get course from network when device is online`() = runTest {
        coEvery { networkDataSource.getCourseWithGrade(any(), any()) } returns Course(id = 1L, name = "Course 1")
        coEvery { localDataSource.getCourseWithGrade(any(), any()) } returns Course(id = 2L, name = "Course 2")
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getCourseWithGrade(1, true)

        Assert.assertEquals(1L, result!!.id)
    }
}
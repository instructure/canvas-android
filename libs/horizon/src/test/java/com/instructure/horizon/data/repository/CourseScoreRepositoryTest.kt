/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Grades
import com.instructure.horizon.data.datasource.CourseScoreLocalDataSource
import com.instructure.horizon.data.datasource.CourseScoreNetworkDataSource
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CourseScoreRepositoryTest {
    private val networkDataSource: CourseScoreNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: CourseScoreLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val testAssignmentGroups = listOf(
        AssignmentGroup(
            id = 1L,
            name = "Homework",
            groupWeight = 40.0,
            assignments = listOf(
                Assignment(id = 101L, name = "Assignment 1"),
                Assignment(id = 102L, name = "Assignment 2")
            )
        ),
        AssignmentGroup(
            id = 2L,
            name = "Exams",
            groupWeight = 60.0,
            assignments = listOf(
                Assignment(id = 201L, name = "Midterm Exam")
            )
        )
    )
    private val testEnrollments = listOf(
        Enrollment(
            id = 1L,
            enrollmentState = EnrollmentAPI.STATE_ACTIVE,
            grades = Grades(currentScore = 85.5)
        )
    )

    @Before
    fun setup() {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        coEvery { networkDataSource.getAssignmentGroups(any(), any()) } returns testAssignmentGroups
        coEvery { networkDataSource.getEnrollments(any(), any()) } returns testEnrollments
    }

    @Test
    fun `getAssignmentGroups returns list from network data source when online`() = runTest {
        val repository = getRepository()
        val result = repository.getAssignmentGroups(1L, false)

        assertEquals(2, result.size)
        assertEquals("Homework", result[0].name)
        assertEquals(2, result[0].assignments.size)
        assertEquals("Exams", result[1].name)
        assertEquals(1, result[1].assignments.size)
        coVerify { networkDataSource.getAssignmentGroups(1L, false) }
    }

    @Test
    fun `getAssignmentGroups with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.getAssignmentGroups(1L, true)

        coVerify { networkDataSource.getAssignmentGroups(1L, true) }
    }

    @Test
    fun `getAssignmentGroups returns local data when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { localDataSource.getAssignmentGroups(any()) } returns testAssignmentGroups

        val repository = getRepository()
        val result = repository.getAssignmentGroups(1L, false)

        assertEquals(2, result.size)
        coVerify { localDataSource.getAssignmentGroups(1L) }
    }

    @Test
    fun `getEnrollments returns list from network data source when online`() = runTest {
        val repository = getRepository()
        val result = repository.getEnrollments(1L, false)

        assertEquals(1, result.size)
        assertEquals(EnrollmentAPI.STATE_ACTIVE, result[0].enrollmentState)
        assertEquals(85.5, result[0].grades?.currentScore)
        coVerify { networkDataSource.getEnrollments(1L, false) }
    }

    @Test
    fun `getEnrollments with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.getEnrollments(1L, true)

        coVerify { networkDataSource.getEnrollments(1L, true) }
    }

    @Test
    fun `getEnrollments returns local data when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { localDataSource.getEnrollments(any()) } returns testEnrollments

        val repository = getRepository()
        val result = repository.getEnrollments(1L, false)

        assertEquals(1, result.size)
        coVerify { localDataSource.getEnrollments(1L) }
    }

    @Test
    fun `getAssignmentGroups returns empty list when no groups`() = runTest {
        coEvery { networkDataSource.getAssignmentGroups(any(), any()) } returns emptyList()
        val repository = getRepository()
        val result = repository.getAssignmentGroups(1L, false)

        assertEquals(0, result.size)
    }

    private fun getRepository(): CourseScoreRepository {
        return CourseScoreRepository(networkDataSource, localDataSource, networkStateProvider, featureFlagProvider)
    }
}

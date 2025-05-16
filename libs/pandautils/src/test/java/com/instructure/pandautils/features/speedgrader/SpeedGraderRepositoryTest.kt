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
package com.instructure.pandautils.features.speedgrader

import com.instructure.canvasapi2.AssignmentDetailsQuery
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SpeedGraderRepositoryTest {

    private val assignmentDetailsManager: AssignmentDetailsManager = mockk(relaxed = true)

    private lateinit var repository: SpeedGraderRepository

    @Before
    fun setup() {
        repository = SpeedGraderRepository(assignmentDetailsManager)
    }

    @Test
    fun `getAssignmentDetails should call manager with the correct assignmentId`() = runTest {
        val assignmentId = 123L

        repository.getAssignmentDetails(assignmentId)

        coVerify { assignmentDetailsManager.getAssignmentDetails(assignmentId) }
    }

    @Test
    fun `getAssignmentDetails should return the correct assignment details`() = runTest {
        val assignmentId = 123L
        val expectedDetails = mockk<AssignmentDetailsQuery.Data>(relaxed = true)

        coEvery { assignmentDetailsManager.getAssignmentDetails(assignmentId) } returns expectedDetails

        val result = repository.getAssignmentDetails(assignmentId)

        assertEquals(expectedDetails, result)
    }

    @Test
    fun `getAssignmentDetails should handle exceptions gracefully`() = runTest {
        val assignmentId = 123L
        val exception = Exception("Network error")

        coEvery { assignmentDetailsManager.getAssignmentDetails(assignmentId) } throws exception

        try {
            repository.getAssignmentDetails(assignmentId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
    }
}
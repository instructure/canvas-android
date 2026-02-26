/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.assignment

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoadMissingAssignmentsUseCaseTest {

    private val repository: AssignmentRepository = mockk()
    private lateinit var useCase: LoadMissingAssignmentsUseCase

    @Before
    fun setUp() {
        useCase = LoadMissingAssignmentsUseCase(repository)
    }

    @Test
    fun `execute returns missing assignments on success`() = runTest {
        val assignments = listOf(
            Assignment(id = 1, name = "Assignment 1"),
            Assignment(id = 2, name = "Assignment 2")
        )
        coEvery { repository.getMissingAssignments(false) } returns DataResult.Success(assignments)

        val result = useCase(LoadMissingAssignmentsParams())

        assertEquals(assignments, result)
    }

    @Test
    fun `execute forces refresh when forceRefresh is true`() = runTest {
        val assignments = listOf(Assignment(id = 1, name = "Assignment 1"))
        coEvery { repository.getMissingAssignments(true) } returns DataResult.Success(assignments)

        val result = useCase(LoadMissingAssignmentsParams(forceRefresh = true))

        assertEquals(assignments, result)
    }

    @Test
    fun `execute throws exception when repository returns failure`() = runTest {
        val exception = Exception("Network error")
        coEvery { repository.getMissingAssignments(false) } returns DataResult.Fail(Failure.Exception(exception))

        assertThrows(Exception::class.java) {
            runTest {
                useCase(LoadMissingAssignmentsParams())
            }
        }
    }

    @Test
    fun `execute returns empty list when no missing assignments`() = runTest {
        coEvery { repository.getMissingAssignments(false) } returns DataResult.Success(emptyList())

        val result = useCase(LoadMissingAssignmentsParams())

        assertEquals(emptyList<Assignment>(), result)
    }
}
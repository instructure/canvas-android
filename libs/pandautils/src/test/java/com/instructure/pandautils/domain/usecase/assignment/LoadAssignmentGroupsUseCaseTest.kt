/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.domain.usecase.assignment

import com.instructure.canvasapi2.models.AssignmentGroup
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
class LoadAssignmentGroupsUseCaseTest {

    private val repository: AssignmentRepository = mockk()
    private lateinit var useCase: LoadAssignmentGroupsUseCase

    @Before
    fun setUp() {
        useCase = LoadAssignmentGroupsUseCase(repository)
    }

    @Test
    fun `execute returns assignment groups on success`() = runTest {
        val courseId = 123L
        val assignmentGroups = listOf(
            AssignmentGroup(id = 1, name = "Group 1"),
            AssignmentGroup(id = 2, name = "Group 2")
        )
        coEvery { repository.getAssignmentGroups(courseId, false) } returns DataResult.Success(assignmentGroups)

        val result = useCase(LoadAssignmentGroupsUseCase.Params(courseId))

        assertEquals(assignmentGroups, result)
    }

    @Test
    fun `execute forces network request when forceNetwork is true`() = runTest {
        val courseId = 123L
        val assignmentGroups = listOf(AssignmentGroup(id = 1, name = "Group 1"))
        coEvery { repository.getAssignmentGroups(courseId, true) } returns DataResult.Success(assignmentGroups)

        val result = useCase(LoadAssignmentGroupsUseCase.Params(courseId, forceNetwork = true))

        assertEquals(assignmentGroups, result)
    }

    @Test
    fun `execute throws exception when repository returns failure`() = runTest {
        val courseId = 123L
        val exception = Exception("Network error")
        coEvery { repository.getAssignmentGroups(courseId, false) } returns DataResult.Fail(Failure.Exception(exception))

        assertThrows(Exception::class.java) {
            runTest {
                useCase(LoadAssignmentGroupsUseCase.Params(courseId))
            }
        }
    }

    @Test
    fun `execute returns empty list when repository returns empty result`() = runTest {
        val courseId = 123L
        coEvery { repository.getAssignmentGroups(courseId, false) } returns DataResult.Success(emptyList())

        val result = useCase(LoadAssignmentGroupsUseCase.Params(courseId))

        assertEquals(emptyList<AssignmentGroup>(), result)
    }
}
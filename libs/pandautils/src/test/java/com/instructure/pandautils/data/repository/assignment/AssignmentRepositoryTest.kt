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

package com.instructure.pandautils.data.repository.assignment

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AssignmentRepositoryTest {

    private val userApi: UserAPI.UsersInterface = mockk()
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk()
    private lateinit var repository: AssignmentRepositoryImpl

    @Before
    fun setUp() {
        repository = AssignmentRepositoryImpl(userApi, assignmentApi)
    }

    @Test
    fun `getMissingAssignments returns success with assignments`() = runTest {
        val assignments = listOf(
            Assignment(id = 1, name = "Assignment 1"),
            Assignment(id = 2, name = "Assignment 2")
        )
        coEvery {
            userApi.getMissingSubmissions(any())
        } returns DataResult.Success(assignments)

        val result = repository.getMissingAssignments(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(2, result.dataOrNull?.size)
    }

    @Test
    fun `getMissingAssignments uses correct RestParams for forceRefresh false`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery {
            userApi.getMissingSubmissions(capture(paramsSlot))
        } returns DataResult.Success(emptyList())

        repository.getMissingAssignments(forceRefresh = false)

        assertFalse(paramsSlot.captured.isForceReadFromNetwork)
        assertTrue(paramsSlot.captured.usePerPageQueryParam)
    }

    @Test
    fun `getMissingAssignments uses correct RestParams for forceRefresh true`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery {
            userApi.getMissingSubmissions(capture(paramsSlot))
        } returns DataResult.Success(emptyList())

        repository.getMissingAssignments(forceRefresh = true)

        assertTrue(paramsSlot.captured.isForceReadFromNetwork)
        assertTrue(paramsSlot.captured.usePerPageQueryParam)
    }

    @Test
    fun `getMissingAssignments returns failure on error`() = runTest {
        val exception = Exception("Network error")
        coEvery {
            userApi.getMissingSubmissions(any())
        } returns DataResult.Fail(Failure.Exception(exception))

        val result = repository.getMissingAssignments(forceRefresh = false)

        assertTrue(result.isFail)
    }

    @Test
    fun `getMissingAssignments returns empty list when no missing assignments`() = runTest {
        coEvery {
            userApi.getMissingSubmissions(any())
        } returns DataResult.Success(emptyList())

        val result = repository.getMissingAssignments(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(0, result.dataOrNull?.size)
    }

    @Test
    fun `getAssignmentGroups returns success with assignment groups`() = runTest {
        val assignmentGroups = listOf(
            AssignmentGroup(id = 1, name = "Group 1"),
            AssignmentGroup(id = 2, name = "Group 2")
        )
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any())
        } returns DataResult.Success(assignmentGroups)

        val result = repository.getAssignmentGroups(courseId = 123L, forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(2, result.dataOrNull?.size)
    }

    @Test
    fun `getAssignmentGroups uses correct course id`() = runTest {
        val courseIdSlot = slot<Long>()
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(capture(courseIdSlot), any())
        } returns DataResult.Success(emptyList())

        repository.getAssignmentGroups(courseId = 456L, forceRefresh = false)

        assertEquals(456L, courseIdSlot.captured)
    }

    @Test
    fun `getAssignmentGroups uses correct RestParams for forceRefresh false`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), capture(paramsSlot))
        } returns DataResult.Success(emptyList())

        repository.getAssignmentGroups(courseId = 123L, forceRefresh = false)

        assertFalse(paramsSlot.captured.isForceReadFromNetwork)
        assertTrue(paramsSlot.captured.usePerPageQueryParam)
    }

    @Test
    fun `getAssignmentGroups uses correct RestParams for forceRefresh true`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), capture(paramsSlot))
        } returns DataResult.Success(emptyList())

        repository.getAssignmentGroups(courseId = 123L, forceRefresh = true)

        assertTrue(paramsSlot.captured.isForceReadFromNetwork)
        assertTrue(paramsSlot.captured.usePerPageQueryParam)
    }

    @Test
    fun `getAssignmentGroups returns failure on error`() = runTest {
        val exception = Exception("Network error")
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any())
        } returns DataResult.Fail(Failure.Exception(exception))

        val result = repository.getAssignmentGroups(courseId = 123L, forceRefresh = false)

        assertTrue(result.isFail)
    }

    @Test
    fun `getAssignmentGroups returns empty list when no assignment groups`() = runTest {
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any())
        } returns DataResult.Success(emptyList())

        val result = repository.getAssignmentGroups(courseId = 123L, forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(0, result.dataOrNull?.size)
    }
}
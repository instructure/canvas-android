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
package com.instructure.horizon.features.moduleitemsequence

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCommentsManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

class ModuleItemSequenceRepositoryTest {
    private val moduleApi: ModuleAPI.ModuleInterface = mockk(relaxed = true)
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val horizonGetCommentsManager: HorizonGetCommentsManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val userId = 1L
    private val courseId = 1L

    @Before
    fun setup() {
        every { apiPrefs.user } returns User(id = userId, name = "Test User")
    }

    @Test
    fun `Test successful module item sequence retrieval`() = runTest {
        val sequence = ModuleItemSequence()
        coEvery { moduleApi.getModuleItemSequence(any(), any(), any(), any(), any()) } returns
            DataResult.Success(sequence)

        val result = getRepository().getModuleItemSequence(courseId, "Assignment", "123")

        assertEquals(sequence, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed module item sequence retrieval throws exception`() = runTest {
        coEvery { moduleApi.getModuleItemSequence(any(), any(), any(), any(), any()) } returns DataResult.Fail()

        getRepository().getModuleItemSequence(courseId, "Assignment", "123")
    }

    @Test
    fun `Test successful modules with items retrieval`() = runTest {
        val module = ModuleObject(id = 1L, name = "Module 1", itemCount = 2, items = listOf(
            ModuleItem(id = 1L, title = "Item 1"),
            ModuleItem(id = 2L, title = "Item 2")
        ))
        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any(), any()) } returns
            DataResult.Success(listOf(module))

        val result = getRepository().getModulesWithItems(courseId, false)

        assertEquals(1, result.size)
        assertEquals(module, result[0])
    }

    @Test
    fun `Test modules with incomplete items fetches all items`() = runTest {
        val incompleteModule = ModuleObject(id = 1L, name = "Module 1", itemCount = 3, items = listOf(
            ModuleItem(id = 1L, title = "Item 1")
        ))
        val allItems = listOf(
            ModuleItem(id = 1L, title = "Item 1"),
            ModuleItem(id = 2L, title = "Item 2"),
            ModuleItem(id = 3L, title = "Item 3")
        )

        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any(), any()) } returns
            DataResult.Success(listOf(incompleteModule))
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any(), any()) } returns
            DataResult.Success(allItems)

        val result = getRepository().getModulesWithItems(courseId, false)

        assertEquals(1, result.size)
        assertEquals(3, result[0].items.size)
    }

    @Test
    fun `Test successful module item retrieval`() = runTest {
        val moduleItem = ModuleItem(id = 1L, title = "Item 1", moduleId = 1L)
        coEvery { moduleApi.getModuleItem(any(), any(), any(), any(), any()) } returns
            DataResult.Success(moduleItem)

        val result = getRepository().getModuleItem(courseId, 1L, 1L)

        assertEquals(moduleItem, result)
    }

    @Test
    fun `Test mark as done success`() = runTest {
        val moduleItem = ModuleItem(id = 1L, moduleId = 1L)
        coEvery { moduleApi.markModuleItemAsDone(any(), any(), any(), any(), any()) } returns
            DataResult.Success("".toResponseBody())

        val result = getRepository().markAsDone(courseId, moduleItem)

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `Test mark as not done success`() = runTest {
        val moduleItem = ModuleItem(id = 1L, moduleId = 1L)
        coEvery { moduleApi.markModuleItemAsNotDone(any(), any(), any(), any(), any()) } returns
            DataResult.Success("".toResponseBody())

        val result = getRepository().markAsNotDone(courseId, moduleItem)

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `Test mark as read`() = runTest {
        coEvery { moduleApi.markModuleItemRead(any(), any(), any(), any(), any()) } returns
            DataResult.Success("".toResponseBody())

        getRepository().markAsRead(courseId, 1L, 1L)

        coVerify { moduleApi.markModuleItemRead(any(), courseId, 1L, 1L, any()) }
    }

    @Test
    fun `Test successful assignment retrieval`() = runTest {
        val assignment = Assignment(id = 1L, name = "Assignment 1")
        coEvery { assignmentApi.getAssignmentWithHistory(any(), any(), any()) } returns
            DataResult.Success(assignment)

        val result = getRepository().getAssignment(1L, courseId, false)

        assertEquals(assignment, result)
    }

    @Test
    fun `Test has unread comments returns true when count greater than zero`() = runTest {
        coEvery { horizonGetCommentsManager.getUnreadCommentsCount(any(), any(), any()) } returns 5

        val result = getRepository().hasUnreadComments(1L, false)

        assertTrue(result)
    }

    @Test
    fun `Test has unread comments returns false when count is zero`() = runTest {
        coEvery { horizonGetCommentsManager.getUnreadCommentsCount(any(), any(), any()) } returns 0

        val result = getRepository().hasUnreadComments(1L, false)

        assertFalse(result)
    }

    @Test
    fun `Test has unread comments returns false when assignment id is null`() = runTest {
        val result = getRepository().hasUnreadComments(null, false)

        assertFalse(result)
    }

    private fun getRepository(): ModuleItemSequenceRepository {
        return ModuleItemSequenceRepository(moduleApi, assignmentApi, horizonGetCommentsManager, apiPrefs)
    }
}

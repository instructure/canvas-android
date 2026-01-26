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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.group.GroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadGroupsUseCaseTest {

    private val groupRepository: GroupRepository = mockk(relaxed = true)
    private lateinit var useCase: LoadGroupsUseCase

    @Before
    fun setup() {
        useCase = LoadGroupsUseCase(groupRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns only favorite groups`() = runTest {
        val groups = listOf(
            Group(id = 1, name = "Favorite Group", isFavorite = true),
            Group(id = 2, name = "Non-Favorite Group", isFavorite = false),
            Group(id = 3, name = "Another Favorite", isFavorite = true)
        )

        coEvery { groupRepository.getGroups(any()) } returns DataResult.Success(groups)

        val result = useCase(LoadGroupsParams())

        assertEquals(2, result.size)
        assertTrue(result.all { it.isFavorite })
        assertEquals(1L, result[0].id)
        assertEquals(3L, result[1].id)
    }

    @Test
    fun `execute returns empty list when no favorite groups exist`() = runTest {
        val groups = listOf(
            Group(id = 1, name = "Group A", isFavorite = false),
            Group(id = 2, name = "Group B", isFavorite = false)
        )

        coEvery { groupRepository.getGroups(any()) } returns DataResult.Success(groups)

        val result = useCase(LoadGroupsParams())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute returns empty list when repository returns empty list`() = runTest {
        coEvery { groupRepository.getGroups(any()) } returns DataResult.Success(emptyList())

        val result = useCase(LoadGroupsParams())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute passes forceRefresh parameter to repository`() = runTest {
        coEvery { groupRepository.getGroups(any()) } returns DataResult.Success(emptyList())

        useCase(LoadGroupsParams(forceRefresh = true))

        coVerify { groupRepository.getGroups(true) }
    }

    @Test
    fun `execute passes false forceRefresh by default`() = runTest {
        coEvery { groupRepository.getGroups(any()) } returns DataResult.Success(emptyList())

        useCase(LoadGroupsParams())

        coVerify { groupRepository.getGroups(false) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws when repository returns failure`() = runTest {
        coEvery { groupRepository.getGroups(any()) } returns DataResult.Fail()

        useCase(LoadGroupsParams())
    }

    @Test
    fun `execute returns all groups when all are favorites`() = runTest {
        val groups = listOf(
            Group(id = 1, name = "Group A", isFavorite = true),
            Group(id = 2, name = "Group B", isFavorite = true),
            Group(id = 3, name = "Group C", isFavorite = true)
        )

        coEvery { groupRepository.getGroups(any()) } returns DataResult.Success(groups)

        val result = useCase(LoadGroupsParams())

        assertEquals(3, result.size)
    }
}
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

package com.instructure.pandautils.data.repository.group

import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
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

class GroupRepositoryTest {

    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private lateinit var repository: GroupRepository

    @Before
    fun setup() {
        repository = GroupRepositoryImpl(groupApi)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getGroups returns success with groups`() = runTest {
        val groups = listOf(
            Group(id = 1L, name = "Group 1"),
            Group(id = 2L, name = "Group 2")
        )
        val expected = DataResult.Success(groups)
        coEvery {
            groupApi.getFirstPageGroups(any())
        } returns expected

        val result = repository.getGroups(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertEquals(groups, (result as DataResult.Success).data)
        coVerify {
            groupApi.getFirstPageGroups(match { !it.isForceReadFromNetwork && it.usePerPageQueryParam })
        }
    }

    @Test
    fun `getGroups with forceRefresh passes correct params`() = runTest {
        val groups = listOf(Group(id = 1L, name = "Group 1"))
        val expected = DataResult.Success(groups)
        coEvery {
            groupApi.getFirstPageGroups(any())
        } returns expected

        val result = repository.getGroups(forceRefresh = true)

        assertTrue(result is DataResult.Success)
        coVerify {
            groupApi.getFirstPageGroups(match { it.isForceReadFromNetwork && it.usePerPageQueryParam })
        }
    }

    @Test
    fun `getGroups returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            groupApi.getFirstPageGroups(any())
        } returns expected

        val result = repository.getGroups(forceRefresh = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getGroups depaginates when next page exists`() = runTest {
        val firstPageGroups = listOf(Group(id = 1L, name = "Group 1"))
        val secondPageGroups = listOf(Group(id = 2L, name = "Group 2"))
        val nextUrl = "https://example.com/api/v1/groups?page=2"

        val firstPageResult = DataResult.Success(
            firstPageGroups,
            linkHeaders = LinkHeaders(nextUrl = nextUrl)
        )
        val secondPageResult = DataResult.Success(secondPageGroups)

        coEvery { groupApi.getFirstPageGroups(any()) } returns firstPageResult
        coEvery { groupApi.getNextPageGroups(nextUrl, any()) } returns secondPageResult

        val result = repository.getGroups(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        val allGroups = (result as DataResult.Success).data
        assertEquals(2, allGroups.size)
        assertEquals(1L, allGroups[0].id)
        assertEquals(2L, allGroups[1].id)
    }

    @Test
    fun `getGroups returns empty list when no groups`() = runTest {
        val expected = DataResult.Success(emptyList<Group>())
        coEvery {
            groupApi.getFirstPageGroups(any())
        } returns expected

        val result = repository.getGroups(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }
}
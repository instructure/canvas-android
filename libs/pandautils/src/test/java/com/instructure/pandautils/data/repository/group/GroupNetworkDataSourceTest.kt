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
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupNetworkDataSourceTest {

    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)

    private val dataSource = GroupNetworkDataSource(groupApi)

    @Test
    fun `getGroups returns groups from api`() = runTest {
        val groups = listOf(Group(id = 1, name = "Group 1"), Group(id = 2, name = "Group 2"))
        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val result = dataSource.getGroups(false)

        assertTrue(result is DataResult.Success)
        assertEquals(groups, (result as DataResult.Success).data)
    }

    @Test
    fun `getGroups returns Fail when api fails`() = runTest {
        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Fail()

        val result = dataSource.getGroups(false)

        assertTrue(result is DataResult.Fail)
    }
}
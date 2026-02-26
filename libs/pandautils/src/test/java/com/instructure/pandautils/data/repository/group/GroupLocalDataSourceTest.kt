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

import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupLocalDataSourceTest {

    private val dataSource = GroupLocalDataSource()

    @Test
    fun `getGroups returns empty list`() = runTest {
        val result = dataSource.getGroups(false)

        assertTrue(result is DataResult.Success)
        assertEquals(emptyList<Group>(), (result as DataResult.Success).data)
    }

    @Test
    fun `getGroups returns empty list with forceRefresh`() = runTest {
        val result = dataSource.getGroups(true)

        assertTrue(result is DataResult.Success)
        assertEquals(emptyList<Group>(), (result as DataResult.Success).data)
    }
}
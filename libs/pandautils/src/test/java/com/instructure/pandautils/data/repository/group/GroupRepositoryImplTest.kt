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
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GroupRepositoryImplTest {

    private val localDataSource: GroupLocalDataSource = mockk(relaxed = true)
    private val networkDataSource: GroupNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = GroupRepositoryImpl(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `getGroups uses network when online`() = runTest {
        val networkGroups = DataResult.Success(listOf(Group(id = 1), Group(id = 2)))
        val localGroups = DataResult.Success(emptyList<Group>())

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getGroups(any()) } returns networkGroups
        coEvery { localDataSource.getGroups(any()) } returns localGroups

        val result = repository.getGroups(false)

        assertEquals(networkGroups, result)
        coVerify { networkDataSource.getGroups(false) }
    }

    @Test
    fun `getGroups uses local when offline`() = runTest {
        val networkGroups = DataResult.Success(listOf(Group(id = 1)))
        val localGroups = DataResult.Success(emptyList<Group>())

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getGroups(any()) } returns networkGroups
        coEvery { localDataSource.getGroups(any()) } returns localGroups

        val result = repository.getGroups(false)

        assertEquals(localGroups, result)
        coVerify { localDataSource.getGroups(false) }
    }

    @Test
    fun `getGroups uses network when offline but offline feature disabled`() = runTest {
        val networkGroups = DataResult.Success(listOf(Group(id = 1)))

        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        coEvery { networkDataSource.getGroups(any()) } returns networkGroups

        val result = repository.getGroups(false)

        assertEquals(networkGroups, result)
        coVerify { networkDataSource.getGroups(false) }
    }
}
/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.navigation

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.navigation.datasource.NavigationLocalDataSource
import com.instructure.student.features.navigation.datasource.NavigationNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NavigationRepositoryTest {

    private val networkDataSource: NavigationNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: NavigationLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = NavigationRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get course with grade if device is online`() = runTest {
        val onlineExpected = Course(1)
        val offlineExpected = Course(2)

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourse(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getCourse(any(), any()) } returns offlineExpected

        val result = repository.getCourse(1, true)

        coVerify { networkDataSource.getCourse(1, true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get course if device is offline`() = runTest {
        val onlineExpected = Course(1)
        val offlineExpected = Course(2)

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCourse(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getCourse(any(), any()) } returns offlineExpected

        val result = repository.getCourse(1, true)

        coVerify { localDataSource.getCourse(1, true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Is token valid returns true if network call succeeds`() = runTest {
        coEvery { networkDataSource.getSelf() } returns DataResult.Success(User())

        val result = repository.isTokenValid()

        assertTrue(result)
    }

    @Test
    fun `Is token valid returns false if network call fails`() = runTest {
        coEvery { networkDataSource.getSelf() } returns DataResult.Fail()

        val result = repository.isTokenValid()

        assertFalse(result)
    }
}
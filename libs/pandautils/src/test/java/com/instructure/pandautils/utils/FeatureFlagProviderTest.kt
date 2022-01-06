/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FeatureFlagProviderTest {

    private val userManager: UserManager = mockk(relaxed = true)
    private val remoteConfigUtils: RemoteConfigUtils = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val featureFlagProvider = FeatureFlagProvider(userManager, remoteConfigUtils, apiPrefs)

    @Before
    fun setUp() {
        every { apiPrefs.elementaryDashboardEnabledOverride } returns true
    }

    @Test
    fun `Return false if feature flag is not enabled`() = runBlockingTest {
        // Given
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User(k5User = false))
        }

        // When
        val canvasForElementaryFlag = featureFlagProvider.getCanvasForElementaryFlag()

        // Then
        assertFalse(canvasForElementaryFlag)
    }

    @Test
    fun `Return true if remote config flag and feature flag is enabled`() = runBlockingTest {
        // Given
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User(k5User = true))
        }

        // When
        val canvasForElementaryFlag = featureFlagProvider.getCanvasForElementaryFlag()

        // Then
        assertTrue(canvasForElementaryFlag)
    }

    @Test
    fun `Return false if feature flag request fails`() = runBlockingTest {
        // Given
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        val canvasForElementaryFlag = featureFlagProvider.getCanvasForElementaryFlag()

        // Then
        assertFalse(canvasForElementaryFlag)
    }

    @Test
    fun `Return true if feature flag request fails, but it is already cached as true`() = runBlockingTest {
        // Given
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { apiPrefs.canvasForElementary } returns true

        // When
        val canvasForElementaryFlag = featureFlagProvider.getCanvasForElementaryFlag()

        // Then
        assertTrue(canvasForElementaryFlag)
    }

    @Test
    fun `Successful request saves feature flag to cache`() = runBlockingTest {
        // Given
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User(k5User = true))
        }

        // When
        featureFlagProvider.getCanvasForElementaryFlag()

        // Then
        verify { apiPrefs.canvasForElementary = true }
    }

    @Test
    fun `Return false if remote config flag and feature flag is enabled but dashboard override is false`() = runBlockingTest {
        // Given
        every { apiPrefs.elementaryDashboardEnabledOverride } returns false
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User(k5User = true))
        }


        // When
        val canvasForElementaryFlag = featureFlagProvider.getCanvasForElementaryFlag()

        // Then
        assertFalse(canvasForElementaryFlag)
    }
}
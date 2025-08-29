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

import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.appdatabase.daos.EnvironmentFeatureFlagsDao
import com.instructure.pandautils.room.appdatabase.entities.EnvironmentFeatureFlags
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FeatureFlagProviderTest {

    private val userManager: UserManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)
    private val environmentFeatureFlags: EnvironmentFeatureFlagsDao = mockk(relaxed = true)

    private val featureFlagProvider = FeatureFlagProvider(userManager, apiPrefs, featuresApi, environmentFeatureFlags)

    @Before
    fun setUp() {
        every { apiPrefs.elementaryDashboardEnabledOverride } returns true
    }

    @Test
    fun `Return false if feature flag is not enabled`() = runTest {
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
    fun `Return true if remote config flag and feature flag is enabled`() = runTest {
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
    fun `Return false if feature flag request fails`() = runTest {
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
    fun `Return true if feature flag request fails, but it is already cached as true`() = runTest {
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
    fun `Successful request saves feature flag to cache`() = runTest {
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
    fun `Return false if remote config flag and feature flag is enabled but dashboard override is false`() = runTest {
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

    @Test
    fun `Save environment feature flags`() = runTest {
        val featureFlags = mapOf("feature_flag" to true)
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { featuresApi.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(featureFlags)

        featureFlagProvider.fetchEnvironmentFeatureFlags()

        coVerify(exactly = 1) { environmentFeatureFlags.insert(EnvironmentFeatureFlags(1L, featureFlags)) }
    }

    @Test
    fun `Offline is enabled when feature flag is enabled and not user is not elementary user`() = runTest {
        every { apiPrefs.canvasForElementary } returns false
        coEvery { environmentFeatureFlags.findByUserId(any()) } returns EnvironmentFeatureFlags(1L, mapOf(FEATURE_FLAG_OFFLINE to true))

        assertTrue(featureFlagProvider.offlineEnabled())
    }

    @Test
    fun `Offline is disabled when feature flag is disabled`() = runTest {
        every { apiPrefs.canvasForElementary } returns false
        coEvery { environmentFeatureFlags.findByUserId(any()) } returns EnvironmentFeatureFlags(1L, mapOf(FEATURE_FLAG_OFFLINE to false))

        assertFalse(featureFlagProvider.offlineEnabled())
    }

    @Test
    fun `Offline is disabled when feature flag is enabled and not user is an elementary user`() = runTest {
        every { apiPrefs.canvasForElementary } returns true
        coEvery { environmentFeatureFlags.findByUserId(any()) } returns EnvironmentFeatureFlags(1L, mapOf(FEATURE_FLAG_OFFLINE to true))

        assertFalse(featureFlagProvider.offlineEnabled())
    }

    @Test
    fun `checkEnvironmentFeatureFlag returns true when feature flag is enabled`() = runTest {
        val featureFlagName = "test_feature_flag"
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { environmentFeatureFlags.findByUserId(1L) } returns EnvironmentFeatureFlags(1L, mapOf(featureFlagName to true))

        val result = featureFlagProvider.checkEnvironmentFeatureFlag(featureFlagName)

        assertTrue(result)
    }

    @Test
    fun `checkEnvironmentFeatureFlag returns false when feature flag is disabled`() = runTest {
        val featureFlagName = "test_feature_flag"
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { environmentFeatureFlags.findByUserId(1L) } returns EnvironmentFeatureFlags(1L, mapOf(featureFlagName to false))

        val result = featureFlagProvider.checkEnvironmentFeatureFlag(featureFlagName)

        assertFalse(result)
    }

    @Test
    fun `checkEnvironmentFeatureFlag returns false when feature flag does not exist`() = runTest {
        val featureFlagName = "non_existent_feature_flag"
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { environmentFeatureFlags.findByUserId(1L) } returns EnvironmentFeatureFlags(1L, mapOf("other_flag" to true))

        val result = featureFlagProvider.checkEnvironmentFeatureFlag(featureFlagName)

        assertFalse(result)
    }

    @Test
    fun `checkEnvironmentFeatureFlag returns false when user is null`() = runTest {
        val featureFlagName = "test_feature_flag"
        every { apiPrefs.user } returns null

        val result = featureFlagProvider.checkEnvironmentFeatureFlag(featureFlagName)

        assertFalse(result)
    }

    @Test
    fun `checkEnvironmentFeatureFlag returns false when environment feature flags is null`() = runTest {
        val featureFlagName = "test_feature_flag"
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { environmentFeatureFlags.findByUserId(1L) } returns null

        val result = featureFlagProvider.checkEnvironmentFeatureFlag(featureFlagName)

        assertFalse(result)
    }

    @Test
    fun `checkRestrictStudentAccessFlag returns true when feature flag is enabled`() = runTest {
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { environmentFeatureFlags.findByUserId(1L) } returns EnvironmentFeatureFlags(1L, mapOf("restrict_student_access" to true))

        val result = featureFlagProvider.checkRestrictStudentAccessFlag()

        assertTrue(result)
    }

    @Test
    fun `checkRestrictStudentAccessFlag returns false when feature flag is disabled`() = runTest {
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { environmentFeatureFlags.findByUserId(1L) } returns EnvironmentFeatureFlags(1L, mapOf("restrict_student_access" to false))

        val result = featureFlagProvider.checkRestrictStudentAccessFlag()

        assertFalse(result)
    }

    @Test
    fun `checkRestrictStudentAccessFlag returns false when feature flag does not exist`() = runTest {
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { environmentFeatureFlags.findByUserId(1L) } returns EnvironmentFeatureFlags(1L, mapOf("other_flag" to true))

        val result = featureFlagProvider.checkRestrictStudentAccessFlag()

        assertFalse(result)
    }
}
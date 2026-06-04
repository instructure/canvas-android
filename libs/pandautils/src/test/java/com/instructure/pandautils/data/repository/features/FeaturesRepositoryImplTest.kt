/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.data.repository.features

import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FeaturesRepositoryImplTest {

    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)

    private lateinit var repository: FeaturesRepositoryImpl

    @Before
    fun setup() {
        repository = FeaturesRepositoryImpl(featuresApi)
    }

    @Test
    fun `getEnvironmentFeatureFlags returns success result from API`() = runTest {
        val featureFlags = mapOf("feature1" to true, "feature2" to false)
        coEvery { featuresApi.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(featureFlags)

        val result = repository.getEnvironmentFeatureFlags(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertEquals(featureFlags, (result as DataResult.Success).data)
    }

    @Test
    fun `getEnvironmentFeatureFlags returns fail result from API`() = runTest {
        coEvery { featuresApi.getEnvironmentFeatureFlags(any()) } returns DataResult.Fail()

        val result = repository.getEnvironmentFeatureFlags(forceRefresh = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getEnvironmentFeatureFlags passes forceRefresh false to RestParams`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery { featuresApi.getEnvironmentFeatureFlags(capture(paramsSlot)) } returns DataResult.Success(emptyMap())

        repository.getEnvironmentFeatureFlags(forceRefresh = false)

        coVerify { featuresApi.getEnvironmentFeatureFlags(any()) }
        assertEquals(false, paramsSlot.captured.isForceReadFromNetwork)
    }

    @Test
    fun `getEnvironmentFeatureFlags passes forceRefresh true to RestParams`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery { featuresApi.getEnvironmentFeatureFlags(capture(paramsSlot)) } returns DataResult.Success(emptyMap())

        repository.getEnvironmentFeatureFlags(forceRefresh = true)

        coVerify { featuresApi.getEnvironmentFeatureFlags(any()) }
        assertEquals(true, paramsSlot.captured.isForceReadFromNetwork)
    }

    @Test
    fun `getEnvironmentFeatureFlags returns empty map when API returns empty`() = runTest {
        coEvery { featuresApi.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(emptyMap())

        val result = repository.getEnvironmentFeatureFlags(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }
}
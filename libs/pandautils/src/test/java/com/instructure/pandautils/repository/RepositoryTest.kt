/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.pandautils.repository

import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RepositoryTest {

    val localDataSource: MockDataSource = mockk(relaxed = true)
    val networkDataSource: MockDataSource = mockk(relaxed = true)
    val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val testRepository = object :
        Repository<MockDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {}

    @Before
    fun setup() {
        every { localDataSource.getTestString() } returns "Local"
        every { networkDataSource.getTestString() } returns "Network"
    }

    @Test
    fun `Return network data source if online`() = runTest {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns true

        assertEquals("Network", testRepository.dataSource().getTestString())
    }

    @Test
    fun `Return local data source if offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true

        assertEquals("Local", testRepository.dataSource().getTestString())
    }

    @Test
    fun `Return network data source if online and feature flag is off`() = runTest {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns false

        assertEquals("Network", testRepository.dataSource().getTestString())
    }

    @Test
    fun `Return network data source if offline and feature flag is off`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns false

        assertEquals("Network", testRepository.dataSource().getTestString())
    }
}

interface MockDataSource {
    fun getTestString(): String
}
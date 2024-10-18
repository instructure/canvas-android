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

package com.instructure.student.features.file.search

import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.files.search.FileSearchLocalDataSource
import com.instructure.student.features.files.search.FileSearchNetworkDataSource
import com.instructure.student.features.files.search.FileSearchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileSearchRepositoryTest {

    private val fileSearchLocalDataSource: FileSearchLocalDataSource = mockk(relaxed = true)
    private val fileSearchNetworkDataSource: FileSearchNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val fileSearchRepository = FileSearchRepository(
        fileSearchLocalDataSource,
        fileSearchNetworkDataSource,
        networkStateProvider,
        featureFlagProvider
    )

    @Before
    fun setup() {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { networkStateProvider.isOnline() } returns true
    }

    @Test
    fun `use localDataSource when network is offline`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false

        assertTrue(fileSearchRepository.dataSource() is FileSearchLocalDataSource)
    }

    @Test
    fun `use networkDataSource when network is online`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns true

        assertTrue(fileSearchRepository.dataSource() is FileSearchNetworkDataSource)
    }
}
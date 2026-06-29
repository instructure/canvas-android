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

package com.instructure.pandautils.data.repository.dashboardcourses

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DashboardCoursesRepositoryImplTest {

    private val localDataSource: DashboardCoursesLocalDataSource = mockk()
    private val networkDataSource: DashboardCoursesNetworkDataSource = mockk()
    private val networkStateProvider: NetworkStateProvider = mockk()
    private val featureFlagProvider: FeatureFlagProvider = mockk()

    private lateinit var repository: DashboardCoursesRepositoryImpl

    private val localData = DashboardCoursesData(listOf(Course(id = 1, name = "Local")))
    private val networkData = DashboardCoursesData(listOf(Course(id = 2, name = "Network")))

    @Before
    fun setup() {
        coEvery { localDataSource.getCourses(any()) } returns localData
        coEvery { networkDataSource.getCourses(any()) } returns networkData
        repository = DashboardCoursesRepositoryImpl(
            localDataSource,
            networkDataSource,
            networkStateProvider,
            featureFlagProvider
        )
    }

    @Test
    fun `uses network data source when online`() = runTest {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns true

        assertEquals(networkData, repository.getCourses(false))
    }

    @Test
    fun `uses local data source when offline and offline enabled`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true

        assertEquals(localData, repository.getCourses(false))
    }
}

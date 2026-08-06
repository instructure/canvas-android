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

package com.instructure.pandautils.data.repository.page

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PageRepositoryImplTest {

    private val networkDataSource: PageNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: PageLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var repository: PageRepositoryImpl

    @Before
    fun setUp() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        repository = PageRepositoryImpl(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

    @Test
    fun `Get front page from network if online`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.getFrontPage(any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getFrontPage(any(), any()) } returns onlineExpected

        val frontPage = repository.getFrontPage(CanvasContext.defaultCanvasContext(), true)

        assertEquals(onlineExpected, frontPage)
    }

    @Test
    fun `Return failed result for front page if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getFrontPage(any(), any()) } returns DataResult.Fail()

        val result = repository.getFrontPage(CanvasContext.defaultCanvasContext(), false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Get front page from db if offline`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getFrontPage(any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getFrontPage(any(), any()) } returns onlineExpected

        val frontPage = repository.getFrontPage(CanvasContext.defaultCanvasContext(), true)

        assertEquals(offlineExpected, frontPage)
    }

    @Test
    fun `Get page details from network if online`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.getPageDetails(any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getPageDetails(any(), any(), any()) } returns onlineExpected

        val pageDetails = repository.getPageDetails(CanvasContext.defaultCanvasContext(), "id", true)

        assertEquals(onlineExpected, pageDetails)
    }

    @Test
    fun `Return failed result for page details if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getPageDetails(any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getPageDetails(CanvasContext.defaultCanvasContext(), "id", false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Get page details from db if offline`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getPageDetails(any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getPageDetails(any(), any(), any()) } returns onlineExpected

        val pageDetails = repository.getPageDetails(CanvasContext.defaultCanvasContext(), "id", true)

        assertEquals(offlineExpected, pageDetails)
    }
}

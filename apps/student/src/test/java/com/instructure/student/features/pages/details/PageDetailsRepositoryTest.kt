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

package com.instructure.student.features.pages.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.pages.details.datasource.PageDetailsLocalDataSource
import com.instructure.student.features.pages.details.datasource.PageDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PageDetailsRepositoryTest {

    private val networkDataSource: PageDetailsNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: PageDetailsLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var repository: PageDetailsRepository

    @Before
    fun setUp() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        repository = PageDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

    @Test
    fun `Get front page from network if online`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.getFrontPage(any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getFrontPage(any(), any()) } returns onlineExpected

        val frontPage = repository.getFrontPage(CanvasContext.defaultCanvasContext(), true)

        TestCase.assertEquals(onlineExpected, frontPage)
    }

    @Test
    fun `Return failed result for front page if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getFrontPage(any(), any()) } returns DataResult.Fail()

        val result = repository.getFrontPage(CanvasContext.defaultCanvasContext(), false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Get front page from db if offline`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getFrontPage(any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getFrontPage(any(), any()) } returns onlineExpected

        val frontPage = repository.getFrontPage(CanvasContext.defaultCanvasContext(), true)

        TestCase.assertEquals(offlineExpected, frontPage)
    }

    @Test
    fun `Get page details from network if online`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.getPageDetails(any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getPageDetails(any(), any(), any()) } returns onlineExpected

        val pageDetails = repository.getPageDetails(CanvasContext.defaultCanvasContext(), "id", true)

        TestCase.assertEquals(onlineExpected, pageDetails)
    }

    @Test
    fun `Return failed result for page details if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getPageDetails(any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getPageDetails(CanvasContext.defaultCanvasContext(), "id", false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Get page details from db if offline`() = runTest {
        val offlineExpected = DataResult.Success(Page(id = 1L, title = "Offline"))
        val onlineExpected = DataResult.Success(Page(id = 2L, title = "Online"))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getPageDetails(any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.getPageDetails(any(), any(), any()) } returns onlineExpected

        val pageDetails = repository.getPageDetails(CanvasContext.defaultCanvasContext(), "id", true)

        TestCase.assertEquals(offlineExpected, pageDetails)
    }
}

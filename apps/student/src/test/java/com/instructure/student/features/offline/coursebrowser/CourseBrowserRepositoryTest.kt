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
package com.instructure.student.features.offline.coursebrowser

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.coursebrowser.CourseBrowserRepository
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserLocalDataSource
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CourseBrowserRepositoryTest {

    private val networkDataSource: CourseBrowserNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: CourseBrowserLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val courseBrowserRepository = CourseBrowserRepository(networkDataSource, localDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Before
    fun setUp() {
        coEvery { networkDataSource.getTabs(any(), any()) } returns listOf(Tab(label = "Online"))
        coEvery { localDataSource.getTabs(any(), any()) } returns listOf(Tab(label = "Offline"))
        coEvery { networkDataSource.getFrontPage(any(), any()) } returns Page(title = "Online front page")
        coEvery { localDataSource.getFrontPage(any(), any()) } returns Page(title = "Offline front page")
    }

    @Test
    fun `Get tabs from network if device is online`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        val tabs = courseBrowserRepository.getTabs(CanvasContext.emptyCourseContext(1), true)

        coVerify { networkDataSource.getTabs(any(), any()) }
        Assert.assertEquals(listOf(Tab(label = "Online")), tabs)
    }

    @Test
    fun `Get local tabs if device is offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false

        val tabs = courseBrowserRepository.getTabs(CanvasContext.emptyCourseContext(1), true)

        coVerify { localDataSource.getTabs(any(), any()) }
        Assert.assertEquals(listOf(Tab(label = "Offline")), tabs)
    }

    @Test
    fun `Get tabs filters external and local tabs`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getTabs(any(), any()) } returns listOf(
            Tab(label = "Offline"),
            Tab(label = "External Hidden tab", type = Tab.TYPE_EXTERNAL, isHidden = true),
        )

        val tabs = courseBrowserRepository.getTabs(CanvasContext.emptyCourseContext(1), true)

        Assert.assertEquals(listOf(Tab(label = "Offline")), tabs)
    }

    @Test
    fun `Get front page from network if device is online`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        val frontPage = courseBrowserRepository.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        coVerify { networkDataSource.getFrontPage(any(), any()) }
        Assert.assertEquals(Page(title = "Online front page"), frontPage)
    }

    @Test
    fun `Get local front page if device is offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false

        val frontPage = courseBrowserRepository.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        coVerify { localDataSource.getFrontPage(any(), any()) }
        Assert.assertEquals(Page(title = "Offline front page"), frontPage)
    }
}
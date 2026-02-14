/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.instructure.pandautils.data.repository.announcement

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnnouncementRepositoryImplTest {

    private val localDataSource: AnnouncementLocalDataSource = mockk(relaxed = true)
    private val networkDataSource: AnnouncementNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = AnnouncementRepositoryImpl(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `getCourseAnnouncements uses network when online`() = runTest {
        val networkAnnouncements = DataResult.Success(listOf(DiscussionTopicHeader(id = 1)))
        val localAnnouncements = DataResult.Success(listOf(DiscussionTopicHeader(id = 2)))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourseAnnouncements(1, false) } returns networkAnnouncements
        coEvery { localDataSource.getCourseAnnouncements(1, false) } returns localAnnouncements

        val result = repository.getCourseAnnouncements(1, false)

        assertEquals(networkAnnouncements, result)
        coVerify { networkDataSource.getCourseAnnouncements(1, false) }
    }

    @Test
    fun `getCourseAnnouncements uses local when offline`() = runTest {
        val networkAnnouncements = DataResult.Success(listOf(DiscussionTopicHeader(id = 1)))
        val localAnnouncements = DataResult.Success(listOf(DiscussionTopicHeader(id = 2)))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCourseAnnouncements(1, false) } returns networkAnnouncements
        coEvery { localDataSource.getCourseAnnouncements(1, false) } returns localAnnouncements

        val result = repository.getCourseAnnouncements(1, false)

        assertEquals(localAnnouncements, result)
        coVerify { localDataSource.getCourseAnnouncements(1, false) }
    }

    @Test
    fun `getCourseAnnouncements uses network when offline but offline feature disabled`() = runTest {
        val networkAnnouncements = DataResult.Success(listOf(DiscussionTopicHeader(id = 1)))

        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        coEvery { networkDataSource.getCourseAnnouncements(1, false) } returns networkAnnouncements

        val result = repository.getCourseAnnouncements(1, false)

        assertEquals(networkAnnouncements, result)
        coVerify { networkDataSource.getCourseAnnouncements(1, false) }
    }
}
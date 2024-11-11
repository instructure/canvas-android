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

package com.instructure.student.test.conferences.conference_list

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FEATURE_FLAG_OFFLINE
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.conferences.conference_list.ConferenceListRepository
import com.instructure.student.mobius.conferences.conference_list.datasource.ConferenceListLocalDataSource
import com.instructure.student.mobius.conferences.conference_list.datasource.ConferenceListNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ConferenceListRepositoryTest {

    private val localDataSource: ConferenceListLocalDataSource = mockk(relaxed = true)
    private val networkDataSource: ConferenceListNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var repository: ConferenceListRepository

    @Before
    fun setUp() {
        repository = ConferenceListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Return conference list if online`() = runTest {
        val expected = DataResult.Success(listOf(Conference(1), Conference(2)))

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getConferencesForContext(any(), any()) } returns expected

        val result = repository.getConferencesForContext(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), true) }
        coVerify(exactly = 0) { localDataSource.getConferencesForContext(any(), any()) }
    }

    @Test
    fun `Return failed result for conference list if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getConferencesForContext(any(), any()) } returns DataResult.Fail()

        val result = repository.getConferencesForContext(CanvasContext.emptyCourseContext(1), false)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return conference list if offline`() = runTest {
        val expected = DataResult.Success(listOf(Conference(1), Conference(2)))

        every { networkStateProvider.isOnline() } returns false

        coEvery { localDataSource.getConferencesForContext(any(), any()) } returns expected

        val result = repository.getConferencesForContext(CanvasContext.emptyCourseContext(1), false)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 0) { networkDataSource.getConferencesForContext(any(), any()) }
        coVerify(exactly = 1) { localDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), false) }
    }

    @Test
    fun `Return authenticated session if online`() = runTest {
        val expected = AuthenticatedSession("sessionUrl")

        every { networkStateProvider.isOnline() } returns true

        coEvery { networkDataSource.getAuthenticatedSession(any()) } returns expected

        val result = repository.getAuthenticatedSession("targetUrl")

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { networkDataSource.getAuthenticatedSession("targetUrl") }
    }
}

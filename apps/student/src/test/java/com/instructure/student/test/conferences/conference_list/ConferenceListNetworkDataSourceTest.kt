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

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.offline.sync.ConferenceSyncHelper
import com.instructure.student.mobius.conferences.conference_list.datasource.ConferenceListNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ConferenceListNetworkDataSourceTest {

    private val conferenceSyncHelper: ConferenceSyncHelper = mockk(relaxed = true)
    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private lateinit var networkDataSource: ConferenceListNetworkDataSource

    @Before
    fun setup() {
        networkDataSource = ConferenceListNetworkDataSource(conferenceSyncHelper, oAuthApi)
    }

    @Test
    fun `Return conferences list api model`() = runTest {
        val expected = DataResult.Success(listOf(Conference(1), Conference(2)))
        coEvery { conferenceSyncHelper.getConferencesForContext(any(), any()) } returns expected

        val result = networkDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { conferenceSyncHelper.getConferencesForContext(CanvasContext.emptyCourseContext(1), true) }
    }

    @Test
    fun `Return failed result if conferences call fails`() = runTest {
        val expected = DataResult.Fail()
        coEvery { conferenceSyncHelper.getConferencesForContext(any(), any()) } returns expected

        val result = networkDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { conferenceSyncHelper.getConferencesForContext(CanvasContext.emptyCourseContext(1), true) }
    }

    @Test
    fun `Return authenticated session api model`() = runTest {
        val expected = AuthenticatedSession("url")
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Success(expected)

        val result = networkDataSource.getAuthenticatedSession("targetUrl")

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) { oAuthApi.getAuthenticatedSession("targetUrl", RestParams(isForceReadFromNetwork = true)) }
    }

    @Test(expected = IllegalStateException::class)
    fun `Throws exception if authenticated session call fails`() = runTest {
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Fail()

        networkDataSource.getAuthenticatedSession("targetUrl")
    }
}

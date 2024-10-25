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

package com.instructure.student.test.conferences.conference_details

import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceList
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.student.mobius.conferences.conference_details.datasource.ConferenceDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ConferenceDetailsNetworkDataSourceTest {

    private val conferencesApi: ConferencesApi.ConferencesInterface = mockk(relaxed = true)
    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private lateinit var networkDataSource: ConferenceDetailsNetworkDataSource

    @Before
    fun setup() {
        networkDataSource = ConferenceDetailsNetworkDataSource(conferencesApi, oAuthApi)
    }

    @Test
    fun `Return conferences list api model`() = runTest {
        val expected = listOf(Conference(1), Conference(2))
        coEvery { conferencesApi.getConferencesForContext(any(), any()) } returns DataResult.Success(ConferenceList(expected))

        val result = networkDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            conferencesApi.getConferencesForContext(
                CanvasContext.emptyCourseContext(1).toAPIString().drop(1),
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        }
    }

    @Test
    fun `Depaginate conferences`() = runTest {
        val page1 = ConferenceList(listOf(Conference(1), Conference(2), Conference(3)))
        val page2 = ConferenceList(listOf(Conference(4), Conference(5), Conference(6)))
        val page3 = ConferenceList(listOf(Conference(7), Conference(8), Conference(9)))

        coEvery { conferencesApi.getConferencesForContext(any(), any()) } returns DataResult.Success(
            page1,
            linkHeaders = LinkHeaders(nextUrl = "page_2_url")
        )

        coEvery { conferencesApi.getNextPage("page_2_url", any()) } returns DataResult.Success(
            page2,
            linkHeaders = LinkHeaders(nextUrl = "page_3_url")
        )

        coEvery { conferencesApi.getNextPage("page_3_url", any()) } returns DataResult.Success(page3)

        val result = networkDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(DataResult.Success(page1.conferences + page2.conferences + page3.conferences), result)
        coVerify(exactly = 1) {
            conferencesApi.getConferencesForContext(
                CanvasContext.emptyCourseContext(1).toAPIString().drop(1),
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
            conferencesApi.getNextPage("page_2_url", RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true))
            conferencesApi.getNextPage("page_3_url", RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed result if conferences call fails`() = runTest {
        val expected = DataResult.Fail()
        coEvery { conferencesApi.getConferencesForContext(any(), any()) } returns expected

        val result = networkDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            conferencesApi.getConferencesForContext(
                CanvasContext.emptyCourseContext(1).toAPIString().drop(1),
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        }
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

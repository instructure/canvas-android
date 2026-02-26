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

package com.instructure.pandautils.data.repository.conference

import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceList
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConferenceRepositoryImplTest {

    private val conferencesApi: ConferencesApi.ConferencesInterface = mockk(relaxed = true)
    private lateinit var repository: ConferenceRepositoryImpl

    @Before
    fun setUp() {
        repository = ConferenceRepositoryImpl(conferencesApi)
    }

    @Test
    fun `getLiveConferences returns conferences from API`() = runTest {
        val conferences = listOf(
            Conference(id = 1, title = "Conference 1"),
            Conference(id = 2, title = "Conference 2")
        )
        val conferenceList = ConferenceList(conferences = conferences)

        coEvery { conferencesApi.getLiveConferences(any()) } returns DataResult.Success(conferenceList)

        val result = repository.getLiveConferences(forceRefresh = true)

        assertTrue(result.isSuccess)
        assertEquals(2, result.dataOrNull?.size)
        assertEquals("Conference 1", result.dataOrNull?.get(0)?.title)
        assertEquals("Conference 2", result.dataOrNull?.get(1)?.title)
    }

    @Test
    fun `getLiveConferences passes forceRefresh to RestParams`() = runTest {
        val conferenceList = ConferenceList(conferences = emptyList())
        val paramsSlot = slot<RestParams>()

        coEvery { conferencesApi.getLiveConferences(capture(paramsSlot)) } returns DataResult.Success(conferenceList)

        repository.getLiveConferences(forceRefresh = true)

        assertTrue(paramsSlot.captured.isForceReadFromNetwork)
    }

    @Test
    fun `getLiveConferences returns failure when API fails`() = runTest {
        coEvery { conferencesApi.getLiveConferences(any()) } returns DataResult.Fail()

        val result = repository.getLiveConferences(forceRefresh = false)

        assertTrue(result.isFail)
    }

    @Test
    fun `getLiveConferences returns empty list when no conferences`() = runTest {
        val conferenceList = ConferenceList(conferences = emptyList())

        coEvery { conferencesApi.getLiveConferences(any()) } returns DataResult.Success(conferenceList)

        val result = repository.getLiveConferences(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(0, result.dataOrNull?.size)
    }
}
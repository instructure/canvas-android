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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.ConferenceFacade
import com.instructure.student.mobius.conferences.conference_list.datasource.ConferenceListLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ConferenceListLocalDataSourceTest {

    private val conferenceFacade: ConferenceFacade = mockk(relaxed = true)
    private lateinit var localDataSource: ConferenceListLocalDataSource

    @Before
    fun setup() {
        localDataSource = ConferenceListLocalDataSource(conferenceFacade)
    }

    @Test
    fun `Return conferences api model`() = runTest {
        val conferences = listOf(Conference(id = 1, conferenceKey = "key 1"), Conference(id = 2, conferenceKey = "key 2"))
        val expected = DataResult.Success(conferences)

        coEvery { conferenceFacade.getConferencesByCourseId(any()) } returns conferences

        val result = localDataSource.getConferencesForContext(CanvasContext.emptyCourseContext(1), false)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            conferenceFacade.getConferencesByCourseId(1)
        }
    }
}

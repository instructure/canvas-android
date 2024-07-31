/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.calendarevent.createupdate

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.parentapp.features.calendarevent.ParentCreateUpdateEventRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ParentCreateUpdateEventRepositoryTest {

    private val eventsApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val repository = ParentCreateUpdateEventRepository(eventsApi, apiPrefs)

    @Test
    fun `Get contexts returns the user only`() = runTest {
        val result = repository.getCanvasContexts()

        assertEquals(1, result.size)
    }
}
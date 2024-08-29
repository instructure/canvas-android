/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
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

package com.instructure.pandautils.features.calendar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarSharedEventsTest {

    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    private lateinit var sharedEvents: CalendarSharedEvents

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sharedEvents = CalendarSharedEvents()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Refresh action handled`() = runTest {
        val dates = listOf(LocalDate.now())

        sharedEvents.sendEvent(this, SharedCalendarAction.RefreshDays(dates))

        val event = sharedEvents.events.first()

        val expectedEvent = SharedCalendarAction.RefreshDays(dates)
        assertEquals(expectedEvent, event)
    }

    @Test
    fun `Send event when filter dialog is closed`() = runTest {
        sharedEvents.filtersClosed(this, true)

        val event = sharedEvents.events.first()

        val expectedEvent = SharedCalendarAction.FiltersClosed(true)
        assertEquals(expectedEvent, event)
    }

    @Test
    fun `Send event when filter dialog is closed and filters not changed`() = runTest {
        sharedEvents.filtersClosed(this, false)

        val event = sharedEvents.events.first()

        val expectedEvent = SharedCalendarAction.FiltersClosed(false)
        assertEquals(expectedEvent, event)
    }

    @Test
    fun `Send event when selecting a day from navigation`() = runTest {
        val selectedDate = LocalDate.of(2025, 1, 15)

        sharedEvents.sendEvent(this, SharedCalendarAction.SelectDay(selectedDate))

        val event = sharedEvents.events.first()

        val expectedEvent = SharedCalendarAction.SelectDay(selectedDate)
        assertEquals(expectedEvent, event)
    }
}
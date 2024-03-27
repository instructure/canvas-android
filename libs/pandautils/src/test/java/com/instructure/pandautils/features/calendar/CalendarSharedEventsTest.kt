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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

    lateinit var viewModel: CalendarSharedEvents

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CalendarSharedEvents()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Refresh action handled`() = runTest {
        val dates = listOf(LocalDate.now())

        viewModel.sendEvent(SharedCalendarAction.RefreshDays(dates))

        val events = mutableListOf<SharedCalendarAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedEvent = SharedCalendarAction.RefreshDays(dates)
        assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Send event when filter dialog is closed`() = runTest {
        viewModel.filtersChanged()

        val events = mutableListOf<SharedCalendarAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedEvent = SharedCalendarAction.FiltersClosed
        assertEquals(expectedEvent, events.last())
    }
}
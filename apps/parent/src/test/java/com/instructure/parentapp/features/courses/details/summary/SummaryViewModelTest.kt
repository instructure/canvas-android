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
package com.instructure.parentapp.features.courses.details.summary

import androidx.lifecycle.SavedStateHandle
import com.instructure.pandautils.features.grades.COURSE_ID_KEY
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: SummaryRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        every { savedStateHandle.get<Long>(COURSE_ID_KEY) } returns 1
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `ViewModel should load data on init`() {
        val viewModel = SummaryViewModel(repository, savedStateHandle)
        coVerify(exactly = 1) { repository.getCourse(any()) }
        coVerify(exactly = 1) { repository.getCalendarEvents(any(), false) }
    }

    @Test
    fun `ViewModel should load data with force refresh on refresh`() {
        val viewModel = SummaryViewModel(repository, savedStateHandle)
        viewModel.refresh()
        coVerify(exactly = 2) { repository.getCourse(any()) }
        coVerify(exactly = 1) { repository.getCalendarEvents(any(), false) }
        coVerify(exactly = 1) { repository.getCalendarEvents(any(), true) }
    }
}
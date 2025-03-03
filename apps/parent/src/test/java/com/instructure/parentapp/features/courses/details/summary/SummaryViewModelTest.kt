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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.features.grades.COURSE_ID_KEY
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: SummaryRepository = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)

    private lateinit var viewModel: SummaryViewModel

    @Before
    fun setup() {
        every { savedStateHandle.get<Long>(COURSE_ID_KEY) } returns 1
        Dispatchers.setMain(testDispatcher)
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1, 1)
        every { parentPrefs.currentStudent } returns User(1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `ViewModel should load data on init`() {
        createViewModel()

        coVerify(exactly = 1) { repository.getCourse(any()) }
        coVerify(exactly = 1) { repository.getCalendarEvents(any(), false) }
    }

    @Test
    fun `ViewModel should load data with force refresh on refresh`() {
        createViewModel()

        viewModel.refresh()
        coVerify(exactly = 2) { repository.getCourse(any()) }
        coVerify(exactly = 1) { repository.getCalendarEvents(any(), false) }
        coVerify(exactly = 1) { repository.getCalendarEvents(any(), true) }
    }

    @Test
    fun `Summary data maps correctly`() {
        coEvery { repository.getCourse(any()) } returns Course(1)
        val scheduleItems = listOf(ScheduleItem("Title 1"), ScheduleItem("Title 2"))
        coEvery { repository.getCalendarEvents("course_1", any()) } returns scheduleItems

        createViewModel()

        val expected = SummaryUiState(
            state = ScreenState.Content,
            items = scheduleItems,
            courseId = 1,
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Refresh data`() = runTest {
        coEvery { repository.getCourse(any()) } returns Course(1)
        coEvery { repository.getCalendarEvents("course_1", any()) } returns emptyList()

        createViewModel()

        val expected = SummaryUiState(
            state = ScreenState.Empty,
            items = emptyList(),
            courseId = 1,
            studentColor = 1
        )
        assertEquals(expected, viewModel.uiState.value)

        val scheduleItems = listOf(ScheduleItem("Title 1"), ScheduleItem("Title 2"))
        coEvery { repository.getCalendarEvents("course_1", any()) } returns scheduleItems

        viewModel.refresh()

        val expectedRefreshed = SummaryUiState(
            state = ScreenState.Content,
            items = scheduleItems,
            courseId = 1,
            studentColor = 1
        )

        assertEquals(expectedRefreshed, viewModel.uiState.value)
    }

    private fun createViewModel() {
        viewModel = SummaryViewModel(savedStateHandle, repository, parentPrefs)
    }
}

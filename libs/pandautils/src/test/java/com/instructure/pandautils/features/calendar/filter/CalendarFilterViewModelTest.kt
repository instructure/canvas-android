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
package com.instructure.pandautils.features.calendar.filter

import android.content.res.Resources
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import io.mockk.coEvery
import io.mockk.mockk
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CalendarFilterViewModelTest {

    private val calendarRepository: CalendarRepository = mockk(relaxed = true)
    private val calendarFilterDao: CalendarFilterDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private lateinit var viewModel: CalendarFilterViewModel

    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @Before
    fun setUp() {
        coEvery { calendarRepository.getCalendarFilterLimit() } returns 10
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Create error UI state when there is an error fetching the filters`() {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Fail()

        createViewModel()

        assertTrue(viewModel.uiState.value.error)
    }

    @Test
    fun `Initialize filters from all the possible filters from repository and create UI state with the correct checked status`() {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group")),
                CanvasContext.Type.USER to listOf(User(5, name = "User"))
            )
        )
        coEvery { calendarFilterDao.findByUserIdAndDomain(any(), any()) } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true)),
            calendarLimit = 10
        )

        assertEquals(expectedUiState, uiState)
    }

    @Test
    fun `Retry loads filters again`() {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Fail()
        coEvery { calendarFilterDao.findByUserIdAndDomain(any(), any()) } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        assertTrue(viewModel.uiState.value.error)

        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group")),
                CanvasContext.Type.USER to listOf(User(5, name = "User"))
            )
        )

        viewModel.handleAction(CalendarFilterAction.Retry)

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true)),
        calendarLimit = 10)

        assertEquals(expectedUiState, uiState)
    }

    @Test
    fun `Toggle filter updates filters and saves them into the database`() {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group")),
                CanvasContext.Type.USER to listOf(User(5, name = "User"))
            )
        )
        coEvery { calendarFilterDao.findByUserIdAndDomain(any(), any()) } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true)),
            calendarLimit = 10
        )

        assertEquals(expectedUiState, uiState)

        viewModel.handleAction(CalendarFilterAction.ToggleFilter("course_1"))
        viewModel.handleAction(CalendarFilterAction.ToggleFilter("user_5"))

        val newUiState = viewModel.uiState.value
        val newExpectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", true)),
            listOf(CalendarFilterItemUiState("course_1", "Course", false)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true)),
            calendarLimit = 10
        )

        assertEquals(newExpectedUiState, newUiState)
    }

    @Test
    fun `Show and dismiss snackbar when filter limit is reached`() {
        coEvery { resources.getString(R.string.calendarFilterLimitSnackbar, any()) } returns "Filter limit reached"
        coEvery { calendarRepository.getCalendarFilterLimit() } returns 2
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group")),
                CanvasContext.Type.USER to listOf(User(5, name = "User"))
            )
        )
        coEvery { calendarFilterDao.findByUserIdAndDomain(any(), any()) } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true)),
            calendarLimit = 2
        )

        assertEquals(expectedUiState, uiState)

        viewModel.handleAction(CalendarFilterAction.ToggleFilter("user_5"))

        val newUiState = viewModel.uiState.value
        val newExpectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true)),
            calendarLimit = 2, snackbarMessage = "Filter limit reached"
        )

        assertEquals(newExpectedUiState, newUiState)

        viewModel.handleAction(CalendarFilterAction.SnackbarDismissed)
        assertEquals(newExpectedUiState.copy(snackbarMessage = null), viewModel.uiState.value)
    }

    @Test
    fun `Send filter closed event without change when filters were not changed`() = runTest {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group")),
                CanvasContext.Type.USER to listOf(User(5, name = "User"))
            )
        )
        coEvery { calendarFilterDao.findByUserIdAndDomain(any(), any()) } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        viewModel.filtersClosed()

        val events = mutableListOf<CalendarFilterViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedEvent = CalendarFilterViewModelAction.FiltersClosed(false)
        assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Send filter closed event with change when filters were changed`() = runTest {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group")),
                CanvasContext.Type.USER to listOf(User(5, name = "User"))
            )
        )
        coEvery { calendarFilterDao.findByUserIdAndDomain(any(), any()) } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        viewModel.handleAction(CalendarFilterAction.ToggleFilter("user_5"))
        viewModel.filtersClosed()

        val events = mutableListOf<CalendarFilterViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedEvent = CalendarFilterViewModelAction.FiltersClosed(true)
        assertEquals(expectedEvent, events.last())
    }

    private fun createViewModel() {
        viewModel = CalendarFilterViewModel(calendarRepository, calendarFilterDao, apiPrefs, resources)
    }
}
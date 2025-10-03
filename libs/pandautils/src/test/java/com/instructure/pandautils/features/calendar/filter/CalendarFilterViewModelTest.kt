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
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CalendarFilterViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val calendarRepository: CalendarRepository = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private lateinit var viewModel: CalendarFilterViewModel

    @Before
    fun setUp() {
        ContextKeeper.appContext = mockk(relaxed = true)
        coEvery { calendarRepository.getCalendarFilterLimit() } returns 10
        every { resources.getString(R.string.calendarFilterExplanationLimited, any()) } returns "Limit 10"
        coEvery { resources.getString(R.string.calendarFilterLimitSnackbar, any()) } returns "Filter limit reached"
    }

    @Test
    fun `Create error UI state when there is an error fetching the filters`() {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Fail()

        createViewModel()

        assertTrue(viewModel.uiState.value.error)
    }

    @Test
    fun `Initialize filters from all the possible filters from repository and create UI state with the correct checked status`() {
        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10",
        )

        assertEquals(expectedUiState, uiState)
    }

    @Test
    fun `Retry loads filters again`() {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Fail()
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        assertTrue(viewModel.uiState.value.error)

        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )

        viewModel.handleAction(CalendarFilterAction.Retry)

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10")

        assertEquals(expectedUiState, uiState)
    }

    @Test
    fun `Toggle filter updates filters and saves them into the database`() {
        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(expectedUiState, uiState)

        viewModel.handleAction(CalendarFilterAction.ToggleFilter("course_1"))
        viewModel.handleAction(CalendarFilterAction.ToggleFilter("user_5"))

        val newUiState = viewModel.uiState.value
        val newExpectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", true, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", false, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(newExpectedUiState, newUiState)
        coVerify { calendarRepository.updateCalendarFilters(any()) }
    }

    @Test
    fun `Show and dismiss snackbar when filter limit is reached`() {
        coEvery { calendarRepository.getCalendarFilterLimit() } returns 2
        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(expectedUiState, uiState)

        viewModel.handleAction(CalendarFilterAction.ToggleFilter("user_5"))

        val newUiState = viewModel.uiState.value
        val newExpectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10", snackbarMessage = "Filter limit reached"
        )

        assertEquals(newExpectedUiState, newUiState)

        viewModel.handleAction(CalendarFilterAction.SnackbarDismissed)
        assertEquals(newExpectedUiState.copy(snackbarMessage = null), viewModel.uiState.value)
    }

    @Test
    fun `Send filter closed event without change when filters were not changed`() = runTest {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group"))
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        viewModel.filtersClosed()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        val expectedEvent = CalendarFilterViewModelAction.FiltersClosed(false)
        assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Send filter closed event with change when filters were changed`() = runTest {
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(Course(1, name = "Course")),
                CanvasContext.Type.GROUP to listOf(Group(3, name = "Group"))
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        viewModel.handleAction(CalendarFilterAction.ToggleFilter("user_5"))
        viewModel.filtersClosed()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        val expectedEvent = CalendarFilterViewModelAction.FiltersClosed(true)
        assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Select all selects all filters and saves them into the database`() {
        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", false, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(expectedUiState, uiState)

        viewModel.handleAction(CalendarFilterAction.SelectAll)

        val newUiState = viewModel.uiState.value
        val newExpectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", true, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(newExpectedUiState, newUiState)
        coVerify { calendarRepository.updateCalendarFilters(any()) }
    }

    @Test
    fun `Select all selects only filters up to the limit if filters are limited`() {
        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCalendarFilterLimit() } returns 2
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf()
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", false, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", false, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(expectedUiState, uiState)

        viewModel.handleAction(CalendarFilterAction.SelectAll)

        val newUiState = viewModel.uiState.value
        val newExpectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", true, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", false, group.color)),
            explanationMessage = "Limit 10", snackbarMessage = "Filter limit reached"
        )

        assertEquals(newExpectedUiState, newUiState)
        coVerify { calendarRepository.updateCalendarFilters(any()) }
    }

    @Test
    fun `Deselect all deselects all filters and saves them into the database`() {
        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(expectedUiState, uiState)

        viewModel.handleAction(CalendarFilterAction.DeselectAll)

        val newUiState = viewModel.uiState.value
        val newExpectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", false, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", false, group.color)),
            explanationMessage = "Limit 10"
        )

        assertEquals(newExpectedUiState, newUiState)
        coVerify { calendarRepository.updateCalendarFilters(any()) }
    }

    @Test
    fun `Do not show explanation and make select all available when filter limit is -1`() {
        val course = Course(1, name = "Course")
        val group = Group(3, name = "Group")
        coEvery { calendarRepository.getCalendarFilterLimit() } returns -1
        coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
            mapOf(
                CanvasContext.Type.USER to listOf(User(5, name = "User")),
                CanvasContext.Type.COURSE to listOf(course),
                CanvasContext.Type.GROUP to listOf(group)
            )
        )
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            userId = "1",
            userDomain = "domain.com",
            filters = setOf("course_1", "group_3")
        )

        createViewModel()

        val uiState = viewModel.uiState.value
        val expectedUiState = CalendarFilterScreenUiState(
            listOf(CalendarFilterItemUiState("user_5", "User", false, ThemePrefs.brandColor)),
            listOf(CalendarFilterItemUiState("course_1", "Course", true, course.color)),
            listOf(CalendarFilterItemUiState("group_3", "Group", true, group.color)),
            selectAllAvailable = true,
            explanationMessage = null
        )

        assertEquals(expectedUiState, uiState)
    }

    private fun createViewModel() {
        viewModel = CalendarFilterViewModel(calendarRepository, resources)
    }
}

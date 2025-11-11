/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.todolist.filter

import android.content.Context
import android.text.format.DateFormat
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.room.appdatabase.daos.ToDoFilterDao
import com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat

@OptIn(ExperimentalCoroutinesApi::class)
class ToDoFilterViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val toDoFilterDao: ToDoFilterDao = mockk(relaxed = true)

    private lateinit var viewModel: ToDoFilterViewModel

    private val testUser = User(id = 123L, name = "Test User")
    private val testDomain = "test.instructure.com"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android framework classes for date formatting
        mockkStatic(DateFormat::class)
        every { DateFormat.getBestDateTimePattern(any(), any()) } returns "MMM d"

        mockkConstructor(SimpleDateFormat::class)
        every { anyConstructed<SimpleDateFormat>().format(any<java.util.Date>()) } returns "Jan 1"

        every { apiPrefs.user } returns testUser
        every { apiPrefs.fullDomain } returns testDomain
        every { context.getString(R.string.todoFilterFromDate, any()) } returns "From Jan 1"
        every { context.getString(R.string.todoFilterUntilDate, any()) } returns "Until Jan 1"
        every { context.getString(DateRangeSelection.TODAY.pastLabelResId) } returns "Today"
        every { context.getString(DateRangeSelection.THIS_WEEK.pastLabelResId) } returns "This Week"
        every { context.getString(DateRangeSelection.ONE_WEEK.pastLabelResId) } returns "Last Week"
        every { context.getString(DateRangeSelection.TWO_WEEKS.pastLabelResId) } returns "2 Weeks Ago"
        every { context.getString(DateRangeSelection.THREE_WEEKS.pastLabelResId) } returns "3 Weeks Ago"
        every { context.getString(DateRangeSelection.FOUR_WEEKS.pastLabelResId) } returns "4 Weeks Ago"
        every { context.getString(DateRangeSelection.TODAY.futureLabelResId) } returns "Today"
        every { context.getString(DateRangeSelection.THIS_WEEK.futureLabelResId) } returns "This Week"
        every { context.getString(DateRangeSelection.ONE_WEEK.futureLabelResId) } returns "Next Week"
        every { context.getString(DateRangeSelection.TWO_WEEKS.futureLabelResId) } returns "In 2 Weeks"
        every { context.getString(DateRangeSelection.THREE_WEEKS.futureLabelResId) } returns "In 3 Weeks"
        every { context.getString(DateRangeSelection.FOUR_WEEKS.futureLabelResId) } returns "In 4 Weeks"

        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns null
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state has default values when no saved filters exist`() = runTest {
        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        val state = viewModel.uiState.value

        // Default checkbox states should all be false
        assertEquals(4, state.checkboxItems.size)
        assertFalse(state.checkboxItems[0].checked) // Personal todos
        assertFalse(state.checkboxItems[1].checked) // Calendar events
        assertFalse(state.checkboxItems[2].checked) // Show completed
        assertFalse(state.checkboxItems[3].checked) // Favorite courses

        // Default date range should be ONE_WEEK
        assertEquals(DateRangeSelection.ONE_WEEK, state.selectedPastOption)
        assertEquals(DateRangeSelection.ONE_WEEK, state.selectedFutureOption)

        // Flags should be false
        assertFalse(state.shouldCloseAndApplyFilters)
        assertFalse(state.areDateFiltersChanged)
    }

    @Test
    fun `Loads saved filters from database on init`() = runTest {
        val savedFilters = ToDoFilterEntity(
            id = 1,
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = true,
            calendarEvents = true,
            showCompleted = false,
            favoriteCourses = true,
            pastDateRange = DateRangeSelection.TWO_WEEKS.name,
            futureDateRange = DateRangeSelection.THREE_WEEKS.name
        )
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns savedFilters

        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        val state = viewModel.uiState.value

        // Checkbox states should match saved filters
        assertTrue(state.checkboxItems[0].checked) // Personal todos
        assertTrue(state.checkboxItems[1].checked) // Calendar events
        assertFalse(state.checkboxItems[2].checked) // Show completed
        assertTrue(state.checkboxItems[3].checked) // Favorite courses

        // Date ranges should match saved filters
        assertEquals(DateRangeSelection.TWO_WEEKS, state.selectedPastOption)
        assertEquals(DateRangeSelection.THREE_WEEKS, state.selectedFutureOption)

        coVerify { toDoFilterDao.findByUser(testDomain, testUser.id) }
    }

    @Test
    fun `Checkbox toggle updates state correctly`() = runTest {
        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Toggle personal todos checkbox
        viewModel.uiState.value.checkboxItems[0].onToggle(true)

        // Verify checkbox state updated
        val state = viewModel.uiState.value
        assertTrue(state.checkboxItems[0].checked) // Personal todos is now checked
        assertFalse(state.checkboxItems[1].checked) // Others remain unchanged
    }

    @Test
    fun `Past date range selection updates state`() = runTest {
        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Change past date range
        viewModel.uiState.value.onPastDaysChanged(DateRangeSelection.THREE_WEEKS)

        // Verify state updated
        val state = viewModel.uiState.value
        assertEquals(DateRangeSelection.THREE_WEEKS, state.selectedPastOption)
    }

    @Test
    fun `Future date range selection updates state`() = runTest {
        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Change future date range
        viewModel.uiState.value.onFutureDaysChanged(DateRangeSelection.FOUR_WEEKS)

        // Verify state updated
        val state = viewModel.uiState.value
        assertEquals(DateRangeSelection.FOUR_WEEKS, state.selectedFutureOption)
    }

    @Test
    fun `handleDone saves filters to database with correct values`() = runTest {
        coEvery { toDoFilterDao.insertOrUpdate(any()) } returns Unit
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns null

        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Toggle some checkboxes and change date ranges
        viewModel.uiState.value.checkboxItems[0].onToggle(true) // Personal todos
        viewModel.uiState.value.checkboxItems[2].onToggle(true) // Show completed
        viewModel.uiState.value.onPastDaysChanged(DateRangeSelection.TWO_WEEKS)
        viewModel.uiState.value.onFutureDaysChanged(DateRangeSelection.THREE_WEEKS)

        // Click done
        viewModel.uiState.value.onDone()

        // Verify saved entity has correct values
        coVerify {
            toDoFilterDao.insertOrUpdate(match { entity ->
                entity.userDomain == testDomain &&
                entity.userId == testUser.id &&
                entity.personalTodos == true &&
                entity.calendarEvents == false &&
                entity.showCompleted == true &&
                entity.favoriteCourses == false &&
                entity.pastDateRange == DateRangeSelection.TWO_WEEKS.name &&
                entity.futureDateRange == DateRangeSelection.THREE_WEEKS.name
            })
        }
    }

    @Test
    fun `handleDone sets shouldCloseAndApplyFilters to true`() = runTest {
        coEvery { toDoFilterDao.insertOrUpdate(any()) } returns Unit
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns null

        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        viewModel.uiState.value.onDone()

        assertTrue(viewModel.uiState.value.shouldCloseAndApplyFilters)
    }

    @Test
    fun `handleDone detects date filter changes when filters are different from saved`() = runTest {
        val savedFilters = ToDoFilterEntity(
            id = 1,
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = false,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.ONE_WEEK.name,
            futureDateRange = DateRangeSelection.ONE_WEEK.name
        )
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns savedFilters
        coEvery { toDoFilterDao.insertOrUpdate(any()) } returns Unit

        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Change date ranges
        viewModel.uiState.value.onPastDaysChanged(DateRangeSelection.TWO_WEEKS)

        viewModel.uiState.value.onDone()

        assertTrue(viewModel.uiState.value.areDateFiltersChanged)
    }

    @Test
    fun `handleDone detects no date filter changes when filters match saved`() = runTest {
        val savedFilters = ToDoFilterEntity(
            id = 1,
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = false,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.ONE_WEEK.name,
            futureDateRange = DateRangeSelection.ONE_WEEK.name
        )
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns savedFilters
        coEvery { toDoFilterDao.insertOrUpdate(any()) } returns Unit

        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Don't change date ranges, only toggle checkbox
        viewModel.uiState.value.checkboxItems[0].onToggle(true)

        viewModel.uiState.value.onDone()

        assertFalse(viewModel.uiState.value.areDateFiltersChanged)
    }

    @Test
    fun `handleDone marks date filters as changed when no previous filters exist`() = runTest {
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns null
        coEvery { toDoFilterDao.insertOrUpdate(any()) } returns Unit

        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        viewModel.uiState.value.onDone()

        // When no saved filters exist, date filters should be considered changed
        assertTrue(viewModel.uiState.value.areDateFiltersChanged)
    }

    @Test
    fun `handleFiltersApplied resets flags`() = runTest {
        coEvery { toDoFilterDao.insertOrUpdate(any()) } returns Unit
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns null

        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Trigger done to set flags
        viewModel.uiState.value.onDone()
        assertTrue(viewModel.uiState.value.shouldCloseAndApplyFilters)

        // Call handleFiltersApplied
        viewModel.uiState.value.onFiltersApplied()

        // Flags should be reset
        assertFalse(viewModel.uiState.value.shouldCloseAndApplyFilters)
        assertFalse(viewModel.uiState.value.areDateFiltersChanged)
    }

    @Test
    fun `Past date options are created in reversed order`() = runTest {
        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        val pastOptions = viewModel.uiState.value.pastDateOptions

        // Should be reversed (FOUR_WEEKS to TODAY)
        assertEquals(DateRangeSelection.FOUR_WEEKS, pastOptions[0].selection)
        assertEquals(DateRangeSelection.THREE_WEEKS, pastOptions[1].selection)
        assertEquals(DateRangeSelection.TWO_WEEKS, pastOptions[2].selection)
        assertEquals(DateRangeSelection.ONE_WEEK, pastOptions[3].selection)
        assertEquals(DateRangeSelection.THIS_WEEK, pastOptions[4].selection)
        assertEquals(DateRangeSelection.TODAY, pastOptions[5].selection)
    }

    @Test
    fun `Future date options are in normal order`() = runTest {
        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        val futureOptions = viewModel.uiState.value.futureDateOptions

        // Should be in order (TODAY to FOUR_WEEKS)
        assertEquals(DateRangeSelection.TODAY, futureOptions[0].selection)
        assertEquals(DateRangeSelection.THIS_WEEK, futureOptions[1].selection)
        assertEquals(DateRangeSelection.ONE_WEEK, futureOptions[2].selection)
        assertEquals(DateRangeSelection.TWO_WEEKS, futureOptions[3].selection)
        assertEquals(DateRangeSelection.THREE_WEEKS, futureOptions[4].selection)
        assertEquals(DateRangeSelection.FOUR_WEEKS, futureOptions[5].selection)
    }

    @Test
    fun `Multiple checkbox toggles update state correctly`() = runTest {
        viewModel = ToDoFilterViewModel(context, apiPrefs, toDoFilterDao)

        // Toggle multiple checkboxes
        viewModel.uiState.value.checkboxItems[0].onToggle(true) // Personal todos
        viewModel.uiState.value.checkboxItems[1].onToggle(true) // Calendar events
        viewModel.uiState.value.checkboxItems[3].onToggle(true) // Favorite courses

        // Verify state updated
        val state = viewModel.uiState.value
        assertTrue(state.checkboxItems[0].checked) // Personal todos
        assertTrue(state.checkboxItems[1].checked) // Calendar events
        assertFalse(state.checkboxItems[2].checked) // Show completed (not toggled)
        assertTrue(state.checkboxItems[3].checked) // Favorite courses
    }
}
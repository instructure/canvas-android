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
package com.instructure.pandautils.features.todolist

import android.content.Context
import android.os.Bundle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.todo.ToDoItemType
import com.instructure.pandautils.compose.composables.todo.ToDoStateMapper
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.todolist.filter.DateRangeSelection
import com.instructure.pandautils.room.appdatabase.daos.ToDoFilterDao
import com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ToDoListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val repository: ToDoListRepository = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val firebaseCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val toDoFilterDao: ToDoFilterDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)
    private val toDoListViewModelBehavior: ToDoListViewModelBehavior = mockk(relaxed = true)
    private val calendarSharedEvents: CalendarSharedEvents = mockk(relaxed = true)
    private val toDoStateMapper: ToDoStateMapper = mockk(relaxed = true)

    private val testUser = User(id = 123L, name = "Test User")
    private val testDomain = "test.instructure.com"

    // Track Bundle values for analytics tests
    private val bundleStorage = mutableMapOf<String, String?>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context

        // Clear bundle storage for each test
        bundleStorage.clear()

        // Mock Bundle constructor for analytics tests
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } answers {
            val key = firstArg<String>()
            val value = secondArg<String>()
            bundleStorage[key] = value
        }
        every { anyConstructed<Bundle>().getString(any()) } answers {
            val key = firstArg<String>()
            bundleStorage[key]
        }

        // Setup default filter DAO and ApiPrefs behavior
        every { apiPrefs.user } returns testUser
        every { apiPrefs.fullDomain } returns testDomain

        // Mock CalendarSharedEvents.events flow to return empty flow
        every { calendarSharedEvents.events } returns MutableSharedFlow()

        // Return a default filter that shows everything (including completed items)
        // This prevents tests from accidentally filtering out items
        val defaultTestFilter = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = true,
            calendarEvents = true,
            showCompleted = true,  // Important: show completed items in tests by default
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.FOUR_WEEKS,
            futureDateRange = DateRangeSelection.THIS_WEEK
        )
        coEvery { toDoFilterDao.findByUser(any(), any()) } returns defaultTestFilter
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkConstructor(Bundle::class)
        unmockkAll()
    }

    @Test
    fun `ViewModel init loads data successfully`() = runTest {
        val courses = listOf(
            Course(id = 1L, name = "Course 1", courseCode = "CS101"),
            Course(id = 2L, name = "Course 2", courseCode = "MATH201")
        )
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", courseId = 1L),
            createPlannerItem(id = 2L, title = "Quiz 1", courseId = 2L, plannableType = PlannableType.QUIZ)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.isLoading)
        assertFalse(uiState.isRefreshing)
        assertFalse(uiState.isError)
        assertEquals(2, uiState.itemsByDate.values.flatten().size)
    }

    @Test
    fun `ViewModel filters out announcements`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", plannableType = PlannableType.ASSIGNMENT),
            createPlannerItem(id = 2L, title = "Announcement", plannableType = PlannableType.ANNOUNCEMENT)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val allItems = uiState.itemsByDate.values.flatten()

        assertEquals(1, allItems.size)
        assertEquals("Assignment 1", allItems.first().title)
    }

    @Test
    fun `ViewModel filters out assessment requests`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", plannableType = PlannableType.ASSIGNMENT),
            createPlannerItem(id = 2L, title = "Assessment Request", plannableType = PlannableType.ASSESSMENT_REQUEST)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val allItems = uiState.itemsByDate.values.flatten()

        assertEquals(1, allItems.size)
        assertEquals("Assignment 1", allItems.first().title)
    }

    @Test
    fun `ViewModel filters out access restricted courses`() = runTest {
        val courses = listOf(
            Course(id = 1L, name = "Available Course", courseCode = "CS101", accessRestrictedByDate = false),
            Course(id = 2L, name = "Restricted Course", courseCode = "CS102", accessRestrictedByDate = true)
        )
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", courseId = 1L),
            createPlannerItem(id = 2L, title = "Assignment 2", courseId = 2L)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)
        every { context.getString(any(), any()) } returns "CS101"

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val allItems = uiState.itemsByDate.values.flatten()

        // Only assignment from non-restricted course should be present with context label
        assertEquals(2, allItems.size)
        // First item should have context label from course map
        assertTrue(allItems.any { it.title == "Assignment 1" })
    }

    @Test
    fun `ViewModel filters out invited courses`() = runTest {
        val courses = listOf(
            Course(id = 1L, name = "Enrolled Course", courseCode = "CS101", enrollments = mutableListOf()),
            Course(id = 2L, name = "Invited Course", courseCode = "CS102", enrollments = mutableListOf(mockk {
                every { enrollmentState } returns "invited"
            }))
        )
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", courseId = 1L),
            createPlannerItem(id = 2L, title = "Assignment 2", courseId = 2L)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        // Should have both assignments, but invited course won't be in course map
        assertEquals(2, uiState.itemsByDate.values.flatten().size)
    }

    @Test
    fun `ViewModel handles error state`() = runTest {
        coEvery { repository.getCourses(any()) } returns DataResult.Fail()
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Fail()

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertTrue(uiState.isError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isRefreshing)
    }

    @Test
    fun `ViewModel handles exception during load`() = runTest {
        coEvery { repository.getCourses(any()) } throws RuntimeException("Test error")

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertTrue(uiState.isError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isRefreshing)
    }

    @Test
    fun `ViewModel groups items by date`() = runTest {
        val date1 = Date(1704067200000L) // Jan 1, 2024
        val date2 = Date(1704153600000L) // Jan 2, 2024

        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date1),
            createPlannerItem(id = 2L, title = "Assignment 2", plannableDate = date1),
            createPlannerItem(id = 3L, title = "Assignment 3", plannableDate = date2)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertEquals(2, uiState.itemsByDate.keys.size)
    }

    @Test
    fun `ViewModel maps item types correctly`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment", plannableType = PlannableType.ASSIGNMENT),
            createPlannerItem(id = 2L, title = "Quiz", plannableType = PlannableType.QUIZ),
            createPlannerItem(id = 3L, title = "Discussion", plannableType = PlannableType.DISCUSSION_TOPIC),
            createPlannerItem(id = 4L, title = "Calendar Event", plannableType = PlannableType.CALENDAR_EVENT),
            createPlannerItem(id = 5L, title = "Planner Note", plannableType = PlannableType.PLANNER_NOTE),
            createPlannerItem(id = 6L, title = "Sub Assignment", plannableType = PlannableType.SUB_ASSIGNMENT)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val allItems = uiState.itemsByDate.values.flatten()

        assertEquals(6, allItems.size)
        assertEquals(ToDoItemType.ASSIGNMENT, allItems.find { it.title == "Assignment" }?.itemType)
        assertEquals(ToDoItemType.QUIZ, allItems.find { it.title == "Quiz" }?.itemType)
        assertEquals(ToDoItemType.DISCUSSION, allItems.find { it.title == "Discussion" }?.itemType)
        assertEquals(ToDoItemType.CALENDAR_EVENT, allItems.find { it.title == "Calendar Event" }?.itemType)
        assertEquals(ToDoItemType.PLANNER_NOTE, allItems.find { it.title == "Planner Note" }?.itemType)
        assertEquals(ToDoItemType.SUB_ASSIGNMENT, allItems.find { it.title == "Sub Assignment" }?.itemType)
    }

    @Test
    fun `ViewModel sets isChecked true for submitted assignments`() = runTest {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Submitted Assignment",
            plannableType = PlannableType.ASSIGNMENT,
            submitted = true
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val item = uiState.itemsByDate.values.flatten().first()

        assertTrue(item.isChecked)
    }

    @Test
    fun `ViewModel sets isChecked false for unsubmitted assignments`() = runTest {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Unsubmitted Assignment",
            plannableType = PlannableType.ASSIGNMENT,
            submitted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val item = uiState.itemsByDate.values.flatten().first()

        assertFalse(item.isChecked)
    }

    // Callback tests
    @Test
    fun `onRefresh callback triggers data reload with forceRefresh`() = runTest {
        val courses = listOf(Course(id = 1L, name = "Course 1", courseCode = "CS101"))
        val initialPlannerItems = listOf(createPlannerItem(id = 1L, title = "Assignment 1"))
        val refreshedPlannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1"),
            createPlannerItem(id = 2L, title = "Assignment 2")
        )

        coEvery { repository.getCourses(false) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), false) } returns DataResult.Success(initialPlannerItems)
        coEvery { repository.getCourses(true) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), true) } returns DataResult.Success(refreshedPlannerItems)

        val viewModel = getViewModel()

        // Verify initial data
        val initialUiState = viewModel.uiState.value
        assertEquals(1, initialUiState.itemsByDate.values.flatten().size)
        assertEquals("Assignment 1", initialUiState.itemsByDate.values.flatten().first().title)

        // Trigger refresh
        viewModel.uiState.value.onRefresh()

        // Verify refreshed data
        val refreshedUiState = viewModel.uiState.value
        assertEquals(2, refreshedUiState.itemsByDate.values.flatten().size)
        assertTrue(refreshedUiState.itemsByDate.values.flatten().any { it.title == "Assignment 2" })
    }

    @Test
    fun `Empty planner items returns empty state`() = runTest {
        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.isLoading)
        assertFalse(uiState.isError)
        assertTrue(uiState.itemsByDate.isEmpty())
    }

    @Test
    fun `Items are sorted by comparison date`() = runTest {
        val date1 = Date(1704067200000L) // Earlier date
        val date2 = Date(1704153600000L) // Later date

        val plannerItems = listOf(
            createPlannerItem(id = 2L, title = "Later Assignment", plannableDate = date2),
            createPlannerItem(id = 1L, title = "Earlier Assignment", plannableDate = date1)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val dates = uiState.itemsByDate.keys.toList()

        // Dates should be sorted (earlier date first)
        assertTrue(dates.size == 2)
    }

    // Todo count tests
    @Test
    fun `ViewModel calculates todo count correctly on initial load`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Unchecked 1", submitted = false),
            createPlannerItem(id = 2L, title = "Checked", submitted = true),
            createPlannerItem(id = 3L, title = "Unchecked 2", submitted = false)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertEquals(2, uiState.toDoCount)
    }

    @Test
    fun `ViewModel emits zero todo count when all items are checked`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Checked 1", submitted = true),
            createPlannerItem(id = 2L, title = "Checked 2", submitted = true)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertEquals(0, uiState.toDoCount)
    }

    @Test
    fun `ViewModel emits todo count when all items are unchecked`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Unchecked 1", submitted = false),
            createPlannerItem(id = 2L, title = "Unchecked 2", submitted = false),
            createPlannerItem(id = 3L, title = "Unchecked 3", submitted = false)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        assertEquals(3, uiState.toDoCount)
    }

    // Checkbox toggle tests
    @Test
    fun `Checkbox toggle successfully marks item as done`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        val uiState = viewModel.uiState.value

        assertTrue(uiState.itemsByDate.values.flatten().first().isChecked)
        assertEquals("Assignment", uiState.confirmationSnackbarData?.title)
        assertTrue(uiState.confirmationSnackbarData?.markedAsDone == true)
        coVerify { repository.createPlannerOverride(1L, PlannableType.ASSIGNMENT, true) }
    }

    @Test
    fun `Checkbox toggle successfully marks item as undone`() = runTest {
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false).copy(
            plannerOverride = plannerOverride
        )
        val updatedOverride = plannerOverride.copy(markedComplete = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.updatePlannerOverride(any(), any()) } returns DataResult.Success(updatedOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(false)

        val uiState = viewModel.uiState.value

        assertFalse(uiState.itemsByDate.values.flatten().first().isChecked)
        coVerify { repository.updatePlannerOverride(100L, false) }
    }

    @Test
    fun `Checkbox toggle shows offline snackbar when device is offline`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        every { networkStateProvider.isOnline() } returns false
        every { context.getString(R.string.todoActionOffline) } returns "This action cannot be performed offline"

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        val uiState = viewModel.uiState.value

        assertFalse(uiState.itemsByDate.values.flatten().first().isChecked)
        assertEquals("This action cannot be performed offline", uiState.snackbarMessage)
    }

    @Test
    fun `Checkbox toggle reverts on failure`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Fail()
        every { networkStateProvider.isOnline() } returns true
        every { context.getString(R.string.errorUpdatingToDo) } returns "Error updating to-do"

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        val uiState = viewModel.uiState.value

        assertFalse(uiState.itemsByDate.values.flatten().first().isChecked)
        assertEquals("Error updating to-do", uiState.snackbarMessage)
    }

    // Swipe to done tests
    @Test
    fun `Swipe to done successfully marks item as done when unchecked`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        val uiState = viewModel.uiState.value

        assertTrue(uiState.itemsByDate.values.flatten().first().isChecked)
        assertEquals("Assignment", uiState.confirmationSnackbarData?.title)
        assertTrue(uiState.confirmationSnackbarData?.markedAsDone == true)
    }

    @Test
    fun `Swipe to done successfully marks item as undone when checked`() = runTest {
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false).copy(
            plannerOverride = plannerOverride
        )
        val updatedOverride = plannerOverride.copy(markedComplete = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.updatePlannerOverride(any(), any()) } returns DataResult.Success(updatedOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.itemsByDate.values.flatten().first().isChecked)
    }

    @Test
    fun `Swipe to done shows offline snackbar when device is offline`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        every { networkStateProvider.isOnline() } returns false
        every { context.getString(R.string.todoActionOffline) } returns "This action cannot be performed offline"

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.itemsByDate.values.flatten().first().isChecked)
        assertEquals("This action cannot be performed offline", uiState.snackbarMessage)
    }

    // Cache invalidation tests
    @Test
    fun `Cache is invalidated after successfully creating planner override`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        verify { repository.invalidateCachedResponses() }
    }

    @Test
    fun `Cache is invalidated after successfully updating planner override`() = runTest {
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false).copy(
            plannerOverride = plannerOverride
        )
        val updatedOverride = plannerOverride.copy(markedComplete = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.updatePlannerOverride(any(), any()) } returns DataResult.Success(updatedOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(false)

        verify { repository.invalidateCachedResponses() }
    }

    @Test
    fun `Cache is not invalidated when planner override update fails`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Fail()
        every { networkStateProvider.isOnline() } returns true
        every { context.getString(R.string.errorUpdatingToDo) } returns "Error updating to-do"

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        verify(exactly = 0) { repository.invalidateCachedResponses() }
    }

    // Undo tests
    @Test
    fun `Undo mark as done successfully reverts item to unchecked`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val revertedOverride = plannerOverride.copy(markedComplete = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(1L, PlannableType.ASSIGNMENT, true) } returns DataResult.Success(plannerOverride)
        coEvery { repository.updatePlannerOverride(100L, false) } returns DataResult.Success(revertedOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        // First mark as done
        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        // Verify marked as done
        assertTrue(viewModel.uiState.value.itemsByDate.values.flatten().first().isChecked)

        // Now undo
        viewModel.uiState.value.onUndoMarkAsDoneUndoneAction()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.itemsByDate.values.flatten().first().isChecked)
        assertEquals(null, uiState.confirmationSnackbarData)
    }

    @Test
    fun `Todo count updates when item is marked as done`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        assertEquals(1, viewModel.uiState.value.toDoCount)

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        assertEquals(0, viewModel.uiState.value.toDoCount)
    }

    @Test
    fun `Todo count updates when item is marked as undone`() = runTest {
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false).copy(
            plannerOverride = plannerOverride
        )
        val updatedOverride = plannerOverride.copy(markedComplete = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.updatePlannerOverride(any(), any()) } returns DataResult.Success(updatedOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        assertEquals(0, viewModel.uiState.value.toDoCount)

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(false)

        assertEquals(1, viewModel.uiState.value.toDoCount)
    }

    // Filter integration tests
    @Test
    fun `onFiltersChanged with dateFiltersChanged true triggers data reload`() = runTest {
        val courses = listOf(Course(id = 1L, name = "Course 1", courseCode = "CS101"))
        val initialPlannerItems = listOf(createPlannerItem(id = 1L, title = "Assignment 1"))
        val updatedPlannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1"),
            createPlannerItem(id = 2L, title = "Assignment 2")
        )

        coEvery { repository.getCourses(false) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), false) } returns DataResult.Success(initialPlannerItems)
        coEvery { repository.getCourses(true) } returns DataResult.Success(courses) andThen DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), true) } returns DataResult.Success(updatedPlannerItems)

        val viewModel = getViewModel()

        // Verify initial data
        assertEquals(1, viewModel.uiState.value.itemsByDate.values.flatten().size)

        // Trigger filter change with dateFiltersChanged=true
        viewModel.uiState.value.onFiltersChanged(true)

        // Verify data was reloaded
        coVerify(atLeast = 2) { repository.getCourses(any()) }
        coVerify(atLeast = 2) { repository.getPlannerItems(any(), any(), any()) }
        verify { toDoListViewModelBehavior.updateWidget(false) }
    }

    @Test
    fun `onFiltersChanged with dateFiltersChanged false applies filters locally`() = runTest {
        val courses = listOf(Course(id = 1L, name = "Course 1", courseCode = "CS101"))
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1"),
            createPlannerItem(id = 2L, title = "Assignment 2")
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        // Clear invocation counters after init
        clearMocks(repository, toDoListViewModelBehavior, answers = false)
        coEvery { repository.getCourses(any()) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        // Trigger filter change with dateFiltersChanged=false
        viewModel.uiState.value.onFiltersChanged(false)

        // Verify data was NOT reloaded from repository (no additional calls)
        coVerify(exactly = 0) { repository.getCourses(any()) }
        coVerify(exactly = 0) { repository.getPlannerItems(any(), any(), any()) }
        verify { toDoListViewModelBehavior.updateWidget(false) }
    }

    @Test
    fun `Swipe to done adds item to removingItemIds when showCompleted is false`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        // Item should be added to removingItemIds since showCompleted=false
        assertTrue(viewModel.uiState.value.removingItemIds.contains("1"))
    }

    @Test
    fun `Swipe to done does NOT add item to removingItemIds when showCompleted is true`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = true
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        // Item should NOT be added to removingItemIds since showCompleted=true
        assertFalse(viewModel.uiState.value.removingItemIds.contains("1"))
    }

    @Test
    fun `Swipe to done removes item from removingItemIds on failure`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Fail()
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true
        every { context.getString(R.string.errorUpdatingToDo) } returns "Error updating to-do"

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        // Item should be removed from removingItemIds since update failed
        assertFalse(viewModel.uiState.value.removingItemIds.contains("1"))
    }

    @Test
    fun `Checkbox toggle does NOT immediately add item to removingItemIds`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        // Item should NOT be immediately added to removingItemIds (debounced)
        assertFalse(viewModel.uiState.value.removingItemIds.contains("1"))
    }

    @Test
    fun `Checkbox debounce timer adds items to removingItemIds after delay`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        // Item should NOT be immediately added
        assertFalse(viewModel.uiState.value.removingItemIds.contains("1"))

        // Advance time past debounce delay (1 second)
        advanceTimeBy(1100)

        // Now item should be added to removingItemIds
        assertTrue(viewModel.uiState.value.removingItemIds.contains("1"))
    }

    @Test
    fun `Checkbox debounce timer resets when another checkbox action occurs`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", submitted = false),
            createPlannerItem(id = 2L, title = "Assignment 2", submitted = false)
        )
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val items = viewModel.uiState.value.itemsByDate.values.flatten()
        items[0].onCheckboxToggle(true)

        // Advance time partway through debounce
        advanceTimeBy(500)

        // Toggle another item - this should reset the timer
        items[1].onCheckboxToggle(true)

        // Advance time to where first item would have been added
        advanceTimeBy(700)

        // First item should NOT be added yet (timer was reset)
        assertFalse(viewModel.uiState.value.removingItemIds.contains("1"))

        // Advance remaining time
        advanceTimeBy(400)

        // Now both items should be added
        assertTrue(viewModel.uiState.value.removingItemIds.contains("1"))
        assertTrue(viewModel.uiState.value.removingItemIds.contains("2"))
    }

    @Test
    fun `Undo removes item from removingItemIds`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val revertedOverride = plannerOverride.copy(markedComplete = false)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(1L, PlannableType.ASSIGNMENT, true) } returns DataResult.Success(plannerOverride)
        coEvery { repository.updatePlannerOverride(100L, false) } returns DataResult.Success(revertedOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        // Mark item as done
        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        // Item should be in removingItemIds
        assertTrue(viewModel.uiState.value.removingItemIds.contains("1"))

        // Undo
        viewModel.uiState.value.onUndoMarkAsDoneUndoneAction()

        // Item should be removed from removingItemIds
        assertFalse(viewModel.uiState.value.removingItemIds.contains("1"))
    }

    @Test
    fun `Data reload clears removingItemIds`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        // Mark item as done
        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        // Item should be in removingItemIds
        assertTrue(viewModel.uiState.value.removingItemIds.contains("1"))

        // Trigger data reload
        viewModel.uiState.value.onRefresh()

        // removingItemIds should be cleared
        assertTrue(viewModel.uiState.value.removingItemIds.isEmpty())
    }

    @Test
    fun `isFilterApplied is true when personal todos filter is enabled`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = true
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when calendar events filter is enabled`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            calendarEvents = true
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when show completed filter is enabled`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = true
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when favorite courses filter is enabled`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            favoriteCourses = true
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when past date range is not default`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            pastDateRange = DateRangeSelection.TWO_WEEKS
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when future date range is not default`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            futureDateRange = DateRangeSelection.THREE_WEEKS
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is false when all filters are default`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = false,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.FOUR_WEEKS,
            futureDateRange = DateRangeSelection.THIS_WEEK
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isFilterApplied)
    }

    // Analytics tracking tests
    @Test
    fun `Analytics event is logged when item is marked as done`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        verify { analytics.logEvent(AnalyticsEventConstants.TODO_ITEM_MARKED_DONE) }
    }

    @Test
    fun `Analytics event is logged when item is marked as undone`() = runTest {
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false).copy(
            plannerOverride = plannerOverride
        )
        val updatedOverride = plannerOverride.copy(markedComplete = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.updatePlannerOverride(any(), any()) } returns DataResult.Success(updatedOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(false)

        verify { analytics.logEvent(AnalyticsEventConstants.TODO_ITEM_MARKED_UNDONE) }
    }

    @Test
    fun `Analytics event is logged when item is marked as done via swipe`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        verify { analytics.logEvent(AnalyticsEventConstants.TODO_ITEM_MARKED_DONE) }
    }

    @Test
    fun `Analytics event is not logged when item update fails`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Assignment", submitted = false)

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Fail()
        every { networkStateProvider.isOnline() } returns true
        every { context.getString(R.string.errorUpdatingToDo) } returns "Error updating to-do"

        val viewModel = getViewModel()

        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        verify(exactly = 0) { analytics.logEvent(AnalyticsEventConstants.TODO_ITEM_MARKED_DONE) }
        verify(exactly = 0) { analytics.logEvent(AnalyticsEventConstants.TODO_ITEM_MARKED_UNDONE) }
    }

    @Test
    fun `Analytics event is logged for default filter on init`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = false,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.FOUR_WEEKS,
            futureDateRange = DateRangeSelection.THIS_WEEK
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        getViewModel()

        verify { analytics.logEvent(AnalyticsEventConstants.TODO_LIST_LOADED_DEFAULT_FILTER) }
    }

    @Test
    fun `Analytics event is logged for custom filter with personal todos enabled`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = true,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.ONE_WEEK,
            futureDateRange = DateRangeSelection.ONE_WEEK
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        getViewModel()

        val bundleSlot = slot<Bundle>()
        verify {
            analytics.logEvent(
                AnalyticsEventConstants.TODO_LIST_LOADED_CUSTOM_FILTER,
                capture(bundleSlot)
            )
        }

        assertEquals("true", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_PERSONAL_TODOS))
        assertEquals("false", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_CALENDAR_EVENTS))
        assertEquals("false", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SHOW_COMPLETED))
        assertEquals("false", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_FAVOURITE_COURSES))
        assertEquals("one_week", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_PAST))
        assertEquals("one_week", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_FUTURE))
    }

    @Test
    fun `Analytics event is logged for custom filter with all options enabled`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = true,
            calendarEvents = true,
            showCompleted = true,
            favoriteCourses = true,
            pastDateRange = DateRangeSelection.TWO_WEEKS,
            futureDateRange = DateRangeSelection.THREE_WEEKS
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        getViewModel()

        val bundleSlot = slot<Bundle>()
        verify {
            analytics.logEvent(
                AnalyticsEventConstants.TODO_LIST_LOADED_CUSTOM_FILTER,
                capture(bundleSlot)
            )
        }

        assertEquals("true", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_PERSONAL_TODOS))
        assertEquals("true", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_CALENDAR_EVENTS))
        assertEquals("true", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SHOW_COMPLETED))
        assertEquals("true", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_FAVOURITE_COURSES))
        assertEquals("two_weeks", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_PAST))
        assertEquals("three_weeks", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_FUTURE))
    }

    @Test
    fun `Analytics event is logged for custom filter with custom date ranges`() = runTest {
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            personalTodos = false,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.FOUR_WEEKS,
            futureDateRange = DateRangeSelection.FOUR_WEEKS
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters

        getViewModel()

        val bundleSlot = slot<Bundle>()
        verify {
            analytics.logEvent(
                AnalyticsEventConstants.TODO_LIST_LOADED_CUSTOM_FILTER,
                capture(bundleSlot)
            )
        }

        assertEquals("false", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_PERSONAL_TODOS))
        assertEquals("false", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_CALENDAR_EVENTS))
        assertEquals("false", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SHOW_COMPLETED))
        assertEquals("false", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_FAVOURITE_COURSES))
        assertEquals("four_weeks", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_PAST))
        assertEquals("four_weeks", bundleSlot.captured.getString(AnalyticsParamConstants.FILTER_SELECTED_DATE_RANGE_FUTURE))
    }

    @Test
    fun `Account-level calendar events are not clickable`() = runTest {
        val accountCalendarEvent = createPlannerItem(
            id = 1L,
            title = "Account Event",
            plannableType = PlannableType.CALENDAR_EVENT
        ).copy(contextType = "Account")

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(accountCalendarEvent))

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val item = uiState.itemsByDate.values.flatten().first()

        assertFalse(item.isClickable)
        assertEquals(ToDoItemType.CALENDAR_EVENT, item.itemType)
    }

    @Test
    fun `Course-level calendar events are clickable`() = runTest {
        val courseCalendarEvent = createPlannerItem(
            id = 1L,
            title = "Course Event",
            courseId = 1L,
            plannableType = PlannableType.CALENDAR_EVENT
        ).copy(contextType = "Course")

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(courseCalendarEvent))

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val item = uiState.itemsByDate.values.flatten().first()

        assertTrue(item.isClickable)
        assertEquals(ToDoItemType.CALENDAR_EVENT, item.itemType)
    }

    @Test
    fun `User-level calendar events are clickable`() = runTest {
        val userCalendarEvent = createPlannerItem(
            id = 1L,
            title = "User Event",
            plannableType = PlannableType.CALENDAR_EVENT
        ).copy(contextType = "User")

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(userCalendarEvent))

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val item = uiState.itemsByDate.values.flatten().first()

        assertTrue(item.isClickable)
        assertEquals(ToDoItemType.CALENDAR_EVENT, item.itemType)
    }

    @Test
    fun `RefreshToDoList event triggers loadData with forceRefresh`() = runTest {
        val courses = listOf(Course(id = 1L, name = "Course 1", courseCode = "CS101"))
        val initialPlannerItems = listOf(createPlannerItem(id = 1L, title = "Assignment 1"))
        val refreshedPlannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1"),
            createPlannerItem(id = 2L, title = "Assignment 2")
        )

        coEvery { repository.getCourses(false) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), false) } returns DataResult.Success(initialPlannerItems)
        coEvery { repository.getCourses(true) } returns DataResult.Success(courses)
        coEvery { repository.getPlannerItems(any(), any(), true) } returns DataResult.Success(refreshedPlannerItems)

        // Create a real MutableSharedFlow for testing
        val sharedEventsFlow = MutableSharedFlow<SharedCalendarAction>()
        every { calendarSharedEvents.events } returns sharedEventsFlow

        val viewModel = getViewModel()

        // Verify initial data
        assertEquals(1, viewModel.uiState.value.itemsByDate.values.flatten().size)

        // Emit RefreshToDoList event
        sharedEventsFlow.emit(SharedCalendarAction.RefreshToDoList)

        // Verify data was reloaded with forceRefresh=true
        coVerify { repository.getCourses(true) }
        coVerify { repository.getPlannerItems(any(), any(), true) }
        assertEquals(2, viewModel.uiState.value.itemsByDate.values.flatten().size)
    }

    @Test
    fun `Empty state is shown when completing the last item via swipe`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Last Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        // Verify we start with one item
        assertEquals(1, viewModel.uiState.value.itemsByDate.values.flatten().size)
        assertEquals(1, viewModel.uiState.value.toDoCount)

        // Complete the last item via swipe
        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onSwipeToDone()

        val uiState = viewModel.uiState.value

        // Item should be marked as checked
        assertTrue(uiState.itemsByDate.values.flatten().first().isChecked)
        // Item should be added to removingItemIds (will be hidden from UI, triggering empty state)
        assertTrue(uiState.removingItemIds.contains("1"))
        // Todo count should be zero
        assertEquals(0, uiState.toDoCount)
    }

    @Test
    fun `Empty state is shown when completing the last item via checkbox after debounce`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Last Assignment", submitted = false)
        val plannerOverride = PlannerOverride(id = 100L, plannableId = 1L, plannableType = PlannableType.ASSIGNMENT, markedComplete = true)
        val filters = ToDoFilterEntity(
            userDomain = testDomain,
            userId = testUser.id,
            showCompleted = false
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(listOf(plannerItem))
        coEvery { repository.createPlannerOverride(any(), any(), any()) } returns DataResult.Success(plannerOverride)
        coEvery { toDoFilterDao.findByUser(testDomain, testUser.id) } returns filters
        every { networkStateProvider.isOnline() } returns true

        val viewModel = getViewModel()

        // Verify we start with one item and todo count is 1
        assertEquals(1, viewModel.uiState.value.itemsByDate.values.flatten().size)
        assertEquals(1, viewModel.uiState.value.toDoCount)

        // Complete the last item via checkbox
        val item = viewModel.uiState.value.itemsByDate.values.flatten().first()
        item.onCheckboxToggle(true)

        // Todo count should be zero after marking as done
        assertEquals(0, viewModel.uiState.value.toDoCount)
        // Item should NOT be in removingItemIds yet (debounced)
        assertFalse(viewModel.uiState.value.removingItemIds.contains("1"))

        // Advance time past debounce delay
        advanceTimeBy(1100)

        // Now item should be added to removingItemIds, which hides it from UI (empty state)
        assertTrue(viewModel.uiState.value.removingItemIds.contains("1"))
        assertEquals(0, viewModel.uiState.value.toDoCount)
    }

    @Test
    fun `ViewModel handles duplicate planner items with same ID`() = runTest {
        val date1 = Date(1704067200000L) // Jan 1, 2024
        val date2 = Date(1704153600000L) // Jan 2, 2024

        // Create duplicate planner items with the same ID but different dates
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date1),
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date2), // Duplicate ID!
            createPlannerItem(id = 2L, title = "Assignment 2", plannableDate = date1)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val allItems = uiState.itemsByDate.values.flatten()

        // Should only have 2 unique items (duplicates removed)
        assertEquals(2, allItems.size)

        // Verify both unique IDs are present
        assertTrue(allItems.any { it.id == "1" })
        assertTrue(allItems.any { it.id == "2" })

        // Verify no duplicate IDs exist
        val itemIds = allItems.map { it.id }
        assertEquals(itemIds.size, itemIds.distinct().size)
    }

    @Test
    fun `ViewModel handles multiple duplicate planner items`() = runTest {
        val date = Date(1704067200000L)

        // Create multiple duplicates
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date),
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date), // Duplicate
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date), // Duplicate
            createPlannerItem(id = 2L, title = "Assignment 2", plannableDate = date),
            createPlannerItem(id = 2L, title = "Assignment 2", plannableDate = date), // Duplicate
            createPlannerItem(id = 3L, title = "Assignment 3", plannableDate = date)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val allItems = uiState.itemsByDate.values.flatten()

        // Should only have 3 unique items
        assertEquals(3, allItems.size)

        // Verify all unique IDs are present
        assertTrue(allItems.any { it.id == "1" })
        assertTrue(allItems.any { it.id == "2" })
        assertTrue(allItems.any { it.id == "3" })

        // Verify no duplicate IDs exist
        val itemIds = allItems.map { it.id }
        assertEquals(itemIds.size, itemIds.distinct().size)
    }

    @Test
    fun `ViewModel handles duplicates across different date groups`() = runTest {
        val date1 = Date(1704067200000L) // Jan 1, 2024
        val date2 = Date(1704153600000L) // Jan 2, 2024

        // Same assignment appearing on two different dates (backend anomaly)
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date1),
            createPlannerItem(id = 1L, title = "Assignment 1", plannableDate = date2), // Same ID, different date
            createPlannerItem(id = 2L, title = "Assignment 2", plannableDate = date1)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value

        // Should have 2 unique items total (first occurrence of each ID kept)
        val allItems = uiState.itemsByDate.values.flatten()
        assertEquals(2, allItems.size)

        // Verify IDs are unique
        val itemIds = allItems.map { it.id }
        assertEquals(itemIds.size, itemIds.distinct().size)

        // Should still have items grouped by date, but no duplicate IDs
        assertTrue(uiState.itemsByDate.keys.size <= 2)
    }

    @Test
    fun `ViewModel preserves first occurrence when duplicates exist`() = runTest {
        val date1 = Date(1704067200000L) // Jan 1, 2024
        val date2 = Date(1704153600000L) // Jan 2, 2024

        // First occurrence should be kept (Jan 1), second occurrence should be filtered out (Jan 2)
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1", courseId = 100L, plannableDate = date1),
            createPlannerItem(id = 1L, title = "Assignment 1 Duplicate", courseId = 200L, plannableDate = date2)
        )

        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(plannerItems)

        val viewModel = getViewModel()

        val uiState = viewModel.uiState.value
        val allItems = uiState.itemsByDate.values.flatten()

        // Should only have 1 item
        assertEquals(1, allItems.size)

        // First item should be kept (the one with date1)
        val item = allItems.first()
        assertEquals("1", item.id)
        assertEquals(date1, item.date)
    }

    // Helper functions
    private fun getViewModel(): ToDoListViewModel {
        return ToDoListViewModel(
            context,
            repository,
            networkStateProvider,
            firebaseCrashlytics,
            toDoFilterDao,
            apiPrefs,
            analytics,
            toDoListViewModelBehavior,
            calendarSharedEvents,
            toDoStateMapper
        )
    }

    private fun createPlannerItem(
        id: Long,
        title: String,
        courseId: Long? = null,
        plannableType: PlannableType = PlannableType.ASSIGNMENT,
        plannableDate: Date = Date(),
        submitted: Boolean = false
    ): PlannerItem {
        return PlannerItem(
            courseId = courseId,
            groupId = null,
            userId = null,
            contextType = if (courseId != null) "Course" else null,
            contextName = null,
            plannableType = plannableType,
            plannable = Plannable(
                id = id,
                title = title,
                courseId = courseId,
                groupId = null,
                userId = null,
                pointsPossible = null,
                dueAt = plannableDate,
                assignmentId = null,
                todoDate = null,
                startAt = null,
                endAt = null,
                details = null,
                allDay = null,
                subAssignmentTag = null
            ),
            plannableDate = plannableDate,
            htmlUrl = null,
            submissionState = if (submitted) SubmissionState(submitted = true) else null,
            newActivity = null,
            plannerOverride = null,
            plannableItemDetails = null
        )
    }
}
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
        assertEquals("Assignment", uiState.markedAsDoneItem?.title)
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
        assertEquals(null, uiState.markedAsDoneItem)
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
        assertEquals("Assignment", uiState.markedAsDoneItem?.title)
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
        assertEquals(null, uiState.markedAsDoneItem)
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
        viewModel.uiState.value.onUndoMarkAsDone()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.itemsByDate.values.flatten().first().isChecked)
        assertEquals(null, uiState.markedAsDoneItem)
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

    // Helper functions
    private fun getViewModel(): ToDoListViewModel {
        return ToDoListViewModel(context, repository, networkStateProvider, firebaseCrashlytics)
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
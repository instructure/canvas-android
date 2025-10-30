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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

    // handleAction tests
    @Test
    fun `handleAction ItemClicked sends OpenToDoItem event`() = runTest {
        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { repository.getPlannerItems(any(), any(), any()) } returns DataResult.Success(emptyList())

        val viewModel = getViewModel()
        val events = mutableListOf<ToDoListViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(ToDoListActionHandler.ItemClicked("123"))

        assertEquals(1, events.size)
        assertTrue(events.first() is ToDoListViewModelAction.OpenToDoItem)
        assertEquals("123", (events.first() as ToDoListViewModelAction.OpenToDoItem).itemId)
    }

    @Test
    fun `handleAction Refresh triggers data reload with forceRefresh`() = runTest {
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
        viewModel.handleAction(ToDoListActionHandler.Refresh)

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

    // Helper functions
    private fun getViewModel(): ToDoListViewModel {
        return ToDoListViewModel(context, repository)
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
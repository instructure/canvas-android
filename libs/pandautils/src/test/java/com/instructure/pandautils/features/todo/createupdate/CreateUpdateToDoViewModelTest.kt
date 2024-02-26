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

package com.instructure.pandautils.features.todo.createupdate

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.Date

@ExperimentalCoroutinesApi
class CreateUpdateToDoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val repository: CreateUpdateToDoRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var viewModel: CreateUpdateToDoViewModel

    private val clock = Clock.fixed(Instant.parse("2024-02-22T11:00:00.00Z"), ZoneId.systemDefault())

    private val plannerItem = PlannerItem(
        courseId = null,
        groupId = null,
        userId = null,
        contextType = null,
        contextName = "Context name",
        plannableType = PlannableType.PLANNER_NOTE,
        plannable = Plannable(
            id = 1,
            title = "Title",
            courseId = 2,
            groupId = null,
            userId = null,
            pointsPossible = null,
            dueAt = null,
            assignmentId = null,
            todoDate = LocalDateTime.now(clock).toApiString(),
            startAt = null,
            endAt = null,
            details = "Description"
        ),
        plannableDate = Date(),
        htmlUrl = null,
        submissionState = null,
        newActivity = false,
        plannerOverride = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Any>(any()) } returns null
        every { apiPrefs.user } returns User(1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initialized with the correct state when creating`() {
        every { savedStateHandle.get<String>(CreateUpdateToDoFragment.INITIAL_DATE) } returns "2024-02-22"

        createViewModel()
        val state = viewModel.uiState.value

        val expectedState = CreateUpdateToDoUiState(
            date = LocalDate.of(2024, 2, 22),
            selectCalendarUiState = SelectCalendarUiState(
                selectedCanvasContext = User(1),
                canvasContexts = listOf(User(1))
            )
        )

        Assert.assertEquals(expectedState, state)
    }

    @Test
    fun `Initialized with the correct state when editing`() {
        every { savedStateHandle.get<PlannerItem>(CreateUpdateToDoFragment.PLANNER_ITEM) } returns plannerItem

        createViewModel()
        val state = viewModel.uiState.value

        val expectedState = CreateUpdateToDoUiState(
            title = "Title",
            date = LocalDate.now(clock),
            time = LocalTime.now(clock),
            details = "Description",
            selectCalendarUiState = SelectCalendarUiState(
                selectedCanvasContext = User(1),
                canvasContexts = listOf(User(1))
            )
        )

        Assert.assertEquals(expectedState, state)
    }

    @Test
    fun `Course list gets fetched`() {
        every { savedStateHandle.get<PlannerItem>(CreateUpdateToDoFragment.PLANNER_ITEM) } returns plannerItem
        val courses = listOf(Course(1), Course(2))
        coEvery { repository.getCourses() } returns courses

        createViewModel()

        coVerify(exactly = 1) { repository.getCourses() }
        Assert.assertEquals(listOf(apiPrefs.user) + courses, viewModel.uiState.value.selectCalendarUiState.canvasContexts)
        Assert.assertEquals(courses.last(), viewModel.uiState.value.selectCalendarUiState.selectedCanvasContext)
    }

    @Test
    fun `Saves new ToDo when creating`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateToDoFragment.INITIAL_DATE) } returns "2024-02-22"
        coEvery { repository.createToDo(any(), any(), any(), any()) } returns Unit

        createViewModel()
        val events = mutableListOf<CreateUpdateToDoViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CreateUpdateToDoAction.UpdateTitle("Title"))
        viewModel.handleAction(CreateUpdateToDoAction.UpdateDate(LocalDate.now(clock)))
        viewModel.handleAction(CreateUpdateToDoAction.UpdateTime(LocalTime.now(clock)))
        viewModel.handleAction(CreateUpdateToDoAction.UpdateCanvasContext(Course(1)))
        viewModel.handleAction(CreateUpdateToDoAction.UpdateDetails("Details"))
        viewModel.handleAction(CreateUpdateToDoAction.Save)

        coVerify(exactly = 1) { repository.createToDo("Title", "Details", "2024-02-22T11:00:00Z", 1) }

        val expectedEvent = CreateUpdateToDoViewModelAction.RefreshCalendarDays(listOf(LocalDate.of(2024, 2, 22)))
        Assert.assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Updates ToDo when editing`() = runTest {
        every { savedStateHandle.get<PlannerItem>(CreateUpdateToDoFragment.PLANNER_ITEM) } returns plannerItem
        coEvery { repository.updateToDo(any(), any(), any(), any(), any()) } returns Unit
        val courses = listOf(Course(1), Course(2))
        coEvery { repository.getCourses() } returns courses

        createViewModel()
        val events = mutableListOf<CreateUpdateToDoViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CreateUpdateToDoAction.UpdateTitle("Updated Title"))
        viewModel.handleAction(CreateUpdateToDoAction.UpdateDate(LocalDate.now(clock).plusDays(1)))
        viewModel.handleAction(CreateUpdateToDoAction.Save)

        coVerify(exactly = 1) { repository.updateToDo(1, "Updated Title", "Description", "2024-02-23T11:00:00Z", 2) }

        val expectedEvent = CreateUpdateToDoViewModelAction.RefreshCalendarDays(
            listOf(
                LocalDate.of(2024, 2, 22),
                LocalDate.of(2024, 2, 23)
            )
        )
        Assert.assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Save ToDo failed`() {
        every { context.getString(R.string.todoSaveErrorMessage) } returns "Failed to save ToDo"
        coEvery { repository.createToDo(any(), any(), any(), any()) } throws Exception()

        createViewModel()

        viewModel.handleAction(CreateUpdateToDoAction.Save)
        Assert.assertEquals("Failed to save ToDo", viewModel.uiState.value.errorSnack)

        viewModel.handleAction(CreateUpdateToDoAction.SnackbarDismissed)
        Assert.assertEquals(null, viewModel.uiState.value.errorSnack)
    }

    @Test
    fun `Check unsaved changes when creating`() {
        every { savedStateHandle.get<String>(CreateUpdateToDoFragment.INITIAL_DATE) } returns "2024-02-22"

        createViewModel()

        viewModel.handleAction(CreateUpdateToDoAction.CheckUnsavedChanges {
            Assert.assertEquals(false, it)
        })

        viewModel.handleAction(CreateUpdateToDoAction.UpdateTitle("Updated Title"))
        viewModel.handleAction(CreateUpdateToDoAction.CheckUnsavedChanges {
            Assert.assertEquals(true, it)
        })
    }

    @Test
    fun `Check unsaved changes when editing`() {
        val courses = listOf(Course(1), Course(2))
        coEvery { repository.getCourses() } returns courses
        every { savedStateHandle.get<PlannerItem>(CreateUpdateToDoFragment.PLANNER_ITEM) } returns plannerItem

        createViewModel()

        viewModel.handleAction(CreateUpdateToDoAction.CheckUnsavedChanges {
            Assert.assertEquals(false, it)
        })

        viewModel.handleAction(CreateUpdateToDoAction.UpdateTitle("Updated Title"))
        viewModel.handleAction(CreateUpdateToDoAction.CheckUnsavedChanges {
            Assert.assertEquals(true, it)
        })
    }

    private fun createViewModel() {
        viewModel = CreateUpdateToDoViewModel(context, savedStateHandle, repository, apiPrefs)
    }
}

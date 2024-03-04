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
package com.instructure.pandautils.features.calendartodo.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment.Companion.PLANNER_ITEM
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ToDoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val toDoRepository: ToDoRepository = mockk(relaxed = true)

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
            courseId = null,
            groupId = null,
            userId = null,
            pointsPossible = null,
            dueAt = null,
            assignmentId = null,
            todoDate = LocalDate.of(2024, 2, 12).atTime(12, 0).toApiString(),
            startAt = null,
            endAt = null,
            details = "Description",
            null
        ),
        plannableDate = Date(),
        htmlUrl = null,
        submissionState = null,
        newActivity = false,
        plannerOverride = null
    )

    lateinit var viewModel: ToDoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { savedStateHandle.get<PlannerItem>(PLANNER_ITEM) } returns plannerItem

        every { context.getString(eq(R.string.calendarAtDateTime), any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} at ${args[1]}"
        }

        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0)

        viewModel = ToDoViewModel(context, savedStateHandle, toDoRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `To do is mapped correctly to state`() {
        val state = viewModel.uiState.value

        val expectedState = ToDoUiState(
            title = "Title",
            contextName = "Context name",
            contextColor = ThemedColor(0).light,
            date = "Feb 12 at 12:00 PM",
            description = "Description"
        )

        Assert.assertEquals(expectedState, state)
    }

    @Test
    fun `Delete ToDo`() = runTest {
        viewModel.handleAction(ToDoAction.DeleteToDo)

        val events = mutableListOf<ToDoViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify(exactly = 1) {
            toDoRepository.deletePlannerNote(plannerItem.plannable.id)
        }

        val expectedEvent = ToDoViewModelAction.RefreshCalendarDay(LocalDate.of(2024, 2, 12))
        Assert.assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Error deleting ToDo`() = runTest {
        every { context.getString(R.string.todoDeleteErrorMessage) } returns "Error deleting to do"
        coEvery { toDoRepository.deletePlannerNote(any()) } throws Exception()

        viewModel.handleAction(ToDoAction.DeleteToDo)

        Assert.assertEquals("Error deleting to do", viewModel.uiState.value.errorSnack)

        viewModel.handleAction(ToDoAction.SnackbarDismissed)
        Assert.assertEquals(null, viewModel.uiState.value.errorSnack)
    }

    @Test
    fun `Open Edit ToDo`() = runTest {
        viewModel.handleAction(ToDoAction.EditToDo)

        val events = mutableListOf<ToDoViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedEvent = ToDoViewModelAction.OpenEditToDo(plannerItem)
        Assert.assertEquals(expectedEvent, events.last())
    }
}

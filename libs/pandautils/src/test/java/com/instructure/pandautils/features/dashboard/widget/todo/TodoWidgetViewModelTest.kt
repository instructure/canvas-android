/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.todo

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.utils.toApiStringSafe
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.calendar.CalendarBodyUiState
import com.instructure.pandautils.compose.composables.calendar.CalendarPageUiState
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.compose.composables.todo.ToDoItemUiState
import com.instructure.pandautils.compose.composables.todo.ToDoItemType
import com.instructure.pandautils.compose.composables.todo.ToDoStateMapper
import com.instructure.pandautils.domain.usecase.courses.LoadAvailableCoursesUseCase
import com.instructure.pandautils.domain.usecase.planner.CreatePlannerOverrideUseCase
import com.instructure.pandautils.domain.usecase.planner.LoadPlannerItemsUseCase
import com.instructure.pandautils.domain.usecase.planner.UpdatePlannerOverrideUseCase
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.WeekFields
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TodoWidgetViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val todoWidgetBehavior: TodoWidgetBehavior = mockk(relaxed = true)
    private val calendarStateMapper: CalendarStateMapper = mockk(relaxed = true)
    private val toDoStateMapper: ToDoStateMapper = mockk(relaxed = true)
    private val loadPlannerItemsUseCase: LoadPlannerItemsUseCase = mockk(relaxed = true)
    private val loadAvailableCoursesUseCase: LoadAvailableCoursesUseCase = mockk(relaxed = true)
    private val updatePlannerOverrideUseCase: UpdatePlannerOverrideUseCase = mockk(relaxed = true)
    private val createPlannerOverrideUseCase: CreatePlannerOverrideUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val calendarSharedEvents: CalendarSharedEvents = mockk(relaxed = true)
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TodoWidgetViewModel
    private val fixedClock = Clock.fixed(
        LocalDate.of(2025, 2, 15).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    )
    private val fixedDate = Date(
        LocalDate.of(2025, 2, 15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    private val sharedEventsFlow = MutableSharedFlow<SharedCalendarAction>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { context.getString(R.string.todoActionOffline) } returns "Offline"
        every { context.getString(R.string.errorUpdatingToDo) } returns "Error"
        every { networkStateProvider.isOnline() } returns true
        every { calendarSharedEvents.events } returns sharedEventsFlow
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig())
        coEvery { loadAvailableCoursesUseCase(any()) } returns emptyList()
        coEvery { loadPlannerItemsUseCase(any()) } returns emptyList()
        every { calendarStateMapper.createBodyUiState(any(), any(), any(), any(), any()) } returns CalendarBodyUiState(
            previousPage = CalendarPageUiState(emptyList(), ""),
            currentPage = CalendarPageUiState(emptyList(), ""),
            nextPage = CalendarPageUiState(emptyList(), "")
        )
        every { crashlytics.recordException(any()) } just Runs

        mockkObject(ColorKeeper)
        every { ColorKeeper.createThemedColor(any()) } returns ThemedColor(0, 0)
        mockkObject(CanvasRestAdapter)
        every { CanvasRestAdapter.clearCacheUrls(any()) } returns Unit
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(): TodoWidgetViewModel {
        return TodoWidgetViewModel(
            context = context,
            todoWidgetBehavior = todoWidgetBehavior,
            calendarStateMapper = calendarStateMapper,
            toDoStateMapper = toDoStateMapper,
            loadPlannerItemsUseCase = loadPlannerItemsUseCase,
            loadAvailableCoursesUseCase = loadAvailableCoursesUseCase,
            updatePlannerOverrideUseCase = updatePlannerOverrideUseCase,
            createPlannerOverrideUseCase = createPlannerOverrideUseCase,
            networkStateProvider = networkStateProvider,
            calendarSharedEvents = calendarSharedEvents,
            observeGlobalConfigUseCase = observeGlobalConfigUseCase,
            crashlytics = crashlytics,
            clock = fixedClock
        )
    }

    @Test
    fun `data fetching loads items successfully`() = runTest {
        val plannerItem1 = createPlannerItem(id = 1L, title = "Assignment 1")
        val plannerItem2 = createPlannerItem(id = 2L, title = "Quiz 1")

        val weekField = WeekFields.of(Locale.getDefault())
        val currentWeekStart = LocalDate.of(2025, 2, 15).with(weekField.dayOfWeek(), 1)
        val currentWeekEnd = currentWeekStart.plusDays(6)

        // Catch-all first, then specific mock (last one wins in MockK)
        coEvery { loadPlannerItemsUseCase(any()) } returns emptyList()

        coEvery {
            loadPlannerItemsUseCase(
                match {
                    it.startDate == currentWeekStart.atStartOfDay().toApiStringSafe() &&
                    it.endDate == currentWeekEnd.atTime(23, 59, 59).toApiStringSafe()
                }
            )
        } returns listOf(plannerItem1, plannerItem2)

        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.todosLoading)
        assertFalse(viewModel.uiState.value.todosError)
        assertEquals(2, viewModel.uiState.value.todos.size)
        assertEquals("Assignment 1", viewModel.uiState.value.todos[0].title)
        assertEquals("Quiz 1", viewModel.uiState.value.todos[1].title)
    }

    @Test
    fun `data fetching shows error state on failure`() = runTest {
        val weekField = WeekFields.of(Locale.getDefault())
        val currentWeekStart = LocalDate.of(2025, 2, 15).with(weekField.dayOfWeek(), 1)
        val currentWeekEnd = currentWeekStart.plusDays(6)

        // Catch-all first, then specific mock (last one wins in MockK)
        coEvery { loadPlannerItemsUseCase(any()) } returns emptyList()

        coEvery {
            loadPlannerItemsUseCase(
                match {
                    it.startDate == currentWeekStart.atStartOfDay().toApiStringSafe() &&
                    it.endDate == currentWeekEnd.atTime(23, 59, 59).toApiStringSafe()
                }
            )
        } throws Exception("Network error")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.todosLoading)
        assertTrue(viewModel.uiState.value.todosError)
        assertEquals(0, viewModel.uiState.value.todos.size)
    }

    @Test
    fun `data fetching shows empty state when no items`() = runTest {
        coEvery { loadPlannerItemsUseCase(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.todosLoading)
        assertFalse(viewModel.uiState.value.todosError)
        assertEquals(0, viewModel.uiState.value.todos.size)
    }

    @Test
    fun `onTodoClick delegates to behavior`() = runTest {
        viewModel = createViewModel()
        val activity = mockk<FragmentActivity>()
        val htmlUrl = "https://instructure.com/test"

        viewModel.uiState.value.onTodoClick(activity, htmlUrl)

        verify { todoWidgetBehavior.onTodoClick(activity, htmlUrl) }
    }

    @Test
    fun `onAddTodoClick delegates to behavior with selected date`() = runTest {
        viewModel = createViewModel()
        val activity = mockk<FragmentActivity>()

        viewModel.uiState.value.onAddTodoClick(activity)

        verify { todoWidgetBehavior.onAddTodoClick(activity, any()) }
    }

    @Test
    fun `toggleShowCompleted updates showCompleted state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCompleted)

        viewModel.uiState.value.onToggleShowCompleted()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCompleted)

        viewModel.uiState.value.onToggleShowCompleted()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCompleted)
    }

    @Test
    fun `onDaySelected updates selected day`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialDay = viewModel.uiState.value.selectedDay
        val newDay = LocalDate.of(2025, 2, 20)

        viewModel.uiState.value.onDaySelected(newDay)
        advanceUntilIdle()

        assertEquals(newDay, viewModel.uiState.value.selectedDay)
        assertTrue(initialDay != newDay)
    }

    @Test
    fun `onNavigateWeek updates scrollToPageOffset`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.scrollToPageOffset)

        viewModel.uiState.value.onNavigateWeek(2)

        assertEquals(2, viewModel.uiState.value.scrollToPageOffset)
    }

    @Test
    fun `onPageChanged updates selected day and loads adjacent week`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialDay = viewModel.uiState.value.selectedDay

        viewModel.uiState.value.onPageChanged(1)
        advanceUntilIdle()

        val newDay = viewModel.uiState.value.selectedDay
        assertEquals(initialDay.plusWeeks(1), newDay)
        coVerify(atLeast = 1) { loadPlannerItemsUseCase(any()) }
    }

    @Test
    fun `onPageChanged with negative offset moves to previous week`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialDay = viewModel.uiState.value.selectedDay

        viewModel.uiState.value.onPageChanged(-1)
        advanceUntilIdle()

        val newDay = viewModel.uiState.value.selectedDay
        assertEquals(initialDay.minusWeeks(1), newDay)
    }

    @Test
    fun `onPageChanged with zero offset does nothing`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialDay = viewModel.uiState.value.selectedDay

        viewModel.uiState.value.onPageChanged(0)
        advanceUntilIdle()

        assertEquals(initialDay, viewModel.uiState.value.selectedDay)
    }

    @Test
    fun `handleSwipeToDone marks item as complete`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Test Item")
        val plannerOverride = PlannerOverride(
            id = 100L,
            plannableId = 1L,
            plannableType = PlannableType.ASSIGNMENT,
            markedComplete = true
        )

        coEvery { loadPlannerItemsUseCase(any()) } returns listOf(plannerItem)
        coEvery { createPlannerOverrideUseCase(any()) } returns plannerOverride
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val todoItem = viewModel.uiState.value.todos.firstOrNull()
        todoItem?.onSwipeToDone?.invoke()
        advanceUntilIdle()

        coVerify { createPlannerOverrideUseCase(any()) }
        assertEquals("Test Item", viewModel.uiState.value.confirmationSnackbarData?.title)
        assertTrue(viewModel.uiState.value.confirmationSnackbarData?.markedAsDone == true)
    }

    @Test
    fun `handleSwipeToDone when offline shows offline message`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Test Item")
        coEvery { loadPlannerItemsUseCase(any()) } returns listOf(plannerItem)
        every { networkStateProvider.isOnline() } returns false
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val todoItem = viewModel.uiState.value.todos.firstOrNull()
        todoItem?.onSwipeToDone?.invoke()
        advanceUntilIdle()

        assertEquals("Offline", viewModel.uiState.value.snackbarMessage)
        coVerify(exactly = 0) { createPlannerOverrideUseCase(any()) }
    }

    @Test
    fun `handleCheckboxToggle when offline shows offline message`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Test Item")
        coEvery { loadPlannerItemsUseCase(any()) } returns listOf(plannerItem)
        every { networkStateProvider.isOnline() } returns false
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val todoItem = viewModel.uiState.value.todos.firstOrNull()
        todoItem?.onCheckboxToggle?.invoke(true)
        advanceUntilIdle()

        assertEquals("Offline", viewModel.uiState.value.snackbarMessage)
        coVerify(exactly = 0) { createPlannerOverrideUseCase(any()) }
    }

    @Test
    fun `handleUndoMarkAsDoneUndone reverts completion`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Test Item")
        val plannerOverride = PlannerOverride(
            id = 100L,
            plannableId = 1L,
            plannableType = PlannableType.ASSIGNMENT,
            markedComplete = true
        )

        coEvery { loadPlannerItemsUseCase(any()) } returns listOf(plannerItem)
        coEvery { createPlannerOverrideUseCase(any()) } returns plannerOverride
        coEvery { updatePlannerOverrideUseCase(any()) } returns plannerOverride.copy(markedComplete = false)
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val todoItem = viewModel.uiState.value.todos.firstOrNull()
        todoItem?.onSwipeToDone?.invoke()
        advanceUntilIdle()

        viewModel.uiState.value.onUndoMarkAsDoneUndone("1", true)
        advanceUntilIdle()

        coVerify { updatePlannerOverrideUseCase(any()) }
        assertEquals(null, viewModel.uiState.value.confirmationSnackbarData)
    }

    @Test
    fun `updateItemCompleteState creates planner override when none exists`() = runTest {
        val plannerItem = createPlannerItem(id = 1L, title = "Test Item")
        val plannerOverride = PlannerOverride(
            id = 100L,
            plannableId = 1L,
            plannableType = PlannableType.ASSIGNMENT,
            markedComplete = true
        )

        coEvery { loadPlannerItemsUseCase(any()) } returns listOf(plannerItem)
        coEvery { createPlannerOverrideUseCase(any()) } returns plannerOverride
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val todoItem = viewModel.uiState.value.todos.firstOrNull()
        todoItem?.onSwipeToDone?.invoke()
        advanceUntilIdle()

        coVerify { createPlannerOverrideUseCase(any()) }
        coVerify(exactly = 0) { updatePlannerOverrideUseCase(any()) }
    }

    @Test
    fun `clearSnackbarMessage clears snackbar`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onSnackbarDismissed()

        assertEquals(null, viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `clearMarkedAsDoneItem clears confirmation snackbar`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onMarkedAsDoneSnackbarDismissed()

        assertEquals(null, viewModel.uiState.value.confirmationSnackbarData)
    }

    @Test
    fun `onToDoCountUpdated resets updateToDoCount flag`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onToDoCountUpdated()

        assertFalse(viewModel.uiState.value.updateToDoCount)
    }

    @Test
    fun `refresh reloads visible weeks with force refresh`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onRefresh()
        advanceUntilIdle()

        coVerify(atLeast = 1) { loadPlannerItemsUseCase(match { it.forceNetwork }) }
    }

    @Test
    fun `observeConfig updates color when config changes`() = runTest {
        val testColor = 0xFF00FF00.toInt()
        val themedColor = ThemedColor(testColor, testColor)
        every { ColorKeeper.createThemedColor(testColor) } returns themedColor
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig(backgroundColor = testColor))

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(themedColor, viewModel.uiState.value.color)
    }

    @Test
    fun `calendar shared events triggers refresh`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        sharedEventsFlow.emit(SharedCalendarAction.RefreshToDoList)
        advanceUntilIdle()

        coVerify(atLeast = 1) { loadPlannerItemsUseCase(match { it.forceNetwork }) }
    }

    @Test
    fun `calendar shared event SelectDay with fromTodoList false selects day in same week`() = runTest {
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val initialSelectedDay = viewModel.uiState.value.selectedDay
        val newDay = LocalDate.of(2025, 2, 13)

        sharedEventsFlow.emit(SharedCalendarAction.SelectDay(newDay, fromTodoList = false))
        advanceUntilIdle()

        assertEquals(newDay, viewModel.uiState.value.selectedDay)
        assertTrue(initialSelectedDay != newDay)
    }

    @Test
    fun `calendar shared event SelectDay with fromTodoList false selects day in different week`() = runTest {
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val initialSelectedDay = viewModel.uiState.value.selectedDay
        val futureDay = LocalDate.of(2025, 2, 25)

        sharedEventsFlow.emit(SharedCalendarAction.SelectDay(futureDay, fromTodoList = false))
        advanceUntilIdle()

        // Should have scroll offset since day is in different week
        assertEquals(1, viewModel.uiState.value.scrollToPageOffset)

        // Simulate page change to complete the navigation
        viewModel.uiState.value.onPageChanged(1)
        advanceUntilIdle()

        assertEquals(futureDay, viewModel.uiState.value.selectedDay)
        assertTrue(initialSelectedDay != futureDay)
    }

    @Test
    fun `calendar shared event SelectDay with fromTodoList true does not select day`() = runTest {
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val initialSelectedDay = viewModel.uiState.value.selectedDay
        val newDay = LocalDate.of(2025, 2, 17)

        sharedEventsFlow.emit(SharedCalendarAction.SelectDay(newDay, fromTodoList = true))
        advanceUntilIdle()

        // Should still be on the initial day
        assertEquals(initialSelectedDay, viewModel.uiState.value.selectedDay)
        assertEquals(0, viewModel.uiState.value.scrollToPageOffset)
    }

    @Test
    fun `showCompleted filter shows completed items`() = runTest {
        val completedItem = createPlannerItem(id = 1L, title = "Completed", isComplete = true)
        val incompleteItem = createPlannerItem(id = 2L, title = "Incomplete", isComplete = false)

        // Calculate week dates the same way as ViewModel
        val weekField = WeekFields.of(Locale.getDefault())
        val currentWeekStart = LocalDate.of(2025, 2, 15).with(weekField.dayOfWeek(), 1)
        val currentWeekEnd = currentWeekStart.plusDays(6)
        val previousWeekStart = currentWeekStart.minusWeeks(1)
        val previousWeekEnd = previousWeekStart.plusDays(6)
        val nextWeekStart = currentWeekStart.plusWeeks(1)
        val nextWeekEnd = nextWeekStart.plusDays(6)

        // Mock for the current week containing 2025-02-15
        coEvery {
            loadPlannerItemsUseCase(
                match {
                    it.startDate == currentWeekStart.atStartOfDay().toApiStringSafe() &&
                    it.endDate == currentWeekEnd.atTime(23, 59, 59).toApiStringSafe()
                }
            )
        } returns listOf(completedItem, incompleteItem)

        // Mock for previous and next weeks to return empty
        coEvery {
            loadPlannerItemsUseCase(
                match {
                    it.startDate == previousWeekStart.atStartOfDay().toApiStringSafe() &&
                    it.endDate == previousWeekEnd.atTime(23, 59, 59).toApiStringSafe()
                }
            )
        } returns emptyList()

        coEvery {
            loadPlannerItemsUseCase(
                match {
                    it.startDate == nextWeekStart.atStartOfDay().toApiStringSafe() &&
                    it.endDate == nextWeekEnd.atTime(23, 59, 59).toApiStringSafe()
                }
            )
        } returns emptyList()

        every { toDoStateMapper.mapToUiState(eq(completedItem), any(), any(), any()) } answers {
            createToDoItemUiState(completedItem, thirdArg(), arg(3))
        }
        every { toDoStateMapper.mapToUiState(eq(incompleteItem), any(), any(), any()) } answers {
            createToDoItemUiState(incompleteItem, thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        // Initially showCompleted is false, so only incomplete items
        assertEquals(1, viewModel.uiState.value.todos.size)
        assertEquals("Incomplete", viewModel.uiState.value.todos[0].title)

        // Toggle to show completed
        viewModel.uiState.value.onToggleShowCompleted()
        advanceUntilIdle()

        // Now both items should be visible
        assertEquals(2, viewModel.uiState.value.todos.size)
    }

    @Test
    fun `jump to today when already on today does nothing`() = runTest {
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val initialSelectedDay = viewModel.uiState.value.selectedDay
        val initialScrollOffset = viewModel.uiState.value.scrollToPageOffset

        // Jump to today when already on today (2025-02-15)
        viewModel.uiState.value.onJumpToToday()
        advanceUntilIdle()

        // Should remain on the same day with no scroll offset
        assertEquals(initialSelectedDay, viewModel.uiState.value.selectedDay)
        assertEquals(initialScrollOffset, viewModel.uiState.value.scrollToPageOffset)
    }

    @Test
    fun `jump to today when in current week but not on today selects today`() = runTest {
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        // Select a different day in the same week (2025-02-12)
        val mondayInSameWeek = LocalDate.of(2025, 2, 12)
        viewModel.uiState.value.onDaySelected(mondayInSameWeek)
        advanceUntilIdle()

        assertEquals(mondayInSameWeek, viewModel.uiState.value.selectedDay)

        // Jump to today
        viewModel.uiState.value.onJumpToToday()
        advanceUntilIdle()

        // Should be on today with no scroll animation
        assertEquals(LocalDate.of(2025, 2, 15), viewModel.uiState.value.selectedDay)
        assertEquals(0, viewModel.uiState.value.scrollToPageOffset)
    }

    @Test
    fun `jump to today from future week animates backward`() = runTest {
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        // Navigate to 2 weeks in the future
        viewModel.uiState.value.onNavigateWeek(1)
        advanceUntilIdle()
        viewModel.uiState.value.onPageChanged(1)
        advanceUntilIdle()

        viewModel.uiState.value.onNavigateWeek(1)
        advanceUntilIdle()
        viewModel.uiState.value.onPageChanged(1)
        advanceUntilIdle()

        val futureDate = viewModel.uiState.value.selectedDay
        assertTrue(futureDate.isAfter(LocalDate.of(2025, 2, 15)))

        // Jump to today
        viewModel.uiState.value.onJumpToToday()
        advanceUntilIdle()

        // Should have negative scroll offset (backward)
        assertEquals(-1, viewModel.uiState.value.scrollToPageOffset)

        // Selected day should be positioned so next page contains today's week
        // (today + 1 week = future week)
        assertTrue(viewModel.uiState.value.selectedDay.isAfter(LocalDate.of(2025, 2, 15)))

        // Simulate page change
        viewModel.uiState.value.onPageChanged(-1)
        advanceUntilIdle()

        // After animation, should be on today
        assertEquals(LocalDate.of(2025, 2, 15), viewModel.uiState.value.selectedDay)
        assertEquals(0, viewModel.uiState.value.scrollToPageOffset)
    }

    @Test
    fun `jump to today from past week animates forward`() = runTest {
        every { toDoStateMapper.mapToUiState(any(), any(), any(), any()) } answers {
            createToDoItemUiState(firstArg(), thirdArg(), arg(3))
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        // Navigate to 2 weeks in the past
        viewModel.uiState.value.onNavigateWeek(-1)
        advanceUntilIdle()
        viewModel.uiState.value.onPageChanged(-1)
        advanceUntilIdle()

        viewModel.uiState.value.onNavigateWeek(-1)
        advanceUntilIdle()
        viewModel.uiState.value.onPageChanged(-1)
        advanceUntilIdle()

        val pastDate = viewModel.uiState.value.selectedDay
        assertTrue(pastDate.isBefore(LocalDate.of(2025, 2, 15)))

        // Jump to today
        viewModel.uiState.value.onJumpToToday()
        advanceUntilIdle()

        // Should have positive scroll offset (forward)
        assertEquals(1, viewModel.uiState.value.scrollToPageOffset)

        // Selected day should be positioned so previous page contains today's week
        // (today - 1 week = past week)
        assertTrue(viewModel.uiState.value.selectedDay.isBefore(LocalDate.of(2025, 2, 15)))

        // Simulate page change
        viewModel.uiState.value.onPageChanged(1)
        advanceUntilIdle()

        // After animation, should be on today
        assertEquals(LocalDate.of(2025, 2, 15), viewModel.uiState.value.selectedDay)
        assertEquals(0, viewModel.uiState.value.scrollToPageOffset)
    }

    @Test
    fun `yearTitle is null when selected date is in current year`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.yearTitle)
    }

    @Test
    fun `yearTitle updates when navigating to different year`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialYearTitle = viewModel.uiState.value.yearTitle
        assertEquals(null, initialYearTitle)

        // Navigate back many weeks to reach a different year (e.g., 2024)
        repeat(10) {
            viewModel.uiState.value.onNavigateWeek(-1)
            viewModel.uiState.value.onPageChanged(-1)
            advanceUntilIdle()
        }

        val newYearTitle = viewModel.uiState.value.yearTitle
        assertEquals("2024", newYearTitle)
    }

    private fun createPlannerItem(
        id: Long,
        title: String,
        isComplete: Boolean = false
    ): PlannerItem {
        return PlannerItem(
            courseId = null,
            groupId = null,
            userId = null,
            contextType = null,
            contextName = null,
            plannableType = PlannableType.ASSIGNMENT,
            plannable = Plannable(
                id = id,
                title = title,
                courseId = null,
                groupId = null,
                userId = null,
                pointsPossible = null,
                dueAt = fixedDate,
                assignmentId = null,
                todoDate = null,
                startAt = null,
                endAt = null,
                details = null,
                allDay = null,
                subAssignmentTag = null
            ),
            plannableDate = fixedDate,
            htmlUrl = null,
            submissionState = if (isComplete) mockk(relaxed = true) { every { submitted } returns true } else null,
            newActivity = null,
            plannerOverride = if (isComplete) PlannerOverride(
                id = 99L,
                plannableId = id,
                plannableType = PlannableType.ASSIGNMENT,
                markedComplete = true
            ) else null,
            plannableItemDetails = null
        )
    }

    private fun createToDoItemUiState(
        plannerItem: PlannerItem,
        onSwipeToDone: () -> Unit,
        onCheckboxToggle: (Boolean) -> Unit
    ): ToDoItemUiState {
        return ToDoItemUiState(
            id = plannerItem.plannable.id.toString(),
            title = plannerItem.plannable.title,
            date = fixedDate,
            dateLabel = "Date",
            contextLabel = "",
            canvasContext = plannerItem.canvasContext,
            itemType = ToDoItemType.ASSIGNMENT,
            isChecked = plannerItem.plannerOverride?.markedComplete == true,
            iconRes = 0,
            tag = "",
            htmlUrl = "",
            isClickable = true,
            onSwipeToDone = onSwipeToDone,
            onCheckboxToggle = onCheckboxToggle
        )
    }
}
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
package com.instructure.pandautils.features.calendar

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Calendar
import java.util.Date


@ExperimentalCoroutinesApi
class CalendarViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val calendarRepository: CalendarRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var viewModel: CalendarViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(eq(R.string.calendarDueDate), any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "Due ${args[0]} ${args[1]}"
        }

        every { context.getString(eq(R.string.calendarDate), any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} ${args[1]}"
        }

        every { context.getString(eq(R.string.calendarEventDate), any(), any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} ${args[1]} - ${args[2]}"
        }

        every { context.getString(eq(R.string.courseToDo), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} To Do"
        }

        every { context.getString(R.string.calendarRefreshFailed) } returns "Error refreshing events"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Calendar is initialized with the correct state`() {
        initViewModel()
        val initialState = viewModel.uiState.value

        val expectedState = CalendarUiState(LocalDate.now(clock), true)

        assertEquals(expectedState, initialState)
    }

    @Test
    fun `Events are fetched for the correct date initially`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val previousMonthStartDate = LocalDate.of(2023, 3, 1).atStartOfDay().toApiString()
        val previousMonthEndDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentStartDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentMonthEndDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthStartDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthEndDate = LocalDate.of(2023, 6, 1).atStartOfDay().toApiString()

        coVerify { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, emptyList(), true) }
        coVerify { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, emptyList(), true) }
        coVerify { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, emptyList(), true) }
    }

    @Test
    fun `Error state is set when fetching events fails`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } throws Exception()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), error = true),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), error = true),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21), error = true)
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Calendar events are showed for the correct days`() {
        val events = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
            createPlannerItem(1, 2, PlannableType.QUIZ, createDate(2023, 4, 20, 12)),
            createPlannerItem(
                1,
                3,
                PlannableType.CALENDAR_EVENT,
                createDate(2023, 4, 19, 12),
                startAt = createDate(2023, 4, 19, 12),
                endAt = createDate(2023, 4, 19, 13)
            ),
            createPlannerItem(2, 4, PlannableType.DISCUSSION_TOPIC, createDate(2023, 4, 19, 12)),
            createPlannerItem(2, 5, PlannableType.PLANNER_NOTE, createDate(2023, 4, 21, 12)),
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        initViewModel()

        val expectedCurrentEvents = listOf(
            EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM"),
            EventUiState(2, "Course 1", Course(1), "Plannable 2", R.drawable.ic_quiz, "Due Apr 20 12:00 PM")
        )
        val expectedPreviousEvents = listOf(
            EventUiState(3, "Course 1", Course(1), "Plannable 3", R.drawable.ic_calendar, "Apr 19 12:00 PM - 1:00 PM"),
            EventUiState(4, "Course 2", Course(2), "Plannable 4", R.drawable.ic_discussion, "Due Apr 19 12:00 PM")
        )
        val expectedNextEvents = listOf(
            EventUiState(5, "Course 2 To Do", Course(2), "Plannable 5", R.drawable.ic_todo, "Apr 21 12:00 PM"),
        )
        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expectedPreviousEvents),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedCurrentEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21), events = expectedNextEvents)
            ), eventIndicators = mapOf(LocalDate.of(2023, 4, 20) to 2, LocalDate.of(2023, 4, 19) to 2, LocalDate.of(2023, 4, 21) to 1)
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Do not load months again when selected day is changed but month is already loaded`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2023, 4, 19)))

        val previousMonthStartDate = LocalDate.of(2023, 3, 1).atStartOfDay().toApiString()
        val previousMonthEndDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentStartDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentMonthEndDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthStartDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthEndDate = LocalDate.of(2023, 6, 1).atStartOfDay().toApiString()

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, emptyList(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, emptyList(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, emptyList(), true) }
    }

    @Test
    fun `Load new month when selected day is changed to a different month`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val previousMonthStartDate = LocalDate.of(2023, 3, 1).atStartOfDay().toApiString()
        val previousMonthEndDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentStartDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentMonthEndDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthStartDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthEndDate = LocalDate.of(2023, 6, 1).atStartOfDay().toApiString()

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, emptyList(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, emptyList(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, emptyList(), true) }

        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2023, 5, 19)))

        val newMonthStartDate = LocalDate.of(2023, 6, 1).atStartOfDay().toApiString()
        val newMonthEndDate = LocalDate.of(2023, 7, 1).atStartOfDay().toApiString()

        coVerify { calendarRepository.getPlannerItems(newMonthStartDate!!, newMonthEndDate!!, emptyList(), true) }
    }

    @Test
    fun `Update state when new day is selected`() {
        val events = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
            createPlannerItem(2, 4, PlannableType.DISCUSSION_TOPIC, createDate(2023, 4, 19, 12)),
            createPlannerItem(2, 5, PlannableType.PLANNER_NOTE, createDate(2023, 4, 21, 12)),
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        initViewModel()

        val expected20thEvents = listOf(
            EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM"),
        )
        val expected19thEvents = listOf(
            EventUiState(4, "Course 2", Course(2), "Plannable 4", R.drawable.ic_discussion, "Due Apr 19 12:00 PM")
        )
        val expected21stEvents = listOf(
            EventUiState(5, "Course 2 To Do", Course(2), "Plannable 5", R.drawable.ic_todo, "Apr 21 12:00 PM"),
        )
        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expected19thEvents),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expected20thEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21), events = expected21stEvents)
            ), eventIndicators = mapOf(LocalDate.of(2023, 4, 20) to 1, LocalDate.of(2023, 4, 19) to 1, LocalDate.of(2023, 4, 21) to 1)
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2023, 4, 19)))

        val expectedNewState = CalendarUiState(
            LocalDate.of(2023, 4, 19), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 18)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expected19thEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expected20thEvents)
            ), eventIndicators = mapOf(LocalDate.of(2023, 4, 20) to 1, LocalDate.of(2023, 4, 19) to 1, LocalDate.of(2023, 4, 21) to 1)
        )

        assertEquals(expectedNewState, viewModel.uiState.value)
    }

    @Test
    fun `Change expanded state when expand changes`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandChanged)
        assertEquals(expectedState.copy(expanded = false), viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandChanged)
        assertEquals(expectedState.copy(expanded = true), viewModel.uiState.value)
    }

    @Test
    fun `Change expanded state to false when expand is disabled`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandDisabled)
        assertEquals(expectedState.copy(expanded = false), viewModel.uiState.value)
    }

    @Test
    fun `Select today when jump to today is tapped`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        // Select an other day
        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2022, 2, 19)))
        val newDayExpectedState = CalendarUiState(
            LocalDate.of(2022, 2, 19), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2022, 2, 18)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2022, 2, 19)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2022, 2, 20))
            )
        )
        assertEquals(newDayExpectedState, viewModel.uiState.value)

        // Tap on today
        viewModel.handleAction(CalendarAction.TodayTapped)
        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Go to next month when calendar is expanded and page is swiped`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.PageChanged(1))
        val newExpectedState = CalendarUiState(
            LocalDate.of(2023, 5, 20), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 5, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 5, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 5, 21))
            )
        )
        assertEquals(newExpectedState, viewModel.uiState.value)
    }

    @Test
    fun `Go to next week when calendar is not expanded and page is swiped`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandChanged)
        viewModel.handleAction(CalendarAction.PageChanged(1))

        val newExpectedState = CalendarUiState(
            LocalDate.of(2023, 4, 27), false, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 26)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 27)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 28))
            )
        )
        assertEquals(newExpectedState, viewModel.uiState.value)
    }

    @Test
    fun `Go to next day when event page is changed`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.EventPageChanged(1))

        val newExpectedState = CalendarUiState(
            LocalDate.of(2023, 4, 21), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 22))
            )
        )
        assertEquals(newExpectedState, viewModel.uiState.value)
    }

    @Test
    fun `Get planner items for the selected day when day is refreshed`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        viewModel.handleAction(CalendarAction.RefreshDay(LocalDate.of(2023, 4, 20)))

        val startDate = LocalDate.of(2023, 4, 20).atStartOfDay().toApiString()
        val endDate = LocalDate.of(2023, 4, 21).atStartOfDay().toApiString()

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(startDate!!, endDate!!, emptyList(), true) }
    }

    @Test
    fun `Update items after refresh`() {
        val startDate = LocalDate.of(2023, 4, 20).atStartOfDay().toApiString()
        val endDate = LocalDate.of(2023, 4, 21).atStartOfDay().toApiString()

        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        coEvery { calendarRepository.getPlannerItems(startDate!!, endDate!!, any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
        )
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.RefreshDay(LocalDate.of(2023, 4, 20)))

        val expectedEvents = listOf(
            EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM"),
        )

        val newExpectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            ), eventIndicators = mapOf(LocalDate.of(2023, 4, 20) to 1)
        )

        assertEquals(newExpectedState, viewModel.uiState.value)
    }

    @Test
    fun `Show error snackbar when refresh failed and update state after snackbar is dismissed`() {
        val startDate = LocalDate.of(2023, 4, 20).atStartOfDay().toApiString()
        val endDate = LocalDate.of(2023, 4, 21).atStartOfDay().toApiString()

        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        coEvery { calendarRepository.getPlannerItems(startDate!!, endDate!!, any(), any()) } throws IllegalStateException()
        initViewModel()

        val expectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.RefreshDay(LocalDate.of(2023, 4, 20)))

        val newExpectedState = CalendarUiState(
            LocalDate.now(clock), true, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            ), snackbarMessage = "Error refreshing events"
        )

        assertEquals(newExpectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.SnackbarDismissed)
        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Open assignment when assignment is selected`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val expectedAction = CalendarViewModelAction.OpenAssignment(Course(1), 1)
        assertEquals(expectedAction, viewModel.events.value.peekContent())
    }

    @Test
    fun `Open discussion when discussion is selected`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.DISCUSSION_TOPIC, createDate(2023, 4, 20, 12)),
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val expectedAction = CalendarViewModelAction.OpenDiscussion(Course(1), 1)
        assertEquals(expectedAction, viewModel.events.value.peekContent())
    }

    @Test
    fun `Open assignment when an assignment quiz is selected`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.QUIZ, createDate(2023, 4, 20, 12)).copy(htmlUrl = "http://quiz.com"),
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val expectedAction = CalendarViewModelAction.OpenAssignment(Course(1), 1)
        assertEquals(expectedAction, viewModel.events.value.peekContent())
    }

    @Test
    fun `Open quiz when a quiz is selected that is not an assignment`() {
        val plannerItem = createPlannerItem(1, 1, PlannableType.QUIZ, createDate(2023, 4, 20, 12))
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            plannerItem.copy(htmlUrl = "http://quiz.com", plannable = plannerItem.plannable.copy(assignmentId = null))
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val expectedAction = CalendarViewModelAction.OpenQuiz(Course(1), "http://quiz.com")
        assertEquals(expectedAction, viewModel.events.value.peekContent())
    }

    @Test
    fun `Open calendar event when calendar event is selected`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.CALENDAR_EVENT, createDate(2023, 4, 20, 12)),
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val expectedAction = CalendarViewModelAction.OpenCalendarEvent(Course(1), 1)
        assertEquals(expectedAction, viewModel.events.value.peekContent())
    }

    private fun initViewModel() {
        viewModel = CalendarViewModel(context, calendarRepository, apiPrefs, clock)
    }

    private fun createPlannerItem(
        courseId: Long,
        plannableId: Long,
        plannableType: PlannableType,
        date: Date,
        submissionState: SubmissionState? = null,
        pointsPossible: Double? = null,
        startAt: Date? = null,
        endAt: Date? = null
    ): PlannerItem {
        val plannable = Plannable(
            id = plannableId,
            title = "Plannable $plannableId",
            courseId,
            null,
            null,
            pointsPossible,
            date,
            plannableId,
            date.toApiString(),
            startAt,
            endAt
        )
        return PlannerItem(
            courseId,
            null,
            null,
            null,
            "Course $courseId",
            plannableType,
            plannable,
            date,
            null,
            submissionState,
            plannerOverride = null,
            newActivity = null
        )
    }

    private fun createDate(year: Int, month: Int, day: Int, hour: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        return calendar.time
    }
}
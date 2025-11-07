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
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerItemDetails
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.appointmentgroups.domain.usecase.CancelReservationUseCase
import com.instructure.pandautils.features.appointmentgroups.domain.usecase.GetAppointmentGroupsUseCase
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    private val calendarPrefs: CalendarPrefs = mockk(relaxed = true)
    private val calendarStateMapper: CalendarStateMapper = mockk(relaxed = true)
    private val calendarSharedEvents: CalendarSharedEvents = mockk(relaxed = true)
    private val calendarBehavior: CalendarBehavior = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val cancelReservationUseCase: CancelReservationUseCase = mockk(relaxed = true)
    private val getAppointmentGroupsUseCase: GetAppointmentGroupsUseCase = mockk(relaxed = true)

    private lateinit var viewModel: CalendarViewModel

    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    private val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())

    private val baseCalendarUiState = CalendarUiState(
        selectedDay = LocalDate.now(clock),
        expanded = false,
        headerUiState = CalendarHeaderUiState("2023", "April"),
        bodyUiState = CalendarBodyUiState(
            previousPage = CalendarPageUiState(emptyList(), ""),
            currentPage = CalendarPageUiState(emptyList(), ""),
            nextPage = CalendarPageUiState(emptyList(), "")
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(eq(R.string.calendarDueDate), any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "Due ${args[0]} ${args[1]}"
        }

        every { context.getString(eq(R.string.calendarAtDateTime), any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} ${args[1]}"
        }

        every { context.getString(eq(R.string.calendarFromTo), any(), any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} ${args[1]} - ${args[2]}"
        }

        every { context.getString(eq(R.string.courseToDo), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} To Do"
        }

        every { context.getString(eq(R.string.reply_to_topic)) } answers {
            "Reply to topic"
        }

        every { context.getString(eq(R.string.additional_replies), any()) } answers {
            val args = secondArg<Array<Any>>()
            "Additional replies (${args[0]})"
        }

        every { context.getString(R.string.calendarRefreshFailed) } returns "Error refreshing events"
        every { context.getString(R.string.calendarEventExcused) } returns "excused"
        every { context.getString(R.string.calendarEventMissing) } returns "missing"
        every { context.getString(R.string.calendarEventGraded) } returns "graded"
        every { context.getString(R.string.calendarEventSubmitted) } returns "needs grading"
        every { context.getString(R.string.userCalendarToDo) } returns "To Do"
        every { context.getString(eq(R.string.calendarEventPoints), any()) } answers {
            val args = secondArg<Array<Any>>()
            "${args[0]} pts"
        }

        // We don't care about these states in the tests because theses are tested in the CalendarStateMapperTest
        every { calendarStateMapper.createHeaderUiState(any(), any()) } returns CalendarHeaderUiState("2023", "April")
        every { calendarStateMapper.createBodyUiState(any(), any(), any(), any(), any()) } returns CalendarBodyUiState(
            previousPage = CalendarPageUiState(emptyList(), ""),
            currentPage = CalendarPageUiState(emptyList(), ""),
            nextPage = CalendarPageUiState(emptyList(), "")
        )

        coEvery { calendarRepository.getCalendarFilterLimit() } returns -1
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            1,
            "",
            "1",
            filters = setOf("course_1", "course_2", "group_3", "group_4", "user_5")
        )

        coEvery { calendarBehavior.shouldShowAddEventButton() } returns true

        every { savedStateHandle.get<Any>(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Calendar is initialized with the correct state`() {
        initViewModel()
        val initialState = viewModel.uiState.value

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

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

        coVerify { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, any(), true) }
        coVerify { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, any(), true) }
        coVerify { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, any(), true) }
    }

    @Test
    fun `Error state is set when fetching events fails`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } throws Exception()
        initViewModel()

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
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
            createPlannerItem(2, 6, PlannableType.SUB_ASSIGNMENT, createDate(2023, 4, 20, 12)),
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        initViewModel()

        val expectedCurrentEvents = listOf(
            EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM"),
            EventUiState(2, "Course 1", Course(1), "Plannable 2", R.drawable.ic_quiz, "Due Apr 20 12:00 PM"),
            EventUiState(6, "Course 2", Course(2), "Plannable 6", R.drawable.ic_discussion, "Due Apr 20 12:00 PM")
        )
        val expectedPreviousEvents = listOf(
            EventUiState(3, "Course 1", Course(1), "Plannable 3", R.drawable.ic_calendar, "Apr 19 12:00 PM - 1:00 PM"),
            EventUiState(4, "Course 2", Course(2), "Plannable 4", R.drawable.ic_discussion, "Due Apr 19 12:00 PM")
        )
        val expectedNextEvents = listOf(
            EventUiState(5, "Course 2 To Do", Course(2), "Plannable 5", R.drawable.ic_todo, "Apr 21 12:00 PM"),
        )
        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expectedPreviousEvents),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedCurrentEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21), events = expectedNextEvents)
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Calendar events show the correct date text depending on it's start and end date`() {
        val events = listOf(
            createPlannerItem(
                1,
                3,
                PlannableType.CALENDAR_EVENT,
                createDate(2023, 4, 19, 12),
                startAt = createDate(2023, 4, 19, 12),
                endAt = createDate(2023, 4, 19, 13)
            ),
            createPlannerItem(
                1,
                4,
                PlannableType.CALENDAR_EVENT,
                createDate(2023, 4, 19, 12),
                startAt = createDate(2023, 4, 19, 10),
                endAt = createDate(2023, 4, 19, 10)
            ),
            createPlannerItem(
                1,
                5,
                PlannableType.CALENDAR_EVENT,
                createDate(2023, 4, 19, 12),
                startAt = createDate(2023, 4, 19, 10),
                endAt = createDate(2023, 4, 19, 10),
                allDay = true
            ),
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        initViewModel()

        val expectedPreviousEvents = listOf(
            EventUiState(3, "Course 1", Course(1), "Plannable 3", R.drawable.ic_calendar, "Apr 19 12:00 PM - 1:00 PM"),
            EventUiState(4, "Course 1", Course(1), "Plannable 4", R.drawable.ic_calendar, "Apr 19 10:00 AM"),
            EventUiState(5, "Course 1", Course(1), "Plannable 5", R.drawable.ic_calendar, "Apr 19"),
        )
        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expectedPreviousEvents),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Calendar events show the correct tag`() {
        val events = listOf(
            createPlannerItem(
                1,
                3,
                PlannableType.CALENDAR_EVENT,
                createDate(2023, 4, 19, 12),
                startAt = createDate(2023, 4, 19, 12),
                endAt = createDate(2023, 4, 19, 13)
            ),
            createPlannerItem(
                1,
                4,
                PlannableType.SUB_ASSIGNMENT,
                createDate(2023, 4, 19, 12),
                startAt = createDate(2023, 4, 19, 10),
                endAt = createDate(2023, 4, 19, 10),
                subAssignmentTag = "reply_to_topic",
                replyRequiredCount = 2
            ),
            createPlannerItem(
                1,
                5,
                PlannableType.SUB_ASSIGNMENT,
                createDate(2023, 4, 19, 12),
                startAt = createDate(2023, 4, 19, 10),
                endAt = createDate(2023, 4, 19, 10),
                subAssignmentTag = "reply_to_entry",
                replyRequiredCount = 2
            )
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        initViewModel()

        val expectedPreviousEvents = listOf(
            EventUiState(3, "Course 1", Course(1), "Plannable 3", R.drawable.ic_calendar, "Apr 19 12:00 PM - 1:00 PM"),
            EventUiState(4, "Course 1", Course(1), "Plannable 4", R.drawable.ic_discussion, "Due Apr 19 12:00 PM", tag = "Reply to topic"),
            EventUiState(5, "Course 1", Course(1), "Plannable 5", R.drawable.ic_discussion, "Due Apr 19 12:00 PM", tag = "Additional replies (2)"),
        )
        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expectedPreviousEvents),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
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

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, any(), true) }
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

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, any(), true) }

        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2023, 5, 19)))
        // We also need to call this in the test because day is not selected instantly,
        // but after the new state is created and the pager animates to the next page and signals to the view model.
        viewModel.handleAction(CalendarAction.PageChanged(1))

        val newMonthStartDate = LocalDate.of(2023, 6, 1).atStartOfDay().toApiString()
        val newMonthEndDate = LocalDate.of(2023, 7, 1).atStartOfDay().toApiString()

        coVerify { calendarRepository.getPlannerItems(newMonthStartDate!!, newMonthEndDate!!, any(), true) }
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
        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expected19thEvents),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expected20thEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21), events = expected21stEvents)
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2023, 4, 19)))

        val expectedNewState = CalendarScreenUiState(
            baseCalendarUiState.copy(selectedDay = LocalDate.of(2023, 4, 19)), CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 18)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19), events = expected19thEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expected20thEvents)
            )
        )

        assertEquals(expectedNewState, viewModel.uiState.value)
    }

    @Test
    fun `Change expanded state when expand changes`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandChanged(true))
        assertEquals(expectedState.calendarUiState.copy(expanded = true), viewModel.uiState.value.calendarUiState)

        viewModel.handleAction(CalendarAction.ExpandChanged(false))
        assertEquals(expectedState.calendarUiState.copy(expanded = false), viewModel.uiState.value.calendarUiState)
    }

    @Test
    fun `Change expanded state to false when expand is disabled`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandChanged(true))
        assertEquals(expectedState.calendarUiState.copy(expanded = true), viewModel.uiState.value.calendarUiState)

        viewModel.handleAction(CalendarAction.ExpandDisabled)
        assertEquals(expectedState.calendarUiState.copy(expanded = false), viewModel.uiState.value.calendarUiState)
    }

    @Test
    fun `Select today when jump to today is tapped`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        // Select an other day
        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2022, 2, 19)))
        // We also need to call this in the test because day is not selected instantly,
        // but after the new state is created and the pager animates to the next page and signals to the view model.
        viewModel.handleAction(CalendarAction.PageChanged(-1))

        val newDayExpectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(selectedDay = LocalDate.of(2022, 2, 19)), CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2022, 2, 18)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2022, 2, 19)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2022, 2, 20))
            )
        )
        assertEquals(newDayExpectedState, viewModel.uiState.value)

        // Tap on today
        viewModel.handleAction(CalendarAction.TodayTapped)

        // Assert that animation is in progress
        assertEquals(LocalDate.now(clock), viewModel.uiState.value.calendarUiState.pendingSelectedDay)
        assertEquals(1, viewModel.uiState.value.calendarUiState.scrollToPageOffset)
        // Selected day remains the same until the animation is finished
        assertEquals(LocalDate.of(2022, 2, 19), viewModel.uiState.value.calendarUiState.selectedDay)

        // Simulate the animation finished
        viewModel.handleAction(CalendarAction.PageChanged(1))

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Today tapped on the same page selects today`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()
        viewModel.handleAction(CalendarAction.ExpandChanged(true)) // Switch to month view

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(expanded = true), CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        // Select an other day
        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2023, 4, 10)))

        val newDayExpectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(expanded = true, selectedDay = LocalDate.of(2023, 4, 10)), CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 9)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 10)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 11))
            )
        )
        assertEquals(newDayExpectedState, viewModel.uiState.value)

        // Tap on today
        viewModel.handleAction(CalendarAction.TodayTapped)
        assertEquals(expectedState.copy(calendarUiState = expectedState.calendarUiState.copy(todayTapped = true)), viewModel.uiState.value)
    }

    @Test
    fun `Go to next month when calendar is expanded and page is swiped`() {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandChanged(true))
        viewModel.handleAction(CalendarAction.PageChanged(1))

        val newExpectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(selectedDay = LocalDate.of(2023, 5, 20), expanded = true), CalendarEventsUiState(
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

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.PageChanged(1))

        val newExpectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(selectedDay = LocalDate.of(2023, 4, 27)), CalendarEventsUiState(
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

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.EventPageChanged(1))

        val newExpectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(selectedDay = LocalDate.of(2023, 4, 21)), CalendarEventsUiState(
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

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(startDate!!, endDate!!, any(), true) }
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

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
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

        val newExpectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
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

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.RefreshDay(LocalDate.of(2023, 4, 20)))

        val newExpectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
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
    fun `Open assignment when assignment is selected`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenAssignment(Course(1), 1)
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open assignment when sub assignment is selected`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.SUB_ASSIGNMENT, createDate(2023, 4, 20, 12)).copy(
                htmlUrl = "/courses/1/assignments/123"
            )
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenAssignment(Course(1), 123)
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open assignment when submitted sub assignment is selected`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.SUB_ASSIGNMENT, createDate(2023, 4, 20, 12)).copy(
                htmlUrl = "/courses/1/assignments/123/submissions/321"
            )
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenAssignment(Course(1), 123)
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open discussion when discussion is selected`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.DISCUSSION_TOPIC, createDate(2023, 4, 20, 12)),
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenDiscussion(Course(1), 1, 1)
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open assignment when an assignment quiz is selected`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.QUIZ, createDate(2023, 4, 20, 12)).copy(htmlUrl = "http://quiz.com"),
        )
        initViewModel()

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val expectedAction = CalendarViewModelAction.OpenAssignment(Course(1), 1)
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open quiz when a quiz is selected that is not an assignment`() = runTest {
        val plannerItem = createPlannerItem(1, 1, PlannableType.QUIZ, createDate(2023, 4, 20, 12))
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            plannerItem.copy(htmlUrl = "http://quiz.com", plannable = plannerItem.plannable.copy(assignmentId = null))
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenQuiz(Course(1), "http://quiz.com")
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open calendar event when calendar event is selected`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(
            createPlannerItem(1, 1, PlannableType.CALENDAR_EVENT, createDate(2023, 4, 20, 12)),
        )
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenCalendarEvent(Course(1), 1)
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open calendar todo when calendar todo is selected`() = runTest {
        val plannerItem = createPlannerItem(1, 1, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12))
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns listOf(plannerItem)
        initViewModel()

        viewModel.handleAction(CalendarAction.EventSelected(1))

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenToDo(plannerItem)
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Open create todo when create todo is selected`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        viewModel.handleAction(CalendarAction.AddToDoTapped)

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenCreateToDo(LocalDate.now(clock).toApiString())
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Correct strings are mapped for submission states`() = runTest {
        val events = listOf(
            createPlannerItem(
                1,
                1,
                PlannableType.ASSIGNMENT,
                createDate(2023, 4, 20, 12),
                submissionState = SubmissionState(excused = true)
            ),
            createPlannerItem(
                1,
                2,
                PlannableType.ASSIGNMENT,
                createDate(2023, 4, 20, 12),
                submissionState = SubmissionState(missing = true)
            ),
            createPlannerItem(
                1,
                3,
                PlannableType.ASSIGNMENT,
                createDate(2023, 4, 20, 12),
                submissionState = SubmissionState(graded = true)
            ),
            createPlannerItem(
                2,
                4,
                PlannableType.ASSIGNMENT,
                createDate(2023, 4, 20, 12),
                submissionState = SubmissionState(needsGrading = true)
            ),
            createPlannerItem(
                2,
                5,
                PlannableType.ASSIGNMENT,
                createDate(2023, 4, 20, 12),
                pointsPossible = 10.0,
                submissionState = SubmissionState()
            ),
            createPlannerItem(2, 6, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12), submissionState = SubmissionState()),
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        initViewModel()

        val currentPageEvents = viewModel.uiState.value.calendarEventsUiState.currentPage.events
        assertEquals("excused", currentPageEvents[0].status)
        assertEquals("missing", currentPageEvents[1].status)
        assertEquals("graded", currentPageEvents[2].status)
        assertEquals("needs grading", currentPageEvents[3].status)
        assertEquals("10 pts", currentPageEvents[4].status)
        assertNull(currentPageEvents[5].status)
    }

    @Test
    fun `Expanded state is dependent on preferences and is saved when changed`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        every { calendarPrefs.calendarExpanded } returns true
        initViewModel()

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(expanded = true), CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandChanged(false))
        viewModel.handleAction(CalendarAction.HeightAnimationFinished) // We also need to call this to fully collapse the calendar and save the state

        assertEquals(expectedState.calendarUiState.copy(expanded = false), viewModel.uiState.value.calendarUiState)
        coVerify { calendarPrefs.calendarExpanded = false }
    }

    @Test
    fun `Expand enabled restores expanded state from prefs`() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        every { calendarPrefs.calendarExpanded } returns true
        initViewModel()

        val expectedState = CalendarScreenUiState(
            baseCalendarUiState.copy(expanded = true), CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        viewModel.handleAction(CalendarAction.ExpandDisabled)
        assertEquals(expectedState.calendarUiState.copy(expanded = false), viewModel.uiState.value.calendarUiState)

        viewModel.handleAction(CalendarAction.ExpandEnabled)
        assertEquals(expectedState.calendarUiState.copy(expanded = true), viewModel.uiState.value.calendarUiState)
    }

    @Test
    fun `Open filters when filters selected`() = runTest {
        initViewModel()

        viewModel.handleAction(CalendarAction.FilterTapped)

        val events = mutableListOf<CalendarViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expectedAction = CalendarViewModelAction.OpenFilters
        assertEquals(expectedAction, events.last())
    }

    @Test
    fun `Filter events based on the selected calendars`() = runTest {
        val events = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
            createPlannerItem(2, 2, PlannableType.QUIZ, createDate(2023, 4, 20, 12)),
            createPlannerItem(
                null,
                4,
                PlannableType.DISCUSSION_TOPIC,
                createDate(2023, 4, 20, 12),
                groupId = 3
            ).copy(contextName = "Group 3"),
            createPlannerItem(null, 5, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), groupId = 4),
            createPlannerItem(null, 6, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), userId = 5).copy(contextName = null),
            createPlannerItem(null, 7, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), userId = 6),
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            1,
            "",
            "1",
            filters = setOf("course_1", "group_3", "user_5")
        )
        initViewModel()

        val expectedCurrentEvents = listOf(
            EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM"),
            EventUiState(4, "Group 3", Group(3), "Plannable 4", R.drawable.ic_discussion, "Due Apr 20 12:00 PM"),
            EventUiState(6, "To Do", User(5), "Plannable 6", R.drawable.ic_todo, "Apr 20 12:00 PM")
        )
        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedCurrentEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Filter events based on the selected calendars when no previous filters are added and all filters are added after context requests`() =
        runTest {
            val events = listOf(
                createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
                createPlannerItem(2, 2, PlannableType.QUIZ, createDate(2023, 4, 20, 12)),
                createPlannerItem(
                    null,
                    4,
                    PlannableType.DISCUSSION_TOPIC,
                    createDate(2023, 4, 20, 12),
                    groupId = 3
                ).copy(contextName = "Group 3"),
                createPlannerItem(null, 5, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), groupId = 4),
                createPlannerItem(null, 6, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), userId = 5).copy(contextName = null),
                createPlannerItem(null, 7, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), userId = 6),
            )
            every { apiPrefs.user } returns User(5)
            coEvery { calendarRepository.getCalendarFilters() } returns null
            coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
            coEvery { calendarRepository.getCanvasContexts() } returns DataResult.Success(
                mapOf(
                    CanvasContext.Type.COURSE to listOf(Course(1)),
                    CanvasContext.Type.GROUP to listOf(Group(3)),
                    CanvasContext.Type.USER to listOf(User(5))
                )
            )
            initViewModel()

            val expectedCurrentEvents = listOf(
                EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM"),
                EventUiState(4, "Group 3", Group(3), "Plannable 4", R.drawable.ic_discussion, "Due Apr 20 12:00 PM"),
                EventUiState(6, "To Do", User(5), "Plannable 6", R.drawable.ic_todo, "Apr 20 12:00 PM")
            )
            val expectedState = CalendarScreenUiState(
                baseCalendarUiState, CalendarEventsUiState(
                    previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                    currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedCurrentEvents),
                    nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
                )
            )

            assertEquals(expectedState, viewModel.uiState.value)
        }

    @Test
    fun `Refresh filter updates filters`() = runTest {
        val events = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
            createPlannerItem(2, 2, PlannableType.QUIZ, createDate(2023, 4, 20, 12)),
            createPlannerItem(
                null,
                4,
                PlannableType.DISCUSSION_TOPIC,
                createDate(2023, 4, 20, 12),
                groupId = 3
            ).copy(contextName = "Group 3"),
            createPlannerItem(null, 5, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), groupId = 4),
            createPlannerItem(null, 6, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), userId = 5).copy(contextName = null),
            createPlannerItem(null, 7, PlannableType.PLANNER_NOTE, createDate(2023, 4, 20, 12), userId = 6),
        )
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns events
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            1,
            "",
            "1",
            filters = setOf("course_1", "group_3", "user_5")
        )
        initViewModel()

        val expectedCurrentEvents = listOf(
            EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM"),
            EventUiState(4, "Group 3", Group(3), "Plannable 4", R.drawable.ic_discussion, "Due Apr 20 12:00 PM"),
            EventUiState(6, "To Do", User(5), "Plannable 6", R.drawable.ic_todo, "Apr 20 12:00 PM")
        )
        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedCurrentEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        // Change filters
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            1,
            "",
            "1",
            filters = setOf("course_2")
        )

        viewModel.handleAction(CalendarAction.FiltersRefreshed)

        val updatedExpectedCurrentEvents = listOf(
            EventUiState(2, "Course 2", Course(2), "Plannable 2", R.drawable.ic_quiz, "Due Apr 20 12:00 PM"),
        )
        val updatedExpectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = updatedExpectedCurrentEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(updatedExpectedState, viewModel.uiState.value)
    }

    @Test
    fun `Refresh filters refreshes calendar when there is a filter limit`() = runTest {
        val events = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, createDate(2023, 4, 20, 12)),
            createPlannerItem(2, 2, PlannableType.QUIZ, createDate(2023, 4, 20, 12))
        )
        coEvery { calendarRepository.getCalendarFilterLimit() } returns 10
        coEvery { calendarRepository.getPlannerItems(any(), any(), listOf("course_1"), any()) } returns events.subList(0, 1)
        coEvery { calendarRepository.getPlannerItems(any(), any(), listOf("course_2"), any()) } returns events.subList(1, 2)
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            1,
            "",
            "1",
            filters = setOf("course_1")
        )
        initViewModel()

        // This should be called 3 times for the 3 visible months
        coVerify(exactly = 3) { calendarRepository.getPlannerItems(any(), any(), listOf("course_1"), any()) }

        val expectedCurrentEvents = listOf(
            EventUiState(1, "Course 1", Course(1), "Plannable 1", R.drawable.ic_assignment, "Due Apr 20 12:00 PM")
        )
        val expectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = expectedCurrentEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(expectedState, viewModel.uiState.value)

        // Change filters
        coEvery { calendarRepository.getCalendarFilters() } returns CalendarFilterEntity(
            1,
            "",
            "1",
            filters = setOf("course_2")
        )

        viewModel.handleAction(CalendarAction.FiltersRefreshed)

        // This should be called 3 times for the 3 visible months
        coVerify(exactly = 3) { calendarRepository.getPlannerItems(any(), any(), listOf("course_2"), any()) }

        val updatedExpectedCurrentEvents = listOf(
            EventUiState(2, "Course 2", Course(2), "Plannable 2", R.drawable.ic_quiz, "Due Apr 20 12:00 PM"),
        )
        val updatedExpectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20), events = updatedExpectedCurrentEvents),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            )
        )

        assertEquals(updatedExpectedState, viewModel.uiState.value)
    }

    @Test
    fun refreshCalendarActionReloadsFullMonth() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val previousMonthStartDate = LocalDate.of(2023, 3, 1).atStartOfDay().toApiString()
        val previousMonthEndDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentStartDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentMonthEndDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthStartDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthEndDate = LocalDate.of(2023, 6, 1).atStartOfDay().toApiString()

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, any(), true) }

        viewModel.handleAction(CalendarAction.PullToRefresh)

        coVerify(exactly = 2) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, any(), true) }
        coVerify(exactly = 2) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, any(), true) }
        coVerify(exactly = 2) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, any(), true) }
    }

    @Test
    fun refreshCalendarFailureShowsErrorSnackbarAndDoesNotRequestPreviousAndNextPage() = runTest {
        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } returns emptyList()
        initViewModel()

        val previousMonthStartDate = LocalDate.of(2023, 3, 1).atStartOfDay().toApiString()
        val previousMonthEndDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentStartDate = LocalDate.of(2023, 4, 1).atStartOfDay().toApiString()
        val currentMonthEndDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthStartDate = LocalDate.of(2023, 5, 1).atStartOfDay().toApiString()
        val nextMonthEndDate = LocalDate.of(2023, 6, 1).atStartOfDay().toApiString()

        coVerify(exactly = 1) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, any(), true) }

        coEvery { calendarRepository.getPlannerItems(any(), any(), any(), any()) } throws Exception()

        viewModel.handleAction(CalendarAction.PullToRefresh)

        coVerify(exactly = 2) { calendarRepository.getPlannerItems(currentStartDate!!, currentMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(previousMonthStartDate!!, previousMonthEndDate!!, any(), true) }
        coVerify(exactly = 1) { calendarRepository.getPlannerItems(nextMonthStartDate!!, nextMonthEndDate!!, any(), true) }

        val newExpectedState = CalendarScreenUiState(
            baseCalendarUiState, CalendarEventsUiState(
                previousPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 19)),
                currentPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 20)),
                nextPage = CalendarEventsPageUiState(date = LocalDate.of(2023, 4, 21))
            ), snackbarMessage = "Error refreshing events"
        )

        assertEquals(newExpectedState, viewModel.uiState.value)
    }

    @Test
    fun `Send shared today button visible event when selected day is changed`() = runTest {
        initViewModel()

        viewModel.handleAction(CalendarAction.DaySelected(LocalDate.of(2023, 4, 22)))

        coVerify { calendarSharedEvents.sendEvent(any(), SharedCalendarAction.TodayButtonVisible(true)) }
    }

    @Test
    fun `Set today tapped in UI state when today tapped and set it back to false when handled`() {
        initViewModel()

        viewModel.handleAction(CalendarAction.TodayTapped)

        assertTrue(viewModel.uiState.value.calendarUiState.todayTapped)

        viewModel.handleAction(CalendarAction.TodayTapHandled)

        assertFalse(viewModel.uiState.value.calendarUiState.todayTapped)
    }

    @Test
    fun `Selected day is parsed from saved state when valid date provided`() {
        every { savedStateHandle.get<String>(CalendarFragment.SELECTED_DAY) } returns "2025-05-10"

        initViewModel()

        assertEquals(
            LocalDate.of(2025, 5, 10),
            viewModel.uiState.value.calendarUiState.selectedDay
        )
    }

    @Test
    fun `showAddEventButton is true when behavior allows it`() = runTest {
        coEvery { calendarBehavior.shouldShowAddEventButton() } returns true
        initViewModel()

        assertTrue(viewModel.uiState.value.showAddEventButton)
    }

    @Test
    fun `showAddEventButton is false when behavior restricts it`() = runTest {
        coEvery { calendarBehavior.shouldShowAddEventButton() } returns false
        initViewModel()

        assertFalse(viewModel.uiState.value.showAddEventButton)
    }

    @Test
    fun `behavior shouldShowAddEventButton is called during initialization`() = runTest {
        initViewModel()

        coVerify { calendarBehavior.shouldShowAddEventButton() }
    }

    private fun initViewModel() {
        viewModel = CalendarViewModel(context, calendarRepository, apiPrefs, clock, calendarPrefs, calendarStateMapper, calendarSharedEvents, calendarBehavior, cancelReservationUseCase, getAppointmentGroupsUseCase, savedStateHandle)
    }

    private fun createPlannerItem(
        courseId: Long?,
        plannableId: Long,
        plannableType: PlannableType,
        date: Date,
        submissionState: SubmissionState? = null,
        pointsPossible: Double? = null,
        startAt: Date? = null,
        endAt: Date? = null,
        allDay: Boolean = false,
        groupId: Long? = null,
        userId: Long? = null,
        subAssignmentTag: String? = null,
        replyRequiredCount: Int? = null
    ): PlannerItem {
        val plannable = Plannable(
            id = plannableId,
            title = "Plannable $plannableId",
            courseId,
            groupId,
            userId,
            pointsPossible,
            date,
            plannableId,
            date.toApiString(),
            startAt,
            endAt,
            "details",
            allDay,
            subAssignmentTag
        )
        val details = if (replyRequiredCount != null) {
            PlannerItemDetails(
                replyRequiredCount = replyRequiredCount
            )
        } else {
            null
        }
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
            newActivity = null,
            plannableItemDetails = details,
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
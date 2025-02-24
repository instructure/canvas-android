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

package com.instructure.pandautils.features.calendarevent.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.reminder.ReminderManager
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.dueAt
import com.instructure.pandautils.utils.eventHtmlUrl
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
import java.time.LocalDateTime


@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val themePrefs: ThemePrefs = mockk(relaxed = true)
    private val reminderManager: ReminderManager = mockk(relaxed = true)
    private val eventViewModelBehavior: EventViewModelBehavior = mockk(relaxed = true)

    private lateinit var viewModel: EventViewModel

    private val scheduleItem = ScheduleItem(
        contextName = "Context name",
        contextCode = "user_1",
        title = "Title",
        startAt = LocalDate.of(2024, 3, 1).atTime(11, 0).toApiString(),
        endAt = LocalDate.of(2024, 3, 1).atTime(12, 0).toApiString(),
        isAllDay = false,
        seriesNaturalLanguage = "Every day",
        locationName = "Location",
        locationAddress = "Address",
        description = "Description html",
        workflowState = "active"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(ThemePrefs)
        every { ThemePrefs.primaryColor } returns 1

        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0)

        coEvery { htmlContentFormatter.formatHtmlWithIframes(any()) } answers { firstArg() }

        every { savedStateHandle.get<Any>(any()) } returns null

        mockkObject(DateHelper)
        every { DateHelper.getPreferredDateFormat(any()) } returns DateHelper.dayMonthDateFormat
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Toolbar color set up by canvas context`() {
        val canvasContext = CanvasContext.emptyCourseContext(1)

        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns canvasContext

        createViewModel()

        Assert.assertEquals(canvasContext.color, viewModel.uiState.value.toolbarUiState.toolbarColor)
    }

    @Test
    fun `Event mapped correctly from arguments`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem

        createViewModel()

        val expectedState = EventUiState(
            toolbarUiState = ToolbarUiState(
                toolbarColor = 1,
                subtitle = "Context name",
                editAllowed = false,
                deleteAllowed = false
            ),
            loading = false,
            title = "Title",
            date = "Mar 1 11:00 AM - 12:00 PM",
            recurrence = "Every day",
            location = "Location",
            address = "Address",
            formattedDescription = "Description html"
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Event mapped correctly from api`() {
        every { savedStateHandle.get<Long>(EventFragment.SCHEDULE_ITEM_ID) } returns 1
        coEvery { eventRepository.getCalendarEvent(1) } returns scheduleItem
        every { apiPrefs.user } returns User(1)

        createViewModel()

        val expectedState = EventUiState(
            toolbarUiState = ToolbarUiState(
                toolbarColor = 1,
                subtitle = "Context name",
                editAllowed = true,
                deleteAllowed = true
            ),
            loading = false,
            title = "Title",
            date = "Mar 1 11:00 AM - 12:00 PM",
            recurrence = "Every day",
            location = "Location",
            address = "Address",
            formattedDescription = "Description html"
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Event loading failed`() {
        every { savedStateHandle.get<Long>(EventFragment.SCHEDULE_ITEM_ID) } returns 1
        every { context.getString(R.string.errorLoadingEvent) } returns "Error loading event"
        coEvery { eventRepository.getCalendarEvent(1) } throws Exception()

        createViewModel()

        val expectedState = EventUiState(
            loadError = "Error loading event"
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Date text when all day event`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(isAllDay = true)
        every { context.getString(R.string.allDayEvent) } returns "All Day Event"

        createViewModel()

        val expectedState = EventUiState(
            toolbarUiState = ToolbarUiState(
                toolbarColor = 1,
                subtitle = "Context name",
                editAllowed = false,
                deleteAllowed = false
            ),
            loading = false,
            title = "Title",
            date = "Mar 1 - All Day Event",
            recurrence = "Every day",
            location = "Location",
            address = "Address",
            formattedDescription = "Description html"
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Date text when time interval event`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(
            startAt = LocalDate.of(2024, 3, 1).atTime(11, 0).toApiString(),
            endAt = LocalDate.of(2024, 3, 1).atTime(12, 0).toApiString()
        )

        createViewModel()

        val expectedState = EventUiState(
            toolbarUiState = ToolbarUiState(
                toolbarColor = 1,
                subtitle = "Context name",
                editAllowed = false,
                deleteAllowed = false
            ),
            loading = false,
            title = "Title",
            date = "Mar 1 11:00 AM - 12:00 PM",
            recurrence = "Every day",
            location = "Location",
            address = "Address",
            formattedDescription = "Description html"
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Date text when not interval event`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(
            startAt = LocalDate.of(2024, 3, 1).atTime(11, 0).toApiString(),
            endAt = LocalDate.of(2024, 3, 1).atTime(11, 0).toApiString()
        )

        createViewModel()

        val expectedState = EventUiState(
            toolbarUiState = ToolbarUiState(
                toolbarColor = 1,
                subtitle = "Context name",
                editAllowed = false,
                deleteAllowed = false
            ),
            loading = false,
            title = "Title",
            date = "Mar 1 11:00 AM",
            recurrence = "Every day",
            location = "Location",
            address = "Address",
            formattedDescription = "Description html"
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Date text without end date`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(
            startAt = LocalDate.of(2024, 3, 1).atTime(11, 0).toApiString(),
            endAt = null
        )

        createViewModel()

        val expectedState = EventUiState(
            toolbarUiState = ToolbarUiState(
                toolbarColor = 1,
                subtitle = "Context name",
                editAllowed = false,
                deleteAllowed = false
            ),
            loading = false,
            title = "Title",
            date = "Mar 1",
            recurrence = "Every day",
            location = "Location",
            address = "Address",
            formattedDescription = "Description html"
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Lti button tapped`() = runTest {
        createViewModel()

        val events = mutableListOf<EventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(EventAction.OnLtiClicked("url"))

        val expectedEvent = EventViewModelAction.OpenLtiScreen("url")
        Assert.assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Edit event button tapped`() = runTest {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem

        createViewModel()

        val events = mutableListOf<EventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(EventAction.EditEvent)

        val expectedEvent = EventViewModelAction.OpenEditEvent(scheduleItem)
        Assert.assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Delete event`() = runTest {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        coEvery { eventRepository.deleteCalendarEvent(scheduleItem.id) } returns scheduleItem

        createViewModel()

        val events = mutableListOf<EventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(EventAction.DeleteEvent(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify {
            eventRepository.deleteCalendarEvent(scheduleItem.id)
        }

        val expectedEvent = EventViewModelAction.RefreshCalendarDays(listOf(LocalDate.of(2024, 3, 1)))
        Assert.assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Delete recurring event`() = runTest {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(rrule = "FREQ=DAILY;INTERVAL=1;COUNT=5")
        coEvery { eventRepository.deleteRecurringCalendarEvent(scheduleItem.id, CalendarEventAPI.ModifyEventScope.ALL) } returns listOf(
            scheduleItem,
            scheduleItem.copy(startAt = LocalDate.of(2024, 3, 2).atTime(11, 0).toApiString())
        )

        createViewModel()

        val events = mutableListOf<EventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(EventAction.DeleteEvent(CalendarEventAPI.ModifyEventScope.ALL))

        coVerify {
            eventRepository.deleteRecurringCalendarEvent(scheduleItem.id, CalendarEventAPI.ModifyEventScope.ALL)
        }

        val expectedEvent = EventViewModelAction.RefreshCalendar
        Assert.assertEquals(expectedEvent, events.last())
    }

    @Test
    fun `Delete event failed`() = runTest {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        every { context.getString(R.string.eventDeleteErrorMessage) } returns "Error message"
        coEvery { eventRepository.deleteCalendarEvent(scheduleItem.id) } throws Exception()

        createViewModel()

        viewModel.handleAction(EventAction.DeleteEvent(CalendarEventAPI.ModifyEventScope.ONE))

        Assert.assertEquals("Error message", viewModel.uiState.value.errorSnack)

        viewModel.handleAction(EventAction.SnackbarDismissed)
        Assert.assertNull(viewModel.uiState.value.errorSnack)
    }

    @Test
    fun `Can modify event if contextCode matches userId`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        every { apiPrefs.user } returns User(1)

        createViewModel()

        Assert.assertTrue(viewModel.uiState.value.toolbarUiState.editAllowed)
        Assert.assertTrue(viewModel.uiState.value.toolbarUiState.deleteAllowed)
    }

    @Test
    fun `Can not modify event if contextCode not matches userId`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        every { apiPrefs.user } returns User(2)

        createViewModel()

        Assert.assertFalse(viewModel.uiState.value.toolbarUiState.editAllowed)
        Assert.assertFalse(viewModel.uiState.value.toolbarUiState.deleteAllowed)
    }

    @Test
    fun `Can modify event if user can manage calendar events and event has course contextCode`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(
            contextCode = "course_1"
        )
        coEvery { eventRepository.canManageCourseCalendar(1) } returns true

        createViewModel()

        Assert.assertTrue(viewModel.uiState.value.toolbarUiState.editAllowed)
        Assert.assertTrue(viewModel.uiState.value.toolbarUiState.deleteAllowed)
    }

    @Test
    fun `Can not modify event if user can not manage calendar events and event has course contextCode`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(
            contextCode = "course_1"
        )
        coEvery { eventRepository.canManageCourseCalendar(1) } returns false

        createViewModel()

        Assert.assertFalse(viewModel.uiState.value.toolbarUiState.editAllowed)
        Assert.assertFalse(viewModel.uiState.value.toolbarUiState.deleteAllowed)
    }

    @Test
    fun `Can modify event if user can manage calendar events and event has group contextCode`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(
            contextCode = "group_1"
        )
        coEvery { eventRepository.canManageGroupCalendar(1) } returns true

        createViewModel()

        Assert.assertTrue(viewModel.uiState.value.toolbarUiState.editAllowed)
        Assert.assertTrue(viewModel.uiState.value.toolbarUiState.deleteAllowed)
    }

    @Test
    fun `Can not modify event if user can not manage calendar events and event has group contextCode`() {
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem.copy(
            contextCode = "group_1"
        )
        coEvery { eventRepository.canManageGroupCalendar(1) } returns false

        createViewModel()

        Assert.assertFalse(viewModel.uiState.value.toolbarUiState.editAllowed)
        Assert.assertFalse(viewModel.uiState.value.toolbarUiState.deleteAllowed)
    }

    @Test
    fun `Custom DatePicker opens to set reminder if Date is null`() {
        val scheduleItem = scheduleItem.copy(startAt = null, endAt = null)
        val context: Context = mockk(relaxed = true)
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        every { apiPrefs.user } returns User(1)
        createViewModel()

        viewModel.showCreateReminderDialog(context, 1)

        coVerify(exactly = 1) {
            reminderManager.showCustomReminderDialog(
                context,
                1,
                scheduleItem.id,
                scheduleItem.title.orEmpty(),
                scheduleItem.eventHtmlUrl.orEmpty(),
                scheduleItem.dueAt,
            )
        }
    }

    @Test
    fun `Custom DatePicker opens to set reminder if Due Date is in past`() {
        val scheduleItem =
            scheduleItem.copy(startAt = LocalDateTime.now().minusDays(2).toString(), endAt = LocalDateTime.now().minusDays(1).toString())
        val context: Context = mockk(relaxed = true)
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        every { apiPrefs.user } returns User(1)
        createViewModel()

        viewModel.showCreateReminderDialog(context, 1)

        coVerify(exactly = 1) {
            reminderManager.showCustomReminderDialog(
                context,
                1,
                scheduleItem.id,
                scheduleItem.title.orEmpty(),
                scheduleItem.eventHtmlUrl.orEmpty(),
                scheduleItem.dueAt,
            )
        }
    }

    @Test
    fun `Before due date dialog opens to set reminder if Due Date is in the future`() {
        val scheduleItem = scheduleItem.copy(startAt = LocalDateTime.now().plusDays(1).toString(), endAt = LocalDateTime.now().plusDays(2).toString())
        val context: Context = mockk(relaxed = true)
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        every { apiPrefs.user } returns User(1)
        createViewModel()

        viewModel.showCreateReminderDialog(context, 1)

        coVerify(exactly = 1) {
            reminderManager.showBeforeDueDateReminderDialog(
                context,
                1,
                scheduleItem.id,
                scheduleItem.title.orEmpty(),
                scheduleItem.eventHtmlUrl.orEmpty(),
                scheduleItem.dueAt!!,
                any()
            )
        }
    }

    @Test
    fun `Send message`() = runTest {
        val canvasContext = CanvasContext.emptyCourseContext(1)
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns canvasContext
        every { savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM) } returns scheduleItem
        val options = InboxComposeOptions()
        every { eventViewModelBehavior.getInboxComposeOptions(canvasContext, scheduleItem) } returns options

        createViewModel()

        val events = mutableListOf<EventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(EventAction.OnMessageFabClicked)

        coVerify {
            eventViewModelBehavior.getInboxComposeOptions(canvasContext, scheduleItem)
        }

        val expectedEvent = EventViewModelAction.NavigateToComposeMessageScreen(options)
        Assert.assertEquals(expectedEvent, events.last())
    }

    private fun createViewModel() {
        viewModel = EventViewModel(
            savedStateHandle,
            context,
            eventRepository,
            htmlContentFormatter,
            apiPrefs,
            themePrefs,
            reminderManager,
            eventViewModelBehavior
        )
    }
}

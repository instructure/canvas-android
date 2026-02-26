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

package com.instructure.pandautils.features.calendarevent.createupdate

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.SelectContextUiState
import com.instructure.pandautils.utils.getSystemLocaleCalendar
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.Calendar


@ExperimentalCoroutinesApi
class CreateUpdateEventViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val resources: Resources = mockk(relaxed = true)
    private val repository: TestCreateUpdateEventRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val createUpdateEventViewModelBehavior: CreateUpdateEventViewModelBehavior = mockk(relaxed = true)

    private val clock = Clock.fixed(Instant.parse("2024-04-10T11:00:00.00Z"), ZoneId.systemDefault())

    private lateinit var viewModel: CreateUpdateEventViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        setupResources()
        every { savedStateHandle.get<Any>(any()) } returns null
        every { apiPrefs.user } returns User(1)
        coEvery { repository.getCanvasContexts() } returns listOf(User(1))
        mockkObject(CanvasRestAdapter)
        every { CanvasRestAdapter.clearCacheUrls(any()) } returns mockk()

        // Mock getSystemLocaleCalendar to return a simple Calendar instance for testing
        mockkStatic(::getSystemLocaleCalendar)
        every { getSystemLocaleCalendar() } returns Calendar.getInstance()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(::getSystemLocaleCalendar)
    }

    @Test
    fun `Initialized with the correct state when creating`() {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()
        val state = viewModel.uiState.value

        val expectedState = CreateUpdateEventUiState(
            date = LocalDate.of(2024, 4, 10),
            selectContextUiState = SelectContextUiState(
                selectedCanvasContext = User(1),
                canvasContexts = listOf(User(1))
            ),
            selectFrequencyUiState = SelectFrequencyUiState(
                selectedFrequency = "Does Not Repeat",
                frequencies = emptyMap()
            ),
        )

        Assert.assertEquals(expectedState, state.copy(selectFrequencyUiState = state.selectFrequencyUiState.copy(frequencies = emptyMap())))
    }

    @Test
    fun `Initialized with the correct state when editing`() {
        every { savedStateHandle.get<ScheduleItem>(CreateUpdateEventFragment.SCHEDULE_ITEM) } returns ScheduleItem(
            itemId = "itemId",
            title = "title",
            startAt = LocalDateTime.now(clock).toApiString(),
            contextCode = "user_1"
        )

        createViewModel()
        val state = viewModel.uiState.value

        val expectedState = CreateUpdateEventUiState(
            title = "title",
            date = LocalDate.now(clock),
            startTime = LocalTime.now(clock),
            selectContextUiState = SelectContextUiState(
                selectedCanvasContext = User(1),
                canvasContexts = listOf(User(1))
            ),
            selectFrequencyUiState = SelectFrequencyUiState(
                selectedFrequency = "Does Not Repeat",
                frequencies = emptyMap()
            ),
        )

        Assert.assertEquals(expectedState, state.copy(selectFrequencyUiState = state.selectFrequencyUiState.copy(frequencies = emptyMap())))
    }

    @Test
    fun `Canvas context list gets fetched`() {
        val canvasContexts = listOf(User(1), Course(1), Course(2))
        coEvery { repository.getCanvasContexts() } returns canvasContexts

        createViewModel()

        val state = viewModel.uiState.value

        coVerify(exactly = 1) { repository.getCanvasContexts() }
        Assert.assertEquals(canvasContexts, state.selectContextUiState.canvasContexts)
    }

    @Test
    fun `Saves new event when creating`() = runTest {
        val createdEvent = ScheduleItem(
            itemId = "itemId",
            title = "Title",
            contextCode = "user_1",
            startAt = LocalDateTime.now(clock).toApiString(),
        )

        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-01"
        coEvery { repository.createEvent(any(), any(), any(), any(), any(), any(), any(), any()) } returns listOf(createdEvent)

        createViewModel()
        val events = mutableListOf<CreateUpdateEventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CreateUpdateEventAction.UpdateTitle("Title"))
        viewModel.handleAction(CreateUpdateEventAction.UpdateDate(LocalDate.now(clock)))
        viewModel.handleAction(CreateUpdateEventAction.UpdateStartTime(LocalTime.now(clock)))
        viewModel.handleAction(CreateUpdateEventAction.UpdateEndTime(LocalTime.now(clock).plusHours(1)))
        viewModel.handleAction(CreateUpdateEventAction.UpdateFrequency("Daily"))
        viewModel.handleAction(CreateUpdateEventAction.UpdateCanvasContext(Course(1)))
        viewModel.handleAction(CreateUpdateEventAction.UpdateLocation("Location"))
        viewModel.handleAction(CreateUpdateEventAction.UpdateAddress("Address"))
        viewModel.handleAction(CreateUpdateEventAction.UpdateDetails("Details"))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                "Title",
                "2024-04-10T11:00:00Z",
                "2024-04-10T12:00:00Z",
                "FREQ=DAILY;COUNT=365;INTERVAL=1",
                "course_1",
                "Location",
                "Address",
                "Details"
            )
        }

        coVerify(exactly = 1) {
            createUpdateEventViewModelBehavior.updateWidget()
        }

        val expectedEvent = CreateUpdateEventViewModelAction.RefreshCalendar
        Assert.assertEquals(expectedEvent, events.last())
        Assert.assertEquals(CreateUpdateEventViewModelAction.AnnounceEventCreation("Title"), events[events.size - 2])
    }

    @Test
    fun `Updates event when editing`() = runTest {
        val event = ScheduleItem(
            itemId = "1",
            title = "Title",
            contextCode = "user_1",
            startAt = LocalDateTime.now(clock).toApiString(),
            endAt = LocalDateTime.now(clock).toApiString()
        )

        every { savedStateHandle.get<ScheduleItem>(CreateUpdateEventFragment.SCHEDULE_ITEM) } returns event
        coEvery { repository.updateEvent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns listOf(
            event.copy(title = "Updated", startAt = LocalDateTime.now(clock).plusDays(1).toApiString())
        )

        createViewModel()
        val events = mutableListOf<CreateUpdateEventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CreateUpdateEventAction.UpdateTitle("Updated"))
        viewModel.handleAction(CreateUpdateEventAction.UpdateDate(LocalDate.now(clock).plusDays(1)))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.updateEvent(
                1,
                "Updated",
                "2024-04-11T11:00:00Z",
                "2024-04-11T11:00:00Z",
                "",
                "user_1",
                "",
                "",
                "",
                CalendarEventAPI.ModifyEventScope.ONE
            )
        }

        coVerify(exactly = 1) {
            createUpdateEventViewModelBehavior.updateWidget()
        }

        val expectedEvent = CreateUpdateEventViewModelAction.RefreshCalendarDays(
            listOf(
                LocalDate.of(2024, 4, 10),
                LocalDate.of(2024, 4, 11)
            )
        )
        Assert.assertEquals(expectedEvent, events.last())
        Assert.assertEquals(CreateUpdateEventViewModelAction.AnnounceEventUpdate("Updated"), events[events.size - 2])
    }

    @Test
    fun `Save event failed`() {
        every { resources.getString(R.string.eventSaveErrorMessage) } returns "Failed to save event"
        coEvery { repository.createEvent(any(), any(), any(), any(), any(), any(), any(), any()) } throws Exception()

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))
        Assert.assertEquals("Failed to save event", viewModel.uiState.value.errorSnack)

        viewModel.handleAction(CreateUpdateEventAction.SnackbarDismissed)
        Assert.assertEquals(null, viewModel.uiState.value.errorSnack)
    }

    @Test
    fun `Check unsaved changes when creating`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()
        val events = mutableListOf<CreateUpdateEventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CreateUpdateEventAction.CheckUnsavedChanges)
        Assert.assertFalse(viewModel.uiState.value.showUnsavedChangesDialog)

        viewModel.handleAction(CreateUpdateEventAction.UpdateTitle("Title"))

        viewModel.handleAction(CreateUpdateEventAction.CheckUnsavedChanges)
        Assert.assertTrue(viewModel.uiState.value.showUnsavedChangesDialog)

        viewModel.handleAction(CreateUpdateEventAction.NavigateBack)
        Assert.assertTrue(viewModel.uiState.value.canNavigateBack)
        Assert.assertEquals(CreateUpdateEventViewModelAction.NavigateBack, events.last())
    }

    @Test
    fun `Check unsaved changes when editing`() = runTest {
        every { savedStateHandle.get<ScheduleItem>(CreateUpdateEventFragment.SCHEDULE_ITEM) } returns ScheduleItem(
            itemId = "1",
            title = "Title",
            contextCode = "user_1",
            startAt = LocalDateTime.now(clock).toApiString()
        )

        createViewModel()
        val events = mutableListOf<CreateUpdateEventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CreateUpdateEventAction.CheckUnsavedChanges)
        Assert.assertFalse(viewModel.uiState.value.showUnsavedChangesDialog)

        viewModel.handleAction(CreateUpdateEventAction.UpdateTitle("Updated Title"))

        viewModel.handleAction(CreateUpdateEventAction.CheckUnsavedChanges)
        Assert.assertTrue(viewModel.uiState.value.showUnsavedChangesDialog)

        viewModel.handleAction(CreateUpdateEventAction.NavigateBack)
        Assert.assertTrue(viewModel.uiState.value.canNavigateBack)
        Assert.assertEquals(CreateUpdateEventViewModelAction.NavigateBack, events.last())
    }

    @Test
    fun `Check unsaved changes when no changes`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()
        val events = mutableListOf<CreateUpdateEventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CreateUpdateEventAction.CheckUnsavedChanges)
        Assert.assertFalse(viewModel.uiState.value.showUnsavedChangesDialog)
        Assert.assertTrue(viewModel.uiState.value.canNavigateBack)
        Assert.assertEquals(CreateUpdateEventViewModelAction.NavigateBack, events.last())
    }

    @Test
    fun `Back pressed when select calendar screen is showing`() = runTest {
        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.ShowSelectCalendarScreen)
        Assert.assertTrue(viewModel.uiState.value.selectContextUiState.show)

        viewModel.onBackPressed()
        Assert.assertFalse(viewModel.uiState.value.selectContextUiState.show)
    }

    @Test
    fun `Back pressed when custom frequency screen is showing`() = runTest {
        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.ShowCustomFrequencyScreen)
        Assert.assertTrue(viewModel.uiState.value.selectFrequencyUiState.customFrequencyUiState.show)

        viewModel.onBackPressed()
        Assert.assertFalse(viewModel.uiState.value.selectFrequencyUiState.customFrequencyUiState.show)
    }

    @Test
    fun `Back pressed when there are unsaved changes`() = runTest {
        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateTitle("Updated Title"))

        viewModel.onBackPressed()

        Assert.assertTrue(viewModel.uiState.value.showUnsavedChangesDialog)
    }

    @Test
    fun `Back pressed when there are no unsaved changes`() = runTest {
        createViewModel()
        val events = mutableListOf<CreateUpdateEventViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.onBackPressed()

        Assert.assertTrue(viewModel.uiState.value.canNavigateBack)
        Assert.assertEquals(CreateUpdateEventViewModelAction.NavigateBack, events.last())
    }

    @Test
    fun `Frequency updates correctly - Does not repeat`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateFrequency("Does Not Repeat"))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `Frequency updates correctly - Daily`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateFrequency("Daily"))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "FREQ=DAILY;COUNT=365;INTERVAL=1",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `Frequency updates correctly - Weekly`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateFrequency("Weekly on Wednesday"))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "FREQ=WEEKLY;COUNT=52;INTERVAL=1;BYDAY=WE",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `Frequency updates correctly - Monthly`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateFrequency("Monthly on the Second Wednesday"))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "FREQ=MONTHLY;COUNT=12;INTERVAL=1;BYDAY=WE;BYSETPOS=2",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `Frequency updates correctly - Annually`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateFrequency("Annually on Apr 10"))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "FREQ=YEARLY;COUNT=5;INTERVAL=1;BYMONTH=4;BYMONTHDAY=10",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `Frequency updates correctly - Weekdays`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateFrequency("Every Weekday (Monday to Friday)"))
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "FREQ=WEEKLY;COUNT=260;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `Frequency updates correctly - Custom`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencyQuantity(2))
        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencySelectedTimeUnitIndex(1))
        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencySelectedDays(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)))
        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencyOccurrences(15))
        viewModel.handleAction(CreateUpdateEventAction.SaveCustomFrequency)
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "FREQ=WEEKLY;COUNT=15;INTERVAL=2;BYDAY=MO,TU",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `Frequency updates correctly - Custom with date until`() = runTest {
        every { savedStateHandle.get<String>(CreateUpdateEventFragment.INITIAL_DATE) } returns "2024-04-10"

        createViewModel()

        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencyQuantity(2))
        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencySelectedTimeUnitIndex(1))
        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencySelectedDays(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)))
        viewModel.handleAction(CreateUpdateEventAction.UpdateCustomFrequencyEndDate(LocalDate.of(2024, 10, 7)))
        viewModel.handleAction(CreateUpdateEventAction.SaveCustomFrequency)
        viewModel.handleAction(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))

        coVerify(exactly = 1) {
            repository.createEvent(
                any(),
                any(),
                any(),
                "FREQ=WEEKLY;UNTIL=20241007T000000Z;INTERVAL=2;BYDAY=MO,TU",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    private fun createViewModel() {
        viewModel = CreateUpdateEventViewModel(savedStateHandle, resources, repository, apiPrefs, createUpdateEventViewModelBehavior)
    }

    private fun setupResources() {
        every { resources.getString(R.string.eventFrequencyDoesNotRepeat) } returns "Does Not Repeat"
        every { resources.getString(R.string.eventFrequencyMonthlyFirst) } returns "First"
        every { resources.getString(R.string.eventFrequencyMonthlySecond) } returns "Second"
        every { resources.getString(R.string.eventFrequencyMonthlyThird) } returns "Third"
        every { resources.getString(R.string.eventFrequencyMonthlyFourth) } returns "Fourth"
        every { resources.getString(R.string.eventFrequencyMonthlyLast) } returns "Last"
        every { resources.getString(R.string.eventFrequencyDaily) } returns "Daily"
        every { resources.getString(R.string.eventFrequencyWeekly, any()) } answers {
            val args = secondArg<Array<Any>>()
            "Weekly on ${args[0]}"
        }
        every { resources.getString(R.string.eventFrequencyMonthly, any(), any()) } answers {
            val args = secondArg<Array<Any>>()
            "Monthly on the ${args[0]} ${args[1]}"
        }
        every { resources.getString(R.string.eventFrequencyAnnually, any()) } answers {
            val args = secondArg<Array<Any>>()
            "Annually on ${args[0]}"
        }
        every { resources.getString(R.string.eventFrequencyWeekdays) } returns "Every Weekday (Monday to Friday)"
    }
}

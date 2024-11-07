/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.pandautils.features.elementary.importantdates

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.importantdates.itemviewmodels.ImportantDatesHeaderItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class ImportantDatesViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val courseManager: CourseManager = mockk(relaxed = true)
    private val calendarEventManager: CalendarEventManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)

    private val now = OffsetDateTime.now()

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic("kotlinx.coroutines.AwaitKt")

        val courses = listOf(
                Course(id = 1, name = "Course 1", courseColor = "#394B58"),
                Course(id = 2, name = "Course 2", courseColor = "#394B58")
        )

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { colorKeeper.getOrGenerateColor(any()) } returns ThemedColor(123)

        setupString()
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `ScheduleItems map correctly`() {
        val events = listOf(
                ScheduleItem(
                        itemId = "1",
                        title = "Event 1",
                        startAt = now.plusDays(1).toApiString(),
                        assignment = null,
                        contextCode = "course_1"
                ),
                ScheduleItem(
                        itemId = "2",
                        title = "Event 2",
                        startAt = now.plusDays(2).toApiString(),
                        assignment = null,
                        contextCode = "course_2"
                )
        )

        val assignments = listOf(
                Assignment(
                        id = 1,
                        name = "Assignment 1",
                        courseId = 1
                ),
                Assignment(
                        id = 2,
                        name = "Quiz 2",
                        courseId = 2,
                        submissionTypesRaw = listOf("online_quiz")
                )
        )
        val importantAssignments = listOf(
                ScheduleItem(
                        itemId = "3",
                        title = "Assignment 1",
                        startAt = now.plusDays(1).toApiString(),
                        assignment = assignments[0],
                        contextCode = "course_1"
                ),
                ScheduleItem(
                        itemId = "4",
                        title = "Quiz 2",
                        startAt = now.plusDays(2).toApiString(),
                        assignment = assignments[1],
                        contextCode = "course_2"
                )
        )

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(events)
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(importantAssignments)
        }

        val viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels
        assertEquals(2, items?.size)

        val expectedData1 = listOf(
                ImportantDatesItemViewData(
                        scheduleItemId = 3L,
                        title = "Assignment 1",
                        courseName = "Course 1",
                        icon = R.drawable.ic_assignment,
                        courseColor = ThemedColor(123)
                ),
                ImportantDatesItemViewData(
                        scheduleItemId = 1L,
                        title = "Event 1",
                        courseName = "Course 1",
                        courseColor = ThemedColor(123),
                        icon = R.drawable.ic_calendar
                )
        )
        val header1 = items!![0]
        assertEquals(SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(now.plusDays(1).toApiString().toDate()), header1.data.title)

        header1.itemViewModels.forEachIndexed { index, itemViewModel ->
            assertEquals(expectedData1[index], itemViewModel.data)
        }


        val expectedData2 = listOf(
                ImportantDatesItemViewData(
                        scheduleItemId = 4L,
                        title = "Quiz 2",
                        courseName = "Course 2",
                        icon = R.drawable.ic_quiz,
                        courseColor = ThemedColor(123)
                ),
                ImportantDatesItemViewData(
                        scheduleItemId = 2L,
                        title = "Event 2",
                        courseName = "Course 2",
                        courseColor = ThemedColor(123),
                        icon = R.drawable.ic_calendar
                )
        )
        val header2 = items[1]
        assertEquals(SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(now.plusDays(2).toApiString().toDate()), header2.data.title)

        header2.itemViewModels.forEachIndexed { index, itemViewModel ->
            assertEquals(expectedData2[index], itemViewModel.data)
        }

    }

    @Test
    fun `Open Assignment`() {
        val assignments = listOf(
                Assignment(
                        id = 1,
                        name = "Assignment 1",
                        courseId = 1
                )
        )
        val importantAssignments = listOf(
                ScheduleItem(
                        itemId = "3",
                        title = "Assignment 1",
                        startAt = now.plusDays(1).toApiString(),
                        assignment = assignments[0],
                        contextCode = "course_1"
                )
        )

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(importantAssignments)
        }

        val viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner, {})
        viewModel.events.observe(lifecycleOwner, {})

        viewModel.data.value?.itemViewModels!![0].itemViewModels[0].open()

        val canvasContext = CanvasContext.fromContextCode("course_1")
        val expectedData = ImportantDatesAction.OpenAssignment(canvasContext!!, 1)

        assertEquals(expectedData, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Open CalendarEvent`() {
        val events = listOf(
                ScheduleItem(
                        itemId = "1",
                        title = "Event 1",
                        startAt = now.plusDays(1).toApiString(),
                        assignment = null,
                        contextCode = "course_1"
                )
        )
        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(events)
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner, {})
        viewModel.events.observe(lifecycleOwner, {})

        viewModel.data.value?.itemViewModels!![0].itemViewModels[0].open()

        val canvasContext = CanvasContext.fromContextCode("course_1")
        val expectedData = ImportantDatesAction.OpenCalendarEvent(canvasContext!!, events[0])

        assertEquals(expectedData, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Empty State`() {
        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner, {})
        viewModel.state.observe(lifecycleOwner, {})

        val expectedState = ViewState.Empty(emptyTitle = R.string.importantDatesEmptyTitle, emptyImage = R.drawable.ic_panda_noannouncements)

        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun `Refresh`() {

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner, {})
        viewModel.state.observe(lifecycleOwner, {})

        val expectedState = ViewState.Empty(emptyTitle = R.string.importantDatesEmptyTitle, emptyImage = R.drawable.ic_panda_noannouncements)

        assertEquals(expectedState, viewModel.state.value)

        val events = listOf(
                ScheduleItem(
                        itemId = "1",
                        title = "Event 1",
                        startAt = now.plusDays(1).toApiString(),
                        assignment = null,
                        contextCode = "course_1"
                ),
                ScheduleItem(
                        itemId = "2",
                        title = "Event 2",
                        startAt = now.plusDays(2).toApiString(),
                        assignment = null,
                        contextCode = "course_2"
                )
        )

        val assignments = listOf(
                Assignment(
                        id = 1,
                        name = "Assignment 1",
                        courseId = 1
                ),
                Assignment(
                        id = 2,
                        name = "Quiz 2",
                        courseId = 2,
                        submissionTypesRaw = listOf("online_quiz")
                )
        )
        val importantAssignments = listOf(
                ScheduleItem(
                        itemId = "3",
                        title = "Assignment 1",
                        startAt = now.plusDays(1).toApiString(),
                        assignment = assignments[0],
                        contextCode = "course_1"
                ),
                ScheduleItem(
                        itemId = "4",
                        title = "Quiz 2",
                        startAt = now.plusDays(2).toApiString(),
                        assignment = assignments[1],
                        contextCode = "course_2"
                )
        )

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(events)
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(importantAssignments)
        }

        viewModel.refresh()
        assertEquals(ViewState.Success, viewModel.state.value)
        val items = viewModel.data.value?.itemViewModels
        assertEquals(2, items?.size)
    }

    @Test
    fun `Fetching courses error`() {
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        val expectedState = ViewState.Error("An unexpected error occurred.")
        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun `Fetching calendar events error`() {
        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        val expectedState = ViewState.Error("An unexpected error occurred.")
        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun `Fetching assignments error`() {
        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { calendarEventManager.getImportantDatesAsync(any(), any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        val viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        val expectedState = ViewState.Error("An unexpected error occurred.")
        assertEquals(expectedState, viewModel.state.value)
    }

    private fun createViewModel(): ImportantDatesViewModel {
        return ImportantDatesViewModel(
                courseManager,
                calendarEventManager,
                resources,
                colorKeeper
        )
    }

    private fun setupString() {
        every { resources.getString(R.string.importantDatesEmptyTitle) } returns "No important dates"
        every { resources.getString(R.string.errorOccurred) } returns "An unexpected error occurred."
    }
}
/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.student.widget.todo

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import com.instructure.student.widget.glance.WidgetState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ToDoWidgetUpdaterTest {

    private val repository: ToDoWidgetRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val updater = ToDoWidgetUpdater(repository, apiPrefs)

    @Before
    fun setUp() {
        ContextKeeper.appContext = mockk(relaxed = true)
        mockkObject(DateHelper)
        every { DateHelper.getPreferredTimeFormat(any()) } returns SimpleDateFormat("HH:mm", Locale.getDefault())
        every { context.getString(R.string.widgetDueDate, any()) } answers { "Due at ${secondArg<Array<Any>>()[0]}" }
        every { context.getString(R.string.userCalendarToDo) } returns "To Do"
        every { context.getString(R.string.widgetAllDay) } returns "All day"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Emits Loading state when called`() = runTest {
        val flow = updater.updateData(context)
        assertEquals(WidgetState.Loading, flow.first().state)
    }

    @Test
    fun `Emits NotLoggedIn state when user is null`() = runTest {
        every { apiPrefs.user } returns null

        val flow = updater.updateData(context)
        assertEquals(WidgetState.NotLoggedIn, flow.last().state)
    }

    @Test
    fun `Emits NotLoggedIn state when api call gets authorization error`() = runTest {
        coEvery { repository.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Fail(Failure.Authorization())

        val flow = updater.updateData(context)
        assertEquals(WidgetState.NotLoggedIn, flow.last().state)
    }

    @Test
    fun `Emits Error state when api calls fail`() = runTest {
        coEvery { repository.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Fail()

        val flow = updater.updateData(context)
        assertEquals(WidgetState.Error, flow.last().state)
    }

    @Test
    fun `Emits Empty state when api returns empty list`() = runTest {
        coEvery { repository.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Success(emptyList())

        val flow = updater.updateData(context)
        assertEquals(WidgetState.Empty, flow.last().state)
    }

    @Test
    fun `Emits Content when api returns data and maps correctly`() = runTest {
        val assignmentItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            date = createDate(2024, 1, 5, 2),
            courseId = 1
        )

        val toDoItem = createPlannerItem(
            plannableType = PlannableType.PLANNER_NOTE,
            date = createDate(2023, 10, 1, 12),
            plannableId = 2,
            userId = 1,
            startAt = createDate(2023, 10, 1, 12),
            endAt = createDate(2023, 10, 1, 13)
        )

        val calendarEvent = createPlannerItem(
            plannableType = PlannableType.CALENDAR_EVENT,
            date = createDate(2025, 5, 21, 12),
            userId = 1,
            startAt = createDate(2025, 5, 21, 12),
            endAt = createDate(2025, 5, 21, 12),
            contextName = "Context Name",
            allDay = true
        )

        coEvery { repository.getFavouriteCourses(any()) } returns listOf(Course(1, courseCode = "CODE"))
        coEvery { repository.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Success(listOf(assignmentItem, toDoItem, calendarEvent))

        val expected = ToDoWidgetUiState(
            WidgetState.Content,
            listOf(
                WidgetPlannerItem(
                    LocalDate.of(2024, 1, 5),
                    R.drawable.ic_assignment,
                    assignmentItem.canvasContext.color,
                    "CODE",
                    "Plannable 1",
                    "Due at 02:00",
                    "https://htmlurl.com"
                ),
                WidgetPlannerItem(
                    LocalDate.of(2023, 10, 1),
                    R.drawable.ic_todo,
                    apiPrefs.user.color,
                    "To Do",
                    "Plannable 2",
                    "12:00",
                    "/todos/2"
                ),
                WidgetPlannerItem(
                    LocalDate.of(2025, 5, 21),
                    R.drawable.ic_calendar,
                    apiPrefs.user.color,
                    "Context Name",
                    "Plannable 1",
                    "All day",
                    "/users/1/calendar_events/1"
                )
            )
        )
        val flow = updater.updateData(context)
        assertEquals(expected, flow.last())
    }

    private fun createPlannerItem(
        plannableType: PlannableType,
        date: Date = Date(),
        plannableId: Long = 1,
        courseId: Long? = null,
        userId: Long? = null,
        startAt: Date? = null,
        endAt: Date? = null,
        contextName: String? = null,
        allDay: Boolean = false
    ): PlannerItem {
        val plannable = Plannable(
            id = plannableId,
            title = "Plannable $plannableId",
            courseId = courseId,
            groupId = null,
            userId = userId,
            pointsPossible = null,
            dueAt = date,
            assignmentId = plannableId,
            todoDate = date.toApiString(),
            startAt = startAt,
            endAt = endAt,
            details = null,
            allDay = allDay
        )
        return PlannerItem(
            courseId = courseId,
            groupId = null,
            userId = userId,
            contextType = null,
            contextName = contextName,
            plannableType = plannableType,
            plannable = plannable,
            plannableDate = date,
            htmlUrl = "https://htmlurl.com",
            submissionState = SubmissionState(submitted = false),
            plannerOverride = PlannerOverride(plannableType = plannableType, plannableId = plannableId),
            newActivity = false
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

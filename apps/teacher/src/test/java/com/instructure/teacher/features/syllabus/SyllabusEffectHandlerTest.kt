/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.teacher.features.syllabus.ui.SyllabusView
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.Executors

private const val COURSE_ID: Long = 1L

class SyllabusEffectHandlerTest {
    private val view: SyllabusView = mockk(relaxed = true)
    private val eventConsumer: Consumer<SyllabusEvent> = mockk(relaxed = true)

    private val effectHandler = SyllabusEffectHandler().apply {
        view = this@SyllabusEffectHandlerTest.view
        connect(eventConsumer)
    }

    private val permissions = CanvasContextPermission(canManageContent = true)

    private lateinit var course: Course

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        course = Course(id = COURSE_ID)
    }

    @Test
    fun `LoadData with failed course results in failed DataLoaded`() {
        // Given
        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { CourseManager.getCourseSettingsAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        // When
        effectHandler.accept(SyllabusEffect.LoadData(COURSE_ID, false))

        // Then
        val expectedEvent = SyllabusEvent.DataLoaded(DataResult.Fail(), DataResult.Fail(), DataResult.Fail(), true)
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with failed schedule items should emit data loaded event with only the course`() {
        // Given
        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }
        every { CourseManager.getPermissionsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(permissions)
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), any(), any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        effectHandler.accept(SyllabusEffect.LoadData(COURSE_ID, false))

        // Then
        val expectedEvent = SyllabusEvent.DataLoaded(DataResult.Success(course), DataResult.Fail(), DataResult.Success(permissions), true)
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData should emit data loaded event with correct data when all data are received`() {
        // Given
        val itemCount = 3
        val now = Date().time
        val assignments = List(itemCount) {
            ScheduleItem(
                itemId = it.toString(),
                itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
                startAt = Date(now + (1000 * it)).toApiString())
        }
        val calendarEvents = List(itemCount) {
            ScheduleItem(
                itemId = (it + assignments.size).toString(),
                itemType = ScheduleItem.Type.TYPE_CALENDAR,
                startAt = Date(now + (1000 * it)).toApiString())
        }

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }
        every { CourseManager.getPermissionsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(permissions)
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(calendarEvents)
        }

        // When
        effectHandler.accept(SyllabusEffect.LoadData(COURSE_ID, false))

        // Then
        val sortedEvents = mutableListOf<ScheduleItem>()
        for (i in 0 until itemCount) {
            sortedEvents.add(assignments[i])
            sortedEvents.add(calendarEvents[i])
        }

        val expectedEvent = SyllabusEvent.DataLoaded(DataResult.Success(course), DataResult.Success(sortedEvents), DataResult.Success(permissions), true)
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with failed calendar events should emit data loaded event with only assignments`() {
        // Given
        val assignments = listOf(ScheduleItem(itemId = "123", itemType = ScheduleItem.Type.TYPE_ASSIGNMENT))

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }
        every { CourseManager.getPermissionsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(permissions)
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        effectHandler.accept(SyllabusEffect.LoadData(COURSE_ID, false))

        // Then
        val expectedEvent = SyllabusEvent.DataLoaded(DataResult.Success(course), DataResult.Success(assignments), DataResult.Success(permissions), true)
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with failed assignments should emit data loaded event with only calendar events`() {
        // Given
        val calendarEvents = listOf(ScheduleItem(itemId = "123", itemType = ScheduleItem.Type.TYPE_CALENDAR))

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }
        every { CourseManager.getPermissionsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(permissions)
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(calendarEvents)
        }

        // When
        effectHandler.accept(SyllabusEffect.LoadData(COURSE_ID, false))

        // Then
        val expectedEvent = SyllabusEvent.DataLoaded(DataResult.Success(course), DataResult.Success(calendarEvents), DataResult.Success(permissions), true)
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData should emit data loaded event with empty events if the summary is disallowed`() {
        // Given
        val assignments = listOf(ScheduleItem(itemId = "123", itemType = ScheduleItem.Type.TYPE_ASSIGNMENT))
        val calendarEvents = listOf(ScheduleItem(itemId = "1234", itemType = ScheduleItem.Type.TYPE_CALENDAR))

        mockkObject(CourseManager)
        every { CourseManager.getCourseSettingsAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = false))
        }
        every { CourseManager.getCourseWithSyllabusAsync(COURSE_ID, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getPermissionsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(permissions)
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(calendarEvents)
        }

        // When
        effectHandler.accept(SyllabusEffect.LoadData(COURSE_ID, false))

        // Then
        val expectedEvent = SyllabusEvent.DataLoaded(DataResult.Success(course), DataResult.Success(emptyList()), DataResult.Success(permissions))
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowAssignmentView results in view calling showAssignmentView`() {
        // Given
        val assignment = Assignment()

        // When
        effectHandler.accept(SyllabusEffect.ShowAssignmentView(assignment, course))

        // Then
        verify(timeout = 100) {
            view.showAssignmentView(assignment, course)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowScheduleItemView results in view calling showScheduleItemView`() {
        // Given
        val scheduleItem = ScheduleItem(itemId = "item")

        // When
        effectHandler.accept(SyllabusEffect.ShowScheduleItemView(scheduleItem, course))

        // Then
        verify(timeout = 100) {
            view.showScheduleItemView(scheduleItem, course)
        }

        confirmVerified(view)
    }

    @Test
    fun `OpenEditSyllabus results in opening the edit syllabus screen`() {
        // When
        effectHandler.accept(SyllabusEffect.OpenEditSyllabus(course, true))

        // Then
        verify(timeout = 100) {
            view.openEditSyllabus(course, true)
        }

        confirmVerified(view)
    }
}
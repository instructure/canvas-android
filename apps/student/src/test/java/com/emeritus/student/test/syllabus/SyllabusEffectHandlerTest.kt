/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.test.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.emeritus.student.mobius.syllabus.SyllabusEffect
import com.emeritus.student.mobius.syllabus.SyllabusEffectHandler
import com.emeritus.student.mobius.syllabus.SyllabusEvent
import com.emeritus.student.mobius.syllabus.ui.SyllabusView
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.Executors

class SyllabusEffectHandlerTest : Assert() {
    private val view: SyllabusView = mockk(relaxed = true)
    private val effectHandler =
        SyllabusEffectHandler().apply { view = this@SyllabusEffectHandlerTest.view }
    private val eventConsumer: Consumer<SyllabusEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    private var courseId: Long = 0L
    private lateinit var course: Course

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        courseId = 1L
        course = Course(id = courseId)
    }

    @Test
    fun `LoadData with failed course results in failed DataLoaded`() {
        val courseId = 1L
        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Fail(),
            DataResult.Fail()
        )

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { CourseManager.getCourseSettingsAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with failed schedule items results in partial success DataLoaded`() {
        val courseId = 1L
        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Success(course),
            DataResult.Fail()
        )

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), any(), any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData results in DataLoaded`() {
        val courseId = 1L
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
        val sortedEvents = mutableListOf<ScheduleItem>()
        for (i in 0 until itemCount) {
            sortedEvents.add(assignments[i])
            sortedEvents.add(calendarEvents[i])
        }

        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Success(course),
            DataResult.Success(sortedEvents)
        )

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(calendarEvents)
        }

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with failed calendar events results in partial success DataLoaded`() {
        val courseId = 1L
        val assignments = List(1) {
            ScheduleItem(itemId = it.toString(), itemType = ScheduleItem.Type.TYPE_ASSIGNMENT)
        }

        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Success(course),
            DataResult.Success(assignments)
        )

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with failed assignments results in partial success DataLoaded`() {
        val courseId = 1L
        val calendarEvents = List(1) {
            ScheduleItem(itemId = it.toString(), itemType = ScheduleItem.Type.TYPE_CALENDAR)
        }

        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Success(course),
            DataResult.Success(calendarEvents)
        )

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithSyllabusAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }
        every { CourseManager.getCourseSettingsAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(calendarEvents)
        }

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with disallowed summary results in DataLoaded with empty events`() {
        val courseId = 1L
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
        val sortedEvents = mutableListOf<ScheduleItem>()
        for (i in 0 until itemCount) {
            sortedEvents.add(assignments[i])
            sortedEvents.add(calendarEvents[i])
        }

        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Success(course),
            DataResult.Success(emptyList())
        )

        mockkObject(CourseManager)
        every { CourseManager.getCourseSettingsAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = false))
        }
        every { CourseManager.getCourseWithSyllabusAsync(courseId, false) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }

        mockkObject(CalendarEventManager)
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }
        every { CalendarEventManager.getCalendarEventsExhaustiveAsync(any(), CalendarEventAPI.CalendarEventType.CALENDAR, any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(calendarEvents)
        }

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowAssignmentView results in view calling showAssignmentView`() {
        val assignment = Assignment()
        connection.accept(SyllabusEffect.ShowAssignmentView(assignment, course))

        verify(timeout = 100) {
            view.showAssignmentView(assignment, course)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowScheduleItemView results in view calling showScheduleItemView`() {
        val scheduleItem = ScheduleItem(itemId = "item")
        connection.accept(SyllabusEffect.ShowScheduleItemView(scheduleItem, course))

        verify(timeout = 100) {
            view.showScheduleItemView(scheduleItem, course)
        }

        confirmVerified(view)
    }
}

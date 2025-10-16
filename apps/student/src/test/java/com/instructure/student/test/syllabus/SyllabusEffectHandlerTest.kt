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
package com.instructure.student.test.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.student.mobius.syllabus.SyllabusEffect
import com.instructure.student.mobius.syllabus.SyllabusEffectHandler
import com.instructure.student.mobius.syllabus.SyllabusEvent
import com.instructure.student.mobius.syllabus.SyllabusRepository
import com.instructure.student.mobius.syllabus.ui.SyllabusView
import com.spotify.mobius.functions.Consumer
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
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
    private val syllabusRepository: SyllabusRepository = mockk(relaxed = true)
    private val effectHandler =
        SyllabusEffectHandler(syllabusRepository).apply { view = this@SyllabusEffectHandlerTest.view }
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

        coEvery { syllabusRepository.getCourseWithSyllabus(any(), any()) } returns DataResult.Fail()

        coEvery { syllabusRepository.getCourseSettings(any(), any()) } returns CourseSettings(courseSummary = true)

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

        coEvery { syllabusRepository.getCourseWithSyllabus(any(), any()) } returns DataResult.Success(course)

        coEvery { syllabusRepository.getCourseSettings(any(), any()) } returns CourseSettings(courseSummary = true)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.SUB_ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        coEvery {
            syllabusRepository.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

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
                startAt = Date(now + (1000 * it)).toApiString()
            )
        }
        val calendarEvents = List(itemCount) {
            ScheduleItem(
                itemId = (it + assignments.size).toString(),
                itemType = ScheduleItem.Type.TYPE_CALENDAR,
                startAt = Date(now + (1000 * it)).toApiString()
            )
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

        coEvery { syllabusRepository.getCourseWithSyllabus(any(), any()) } returns DataResult.Success(course)

        coEvery { syllabusRepository.getCourseSettings(any(), any()) } returns CourseSettings(courseSummary = true)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(assignments)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.SUB_ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(calendarEvents)

        coEvery {
            syllabusRepository.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

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

        coEvery { syllabusRepository.getCourseWithSyllabus(courseId, false) } returns DataResult.Success(course)

        coEvery { syllabusRepository.getCourseSettings(courseId, false) } returns CourseSettings(courseSummary = true)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(assignments)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.SUB_ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        coEvery {
            syllabusRepository.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

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

        coEvery { syllabusRepository.getCourseWithSyllabus(courseId, false) } returns DataResult.Success(course)

        coEvery { syllabusRepository.getCourseSettings(courseId, false) } returns CourseSettings(courseSummary = true)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.SUB_ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(calendarEvents)

        coEvery {
            syllabusRepository.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

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
                startAt = Date(now + (1000 * it)).toApiString()
            )
        }
        val calendarEvents = List(itemCount) {
            ScheduleItem(
                itemId = (it + assignments.size).toString(),
                itemType = ScheduleItem.Type.TYPE_CALENDAR,
                startAt = Date(now + (1000 * it)).toApiString()
            )
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

        coEvery { syllabusRepository.getCourseSettings(courseId, false) } returns CourseSettings(courseSummary = false)

        coEvery { syllabusRepository.getCourseWithSyllabus(courseId, false) } returns DataResult.Success(course)

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with sub-assignments results in DataLoaded with sorted items`() {
        val courseId = 1L
        val itemCount = 2
        val now = Date().time
        val assignments = List(itemCount) {
            ScheduleItem(
                itemId = it.toString(),
                itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
                startAt = Date(now + (1000 * it)).toApiString()
            )
        }
        val subAssignments = List(itemCount) {
            ScheduleItem(
                itemId = (it + assignments.size).toString(),
                itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
                startAt = Date(now + (500 * it)).toApiString()
            )
        }
        val calendarEvents = List(itemCount) {
            ScheduleItem(
                itemId = (it + assignments.size + subAssignments.size).toString(),
                itemType = ScheduleItem.Type.TYPE_CALENDAR,
                startAt = Date(now + (1000 * it)).toApiString()
            )
        }

        val allItems = (assignments + subAssignments + calendarEvents).sortedBy { it.startAt }

        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Success(course),
            DataResult.Success(allItems)
        )

        coEvery { syllabusRepository.getCourseWithSyllabus(any(), any()) } returns DataResult.Success(course)

        coEvery { syllabusRepository.getCourseSettings(any(), any()) } returns CourseSettings(courseSummary = true)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(assignments)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.SUB_ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(subAssignments)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(calendarEvents)

        coEvery {
            syllabusRepository.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadData with failed sub-assignments results in partial success DataLoaded`() {
        val courseId = 1L
        val assignments = List(1) {
            ScheduleItem(itemId = it.toString(), itemType = ScheduleItem.Type.TYPE_ASSIGNMENT)
        }

        val expectedEvent = SyllabusEvent.DataLoaded(
            DataResult.Success(course),
            DataResult.Success(assignments)
        )

        coEvery { syllabusRepository.getCourseWithSyllabus(courseId, false) } returns DataResult.Success(course)

        coEvery { syllabusRepository.getCourseSettings(courseId, false) } returns CourseSettings(courseSummary = true)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(assignments)

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.SUB_ASSIGNMENT,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        coEvery {
            syllabusRepository.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        coEvery {
            syllabusRepository.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(emptyList())

        connection.accept(SyllabusEffect.LoadData(courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowAssignmentView results in view calling showAssignmentView`() {
        val assignment = Assignment()
        connection.accept(SyllabusEffect.ShowAssignmentView(assignment.id, course))

        verify(timeout = 100) {
            view.showAssignmentView(assignment.id, course)
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

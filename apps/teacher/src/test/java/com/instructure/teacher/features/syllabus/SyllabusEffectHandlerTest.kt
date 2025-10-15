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

import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.teacher.features.syllabus.ui.SyllabusView
import com.spotify.mobius.functions.Consumer
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.concurrent.Executors

private const val COURSE_ID: Long = 1L

class SyllabusEffectHandlerTest {
    private val view: SyllabusView = mockk(relaxed = true)
    private val eventConsumer: Consumer<SyllabusEvent> = mockk(relaxed = true)
    private val repository: SyllabusRepository = mockk(relaxed = true)

    private val effectHandler = SyllabusEffectHandler(repository).apply {
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
        mockkObject(ApiPrefs)
        every { ApiPrefs.fullDomain } returns "https://test.instructure.com"
    }

    @After
    fun tearDown() {
        unmockkAll()
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

        coEvery { repository.getCalendarEvents(any(), any(), any(), any(), any(), any()) } returns DataResult.Fail()
        coEvery { repository.getPlannerItems(any(), any(), any(), any(), any()) } returns DataResult.Fail()

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

        coEvery { repository.getCalendarEvents(true, any(), any(), any(), any(), false) } returnsMany listOf(
            DataResult.Success(assignments),
            DataResult.Success(calendarEvents)
        )

        coEvery { repository.getPlannerItems(any(), any(), any(), any(), any()) } returns DataResult.Success(emptyList())

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

        coEvery { repository.getCalendarEvents(true, any(), any(), any(), any(), false) } returnsMany listOf(
            DataResult.Success(assignments),
            DataResult.Fail()
        )

        coEvery { repository.getPlannerItems(any(), any(), any(), any(), any()) } returns DataResult.Success(emptyList())

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

        coEvery { repository.getCalendarEvents(true, any(), any(), any(), any(), false) } returnsMany listOf(
            DataResult.Fail(),
            DataResult.Success(calendarEvents)
        )

        coEvery { repository.getPlannerItems(any(), any(), any(), any(), any()) } returns DataResult.Success(emptyList())

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
    fun `LoadData should filter out assignment and calendar event type planner items to avoid duplicates`() {
        // Given
        val now = Date().time
        val assignment = ScheduleItem(
            itemId = "123",
            itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
            startAt = Date(now + 1000).toApiString()
        )
        val calendarEvent = ScheduleItem(
            itemId = "789",
            itemType = ScheduleItem.Type.TYPE_CALENDAR,
            startAt = Date(now + 2000).toApiString()
        )
        val plannerAssignment = PlannerItem(
            courseId = COURSE_ID,
            groupId = null,
            userId = null,
            contextType = "Course",
            contextName = "Test Course",
            plannableType = PlannableType.ASSIGNMENT,
            plannable = Plannable(
                id = 123,
                title = "Assignment",
                courseId = COURSE_ID,
                groupId = null,
                userId = null,
                pointsPossible = null,
                dueAt = null,
                assignmentId = null,
                todoDate = null,
                startAt = null,
                endAt = null,
                details = null,
                allDay = null
            ),
            plannableDate = Date(),
            htmlUrl = null,
            submissionState = null,
            newActivity = false
        )
        val plannerCalendarEvent = PlannerItem(
            courseId = COURSE_ID,
            groupId = null,
            userId = null,
            contextType = "Course",
            contextName = "Test Course",
            plannableType = PlannableType.CALENDAR_EVENT,
            plannable = Plannable(
                id = 789,
                title = "Calendar Event",
                courseId = COURSE_ID,
                groupId = null,
                userId = null,
                pointsPossible = null,
                dueAt = null,
                assignmentId = null,
                todoDate = null,
                startAt = null,
                endAt = null,
                details = null,
                allDay = null
            ),
            plannableDate = Date(),
            htmlUrl = null,
            submissionState = null,
            newActivity = false
        )
        val plannerQuiz = PlannerItem(
            courseId = COURSE_ID,
            groupId = null,
            userId = null,
            contextType = "Course",
            contextName = "Test Course",
            plannableType = PlannableType.QUIZ,
            plannable = Plannable(
                id = 456,
                title = "Quiz",
                courseId = COURSE_ID,
                groupId = null,
                userId = null,
                pointsPossible = null,
                dueAt = null,
                assignmentId = null,
                todoDate = Date(now + 3000).toApiString(),
                startAt = null,
                endAt = null,
                details = null,
                allDay = null
            ),
            plannableDate = Date(now + 3000),
            htmlUrl = null,
            submissionState = null,
            newActivity = false
        )

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

        coEvery { repository.getCalendarEvents(true, any(), any(), any(), any(), false) } returnsMany listOf(
            DataResult.Success(listOf(assignment)),
            DataResult.Success(listOf(calendarEvent))
        )

        coEvery { repository.getPlannerItems(any(), any(), any(), any(), any()) } returns DataResult.Success(listOf(plannerAssignment, plannerCalendarEvent, plannerQuiz))

        // When
        effectHandler.accept(SyllabusEffect.LoadData(COURSE_ID, false))

        // Then
        // Capture what was actually called
        val slot = slot<SyllabusEvent.DataLoaded>()
        verify(timeout = 100) {
            eventConsumer.accept(capture(slot))
        }

        // Verify the captured event
        val event = slot.captured
        assert(event.course is DataResult.Success)
        assert(event.events is DataResult.Success)
        assert(event.permissionsResult is DataResult.Success)
        assert(event.summaryAllowed == true)

        // Verify we have exactly 3 items: assignment, calendar event, and quiz (no planner duplicates)
        val items = event.events.dataOrNull!!
        assert(items.size == 3) { "Expected 3 items, got ${items.size}" }
        assert(items.count { it.itemType == ScheduleItem.Type.TYPE_ASSIGNMENT } == 1) { "Expected 1 assignment" }
        assert(items.count { it.itemType == ScheduleItem.Type.TYPE_CALENDAR } == 1) { "Expected 1 calendar event" }
        assert(items.count { it.itemType == ScheduleItem.Type.TYPE_SYLLABUS } == 1) { "Expected 1 quiz/syllabus item" }

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
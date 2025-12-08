/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerItemDetails
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

class PlannerItemExtensionsTest {

    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkObject(ApiPrefs)
        every { ApiPrefs.fullDomain } returns "https://test.instructure.com"
        every { apiPrefs.fullDomain } returns "https://example.instructure.com"

        mockkObject(DateHelper)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    companion object {
        // Static dates for predictable testing
        private val TEST_DATE = createDate(2025, Calendar.JANUARY, 15, 14, 30) // Jan 15, 2025 at 2:30 PM
        private val TEST_DATE_2 = createDate(2025, Calendar.JANUARY, 15, 15, 30) // Jan 15, 2025 at 3:30 PM

        private fun createDate(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Date {
            return Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }
    }

    // todoHtmlUrl tests
    @Test
    fun `todoHtmlUrl returns correct URL`() {
        val plannerItem = createPlannerItem(plannableId = 12345L)

        val result = plannerItem.todoHtmlUrl(ApiPrefs)

        assertEquals("https://test.instructure.com/todos/12345", result)
    }

    // getIconForPlannerItem tests
    @Test
    fun `getIconForPlannerItem returns assignment icon for ASSIGNMENT type`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.ASSIGNMENT)

        val result = plannerItem.getIconForPlannerItem()

        assertEquals(R.drawable.ic_assignment, result)
    }

    @Test
    fun `getIconForPlannerItem returns quiz icon for QUIZ type`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.QUIZ)

        val result = plannerItem.getIconForPlannerItem()

        assertEquals(R.drawable.ic_quiz, result)
    }

    @Test
    fun `getIconForPlannerItem returns calendar icon for CALENDAR_EVENT type`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT)

        val result = plannerItem.getIconForPlannerItem()

        assertEquals(R.drawable.ic_calendar, result)
    }

    @Test
    fun `getIconForPlannerItem returns discussion icon for DISCUSSION_TOPIC type`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.DISCUSSION_TOPIC)

        val result = plannerItem.getIconForPlannerItem()

        assertEquals(R.drawable.ic_discussion, result)
    }

    @Test
    fun `getIconForPlannerItem returns discussion icon for SUB_ASSIGNMENT type`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.SUB_ASSIGNMENT)

        val result = plannerItem.getIconForPlannerItem()

        assertEquals(R.drawable.ic_discussion, result)
    }

    @Test
    fun `getIconForPlannerItem returns todo icon for PLANNER_NOTE type`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.PLANNER_NOTE)

        val result = plannerItem.getIconForPlannerItem()

        assertEquals(R.drawable.ic_todo, result)
    }

    @Test
    fun `getIconForPlannerItem returns calendar icon for unknown type`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.ANNOUNCEMENT)

        val result = plannerItem.getIconForPlannerItem()

        assertEquals(R.drawable.ic_calendar, result)
    }

    // getDateTextForPlannerItem tests
    @Test
    fun `getDateTextForPlannerItem returns formatted time for PLANNER_NOTE with todoDate`() {
        val plannable = createPlannable(todoDate = TEST_DATE.toApiString())
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.PLANNER_NOTE,
            plannable = plannable
        )

        every { DateHelper.getFormattedTime(context, TEST_DATE) } returns "2:30 PM"

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertEquals("2:30 PM", result)
    }

    @Test
    fun `getDateTextForPlannerItem returns null for PLANNER_NOTE without todoDate`() {
        val plannable = createPlannable(todoDate = null)
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.PLANNER_NOTE,
            plannable = plannable
        )

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertNull(result)
    }

    @Test
    fun `getDateTextForPlannerItem returns all day text for all-day CALENDAR_EVENT`() {
        val plannable = createPlannable(
            startAt = TEST_DATE,
            endAt = TEST_DATE,
            allDay = true
        )
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.CALENDAR_EVENT,
            plannable = plannable
        )

        every { context.getString(R.string.widgetAllDay) } returns "All Day"

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertEquals("All Day", result)
    }

    @Test
    fun `getDateTextForPlannerItem returns single time for CALENDAR_EVENT with same start and end`() {
        val plannable = createPlannable(
            startAt = TEST_DATE,
            endAt = TEST_DATE,
            allDay = false
        )
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.CALENDAR_EVENT,
            plannable = plannable
        )

        every { DateHelper.getFormattedTime(context, TEST_DATE) } returns "2:30 PM"

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertEquals("2:30 PM", result)
    }

    @Test
    fun `getDateTextForPlannerItem returns time range for CALENDAR_EVENT with different times`() {
        val plannable = createPlannable(
            startAt = TEST_DATE,
            endAt = TEST_DATE_2,
            allDay = false
        )
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.CALENDAR_EVENT,
            plannable = plannable
        )

        every { DateHelper.getFormattedTime(context, TEST_DATE) } returns "2:30 PM"
        every { DateHelper.getFormattedTime(context, TEST_DATE_2) } returns "3:30 PM"
        every { context.getString(R.string.widgetFromTo, "2:30 PM", "3:30 PM") } returns "2:30 PM - 3:30 PM"

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertEquals("2:30 PM - 3:30 PM", result)
    }

    @Test
    fun `getDateTextForPlannerItem returns null for CALENDAR_EVENT without dates`() {
        val plannable = createPlannable(
            startAt = null,
            endAt = null
        )
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.CALENDAR_EVENT,
            plannable = plannable
        )

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertNull(result)
    }

    @Test
    fun `getDateTextForPlannerItem returns formatted time for ASSIGNMENT with dueAt`() {
        val plannable = createPlannable(dueAt = TEST_DATE)
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            plannable = plannable
        )

        every { DateHelper.getFormattedTime(context, TEST_DATE) } returns "2:30 PM"

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertEquals("2:30 PM", result)
    }

    @Test
    fun `getDateTextForPlannerItem returns null for ASSIGNMENT without dueAt`() {
        val plannable = createPlannable(dueAt = null)
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            plannable = plannable
        )

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertNull(result)
    }

    // getContextNameForPlannerItem tests
    @Test
    fun `getContextNameForPlannerItem returns User To-Do for PLANNER_NOTE without contextName`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.PLANNER_NOTE,
            contextName = null
        )

        every { context.getString(R.string.userCalendarToDo) } returns "User To-Do"

        val result = plannerItem.getContextNameForPlannerItem(context, emptyList())

        assertEquals("User To-Do", result)
    }

    @Test
    fun `getContextNameForPlannerItem returns course todo for PLANNER_NOTE with contextName`() {
        val course = Course(id = 123L, courseCode = "CS101")
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.PLANNER_NOTE,
            courseId = 123L,
            contextName = "Computer Science"
        )

        every { context.getString(R.string.courseToDo, "CS101") } returns "CS101 To-Do"

        val result = plannerItem.getContextNameForPlannerItem(context, listOf(course))

        assertEquals("CS101 To-Do", result)
    }

    @Test
    fun `getContextNameForPlannerItem returns course code for Course context`() {
        val course = Course(id = 123L, courseCode = "CS101")
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            courseId = 123L
        )

        val result = plannerItem.getContextNameForPlannerItem(context, listOf(course))

        assertEquals("CS101", result)
    }

    @Test
    fun `getContextNameForPlannerItem returns empty string for Course context without matching course`() {
        val course = Course(id = 999L, courseCode = "CS101")
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            courseId = 123L
        )

        val result = plannerItem.getContextNameForPlannerItem(context, listOf(course))

        assertEquals("", result)
    }

    @Test
    fun `getContextNameForPlannerItem returns contextName for non-Course context`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            userId = 456L,
            contextName = "Personal"
        )

        val result = plannerItem.getContextNameForPlannerItem(context, emptyList())

        assertEquals("Personal", result)
    }

    // getTagForPlannerItem tests
    @Test
    fun `getTagForPlannerItem returns reply to topic for REPLY_TO_TOPIC tag`() {
        val plannable = createPlannable(subAssignmentTag = Const.REPLY_TO_TOPIC)
        val plannerItem = createPlannerItem(plannable = plannable)

        every { context.getString(R.string.reply_to_topic) } returns "Reply to Topic"

        val result = plannerItem.getTagForPlannerItem(context)

        assertEquals("Reply to Topic", result)
    }

    @Test
    fun `getTagForPlannerItem returns additional replies for REPLY_TO_ENTRY with count`() {
        val details = PlannerItemDetails(replyRequiredCount = 3)
        val plannable = createPlannable(subAssignmentTag = Const.REPLY_TO_ENTRY)
        val plannerItem = createPlannerItem(
            plannable = plannable,
            plannableItemDetails = details
        )

        every { context.getString(R.string.additional_replies, 3) } returns "3 Additional Replies"

        val result = plannerItem.getTagForPlannerItem(context)

        assertEquals("3 Additional Replies", result)
    }

    @Test
    fun `getTagForPlannerItem returns null for REPLY_TO_ENTRY without count`() {
        val plannable = createPlannable(subAssignmentTag = Const.REPLY_TO_ENTRY)
        val plannerItem = createPlannerItem(
            plannable = plannable,
            plannableItemDetails = null
        )

        val result = plannerItem.getTagForPlannerItem(context)

        assertNull(result)
    }

    @Test
    fun `getTagForPlannerItem returns null for no subAssignmentTag`() {
        val plannable = createPlannable(subAssignmentTag = null)
        val plannerItem = createPlannerItem(plannable = plannable)

        val result = plannerItem.getTagForPlannerItem(context)

        assertNull(result)
    }

    // isComplete tests
    @Test
    fun `isComplete returns true when plannerOverride markedComplete is true`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            plannerOverride = com.instructure.canvasapi2.models.PlannerOverride(
                plannableType = PlannableType.ASSIGNMENT,
                plannableId = 1L,
                markedComplete = true
            )
        )

        val result = plannerItem.isComplete()

        assertEquals(true, result)
    }

    @Test
    fun `isComplete returns false when plannerOverride markedComplete is false`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            plannerOverride = com.instructure.canvasapi2.models.PlannerOverride(
                plannableType = PlannableType.ASSIGNMENT,
                plannableId = 1L,
                markedComplete = false
            )
        )

        val result = plannerItem.isComplete()

        assertEquals(false, result)
    }

    @Test
    fun `isComplete returns true for ASSIGNMENT when submitted`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = true)
        )

        val result = plannerItem.isComplete()

        assertEquals(true, result)
    }

    @Test
    fun `isComplete returns false for ASSIGNMENT when not submitted`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
        )

        val result = plannerItem.isComplete()

        assertEquals(false, result)
    }

    @Test
    fun `isComplete returns false for CALENDAR_EVENT without override`() {
        val plannerItem = createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT)

        val result = plannerItem.isComplete()

        assertEquals(false, result)
    }

    @Test
    fun `isComplete returns true for QUIZ when submitted`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.QUIZ,
            submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = true)
        )

        val result = plannerItem.isComplete()

        assertEquals(true, result)
    }

    @Test
    fun `isComplete returns false for QUIZ when not submitted`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.QUIZ,
            submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
        )

        val result = plannerItem.isComplete()

        assertEquals(false, result)
    }

    @Test
    fun `isComplete returns false for QUIZ with null submission state`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.QUIZ,
            submissionState = null
        )

        val result = plannerItem.isComplete()

        assertEquals(false, result)
    }

    @Test
    fun `isComplete returns true for QUIZ with plannerOverride marked complete`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.QUIZ,
            plannerOverride = com.instructure.canvasapi2.models.PlannerOverride(
                plannableType = PlannableType.QUIZ,
                plannableId = 1L,
                markedComplete = true
            ),
            submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
        )

        val result = plannerItem.isComplete()

        assertEquals(true, result)
    }

    // filterByToDoFilters tests
    @Test
    fun `filterByToDoFilters filters out PLANNER_NOTE when personalTodos is false`() {
        val filters = createToDoFilterEntity(personalTodos = false)
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.PLANNER_NOTE),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT),
            createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT)
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(2, result.size)
        assertEquals(PlannableType.ASSIGNMENT, result[0].plannableType)
        assertEquals(PlannableType.CALENDAR_EVENT, result[1].plannableType)
    }

    @Test
    fun `filterByToDoFilters includes PLANNER_NOTE when personalTodos is true`() {
        val filters = createToDoFilterEntity(personalTodos = true)
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.PLANNER_NOTE),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT)
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(2, result.size)
        assertEquals(PlannableType.PLANNER_NOTE, result[0].plannableType)
    }

    @Test
    fun `filterByToDoFilters filters out CALENDAR_EVENT when calendarEvents is false`() {
        val filters = createToDoFilterEntity(calendarEvents = false)
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT),
            createPlannerItem(plannableType = PlannableType.PLANNER_NOTE)
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(2, result.size)
        assertEquals(PlannableType.ASSIGNMENT, result[0].plannableType)
        assertEquals(PlannableType.PLANNER_NOTE, result[1].plannableType)
    }

    @Test
    fun `filterByToDoFilters includes CALENDAR_EVENT when calendarEvents is true`() {
        val filters = createToDoFilterEntity(calendarEvents = true)
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT)
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(2, result.size)
        assertEquals(PlannableType.CALENDAR_EVENT, result[0].plannableType)
    }

    @Test
    fun `filterByToDoFilters filters out completed items when showCompleted is false`() {
        val filters = createToDoFilterEntity(showCompleted = false)
        val items = listOf(
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = true)
            ),
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
            ),
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                plannerOverride = com.instructure.canvasapi2.models.PlannerOverride(
                    plannableType = PlannableType.ASSIGNMENT,
                    plannableId = 1L,
                    markedComplete = true
                )
            )
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(1, result.size)
        assertEquals(false, result[0].isComplete())
    }

    @Test
    fun `filterByToDoFilters includes completed items when showCompleted is true`() {
        val filters = createToDoFilterEntity(showCompleted = true)
        val items = listOf(
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = true)
            ),
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
            )
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(2, result.size)
    }

    @Test
    fun `filterByToDoFilters filters to favorite courses only when favoriteCourses is true`() {
        val filters = createToDoFilterEntity(favoriteCourses = true)
        val courses = listOf(
            Course(id = 1L, isFavorite = true),
            Course(id = 2L, isFavorite = false),
            Course(id = 3L, isFavorite = true)
        )
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 1L),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 2L),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 3L)
        )

        val result = items.filterByToDoFilters(filters, courses)

        assertEquals(2, result.size)
        assertEquals(1L, result[0].courseId)
        assertEquals(3L, result[1].courseId)
    }

    @Test
    fun `filterByToDoFilters includes all courses when favoriteCourses is false`() {
        val filters = createToDoFilterEntity(favoriteCourses = false)
        val courses = listOf(
            Course(id = 1L, isFavorite = true),
            Course(id = 2L, isFavorite = false)
        )
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 1L),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 2L)
        )

        val result = items.filterByToDoFilters(filters, courses)

        assertEquals(2, result.size)
    }

    @Test
    fun `filterByToDoFilters includes items with no matching course when favoriteCourses is true`() {
        val filters = createToDoFilterEntity(favoriteCourses = true)
        val courses = listOf(
            Course(id = 1L, isFavorite = true)
        )
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 1L),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 999L)
        )

        val result = items.filterByToDoFilters(filters, courses)

        // Both items should be included: favorite course and item with no matching course
        assertEquals(2, result.size)
        assertEquals(1L, result[0].courseId)
        assertEquals(999L, result[1].courseId)
    }

    @Test
    fun `filterByToDoFilters applies multiple filters correctly`() {
        val filters = createToDoFilterEntity(
            personalTodos = false,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = true
        )
        val courses = listOf(
            Course(id = 1L, isFavorite = true),
            Course(id = 2L, isFavorite = false)
        )
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.PLANNER_NOTE),
            createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT),
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                courseId = 1L,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = true)
            ),
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                courseId = 2L,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
            ),
            createPlannerItem(
                plannableType = PlannableType.ASSIGNMENT,
                courseId = 1L,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
            )
        )

        val result = items.filterByToDoFilters(filters, courses)

        assertEquals(1, result.size)
        assertEquals(PlannableType.ASSIGNMENT, result[0].plannableType)
        assertEquals(1L, result[0].courseId)
        assertEquals(false, result[0].isComplete())
    }

    @Test
    fun `filterByToDoFilters returns empty list when all items are filtered out`() {
        val filters = createToDoFilterEntity(
            personalTodos = false,
            calendarEvents = false
        )
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.PLANNER_NOTE),
            createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT)
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `filterByToDoFilters returns all items when no filters are applied`() {
        val filters = createToDoFilterEntity(
            personalTodos = true,
            calendarEvents = true,
            showCompleted = true,
            favoriteCourses = false
        )
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.PLANNER_NOTE),
            createPlannerItem(plannableType = PlannableType.CALENDAR_EVENT),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT)
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(3, result.size)
    }

    @Test
    fun `filterByToDoFilters handles empty input list`() {
        val filters = createToDoFilterEntity()

        val result = emptyList<PlannerItem>().filterByToDoFilters(filters, emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `filterByToDoFilters includes null courseId items when filtering favorites`() {
        val filters = createToDoFilterEntity(favoriteCourses = true)
        val courses = listOf(Course(id = 1L, isFavorite = true))
        val items = listOf(
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = null),
            createPlannerItem(plannableType = PlannableType.ASSIGNMENT, courseId = 1L)
        )

        val result = items.filterByToDoFilters(filters, courses)

        // Both items should be included: null courseId and favorite course
        assertEquals(2, result.size)
        assertEquals(null, result[0].courseId)
        assertEquals(1L, result[1].courseId)
    }

    @Test
    fun `filterByToDoFilters uses plannable courseId for PLANNER_NOTE when item courseId is null`() {
        val filters = createToDoFilterEntity(favoriteCourses = true)
        val courses = listOf(
            Course(id = 1L, isFavorite = true),
            Course(id = 2L, isFavorite = false)
        )
        val items = listOf(
            createPlannerItem(
                plannableType = PlannableType.PLANNER_NOTE,
                courseId = null,
                plannable = createPlannable(courseId = 1L)
            ),
            createPlannerItem(
                plannableType = PlannableType.PLANNER_NOTE,
                courseId = null,
                plannable = createPlannable(courseId = 2L)
            )
        )

        val result = items.filterByToDoFilters(filters, courses)

        // Only PLANNER_NOTE with favorite course (via plannable.courseId) should be included
        assertEquals(1, result.size)
        assertEquals(1L, result[0].plannable.courseId)
    }

    @Test
    fun `filterByToDoFilters prefers item courseId over plannable courseId for PLANNER_NOTE`() {
        val filters = createToDoFilterEntity(favoriteCourses = true)
        val courses = listOf(
            Course(id = 1L, isFavorite = true),
            Course(id = 2L, isFavorite = false)
        )
        val items = listOf(
            createPlannerItem(
                plannableType = PlannableType.PLANNER_NOTE,
                courseId = 2L, // item.courseId is set (non-favorite)
                plannable = createPlannable(courseId = 1L) // plannable.courseId is favorite
            )
        )

        val result = items.filterByToDoFilters(filters, courses)

        // Should use item.courseId (2L) which is not favorite, so item is filtered out
        assertEquals(0, result.size)
    }

    @Test
    fun `filterByToDoFilters handles DISCUSSION_TOPIC completion`() {
        val filters = createToDoFilterEntity(showCompleted = false)
        val items = listOf(
            createPlannerItem(
                plannableType = PlannableType.DISCUSSION_TOPIC,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = true)
            ),
            createPlannerItem(
                plannableType = PlannableType.DISCUSSION_TOPIC,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
            )
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(1, result.size)
        assertEquals(false, result[0].isComplete())
    }

    @Test
    fun `filterByToDoFilters handles SUB_ASSIGNMENT completion`() {
        val filters = createToDoFilterEntity(showCompleted = false)
        val items = listOf(
            createPlannerItem(
                plannableType = PlannableType.SUB_ASSIGNMENT,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = true)
            ),
            createPlannerItem(
                plannableType = PlannableType.SUB_ASSIGNMENT,
                submissionState = com.instructure.canvasapi2.models.SubmissionState(submitted = false)
            )
        )

        val result = items.filterByToDoFilters(filters, emptyList())

        assertEquals(1, result.size)
        assertEquals(false, result[0].isComplete())
    }

    @Test
    fun `getUrl returns full URL for calendar event`() {
        val plannerItem = createPlannerItem(
            courseId = 123,
            plannableType = PlannableType.CALENDAR_EVENT,
            plannableId = 456
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/courses/123/calendar_events/456", result)
    }

    @Test
    fun `getUrl returns full URL for planner note`() {
        val plannerItem = createPlannerItem(
            userId = 1,
            plannableType = PlannableType.PLANNER_NOTE,
            plannableId = 789
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/todos/789", result)
    }

    @Test
    fun `getUrl returns htmlUrl for assignment when htmlUrl is set`() {
        val plannerItem = createPlannerItem(
            courseId = 123,
            plannableType = PlannableType.ASSIGNMENT,
            plannableId = 456,
            htmlUrl = "https://example.instructure.com/courses/123/assignments/456"
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/courses/123/assignments/456", result)
    }

    @Test
    fun `getUrl returns htmlUrl for quiz when htmlUrl is set`() {
        val plannerItem = createPlannerItem(
            courseId = 123,
            plannableType = PlannableType.QUIZ,
            plannableId = 456,
            htmlUrl = "https://example.instructure.com/courses/123/quizzes/456"
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/courses/123/quizzes/456", result)
    }

    @Test
    fun `getUrl returns htmlUrl for discussion when htmlUrl is set`() {
        val plannerItem = createPlannerItem(
            courseId = 123,
            plannableType = PlannableType.DISCUSSION_TOPIC,
            plannableId = 456,
            htmlUrl = "https://example.instructure.com/courses/123/discussion_topics/456"
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/courses/123/discussion_topics/456", result)
    }

    @Test
    fun `getUrl returns empty string when htmlUrl is null for assignment`() {
        val plannerItem = createPlannerItem(
            courseId = 123,
            plannableType = PlannableType.ASSIGNMENT,
            plannableId = 456,
            htmlUrl = null
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("", result)
    }

    @Test
    fun `getUrl handles calendar event with group context`() {
        val plannerItem = createPlannerItem(
            groupId = 789,
            plannableType = PlannableType.CALENDAR_EVENT,
            plannableId = 456
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/groups/789/calendar_events/456", result)
    }

    @Test
    fun `getUrl handles planner note with relative path correctly`() {
        val plannerItem = createPlannerItem(
            userId = 1,
            plannableType = PlannableType.PLANNER_NOTE,
            plannableId = 123
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/todos/123", result)
    }

    @Test
    fun `getUrl does not double-prepend domain when htmlUrl already contains domain`() {
        val plannerItem = createPlannerItem(
            courseId = 123,
            plannableType = PlannableType.ASSIGNMENT,
            plannableId = 456,
            htmlUrl = "https://example.instructure.com/courses/123/assignments/456"
        )

        val result = plannerItem.getUrl(apiPrefs)

        assertEquals("https://example.instructure.com/courses/123/assignments/456", result)
    }

    // Helper functions to create test objects with default values
    private fun createToDoFilterEntity(
        personalTodos: Boolean = true,
        calendarEvents: Boolean = true,
        showCompleted: Boolean = true,
        favoriteCourses: Boolean = false
    ): com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity {
        return com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = personalTodos,
            calendarEvents = calendarEvents,
            showCompleted = showCompleted,
            favoriteCourses = favoriteCourses
        )
    }
    private fun createPlannable(
        id: Long = 1L,
        title: String = "Test",
        courseId: Long? = null,
        groupId: Long? = null,
        userId: Long? = null,
        pointsPossible: Double? = null,
        dueAt: Date? = null,
        assignmentId: Long? = null,
        todoDate: String? = null,
        startAt: Date? = null,
        endAt: Date? = null,
        details: String? = null,
        allDay: Boolean? = null,
        subAssignmentTag: String? = null
    ): Plannable {
        return Plannable(
            id = id,
            title = title,
            courseId = courseId,
            groupId = groupId,
            userId = userId,
            pointsPossible = pointsPossible,
            dueAt = dueAt,
            assignmentId = assignmentId,
            todoDate = todoDate,
            startAt = startAt,
            endAt = endAt,
            details = details,
            allDay = allDay,
            subAssignmentTag = subAssignmentTag
        )
    }

    private fun createPlannerItem(
        plannableId: Long = 1L,
        plannableType: PlannableType = PlannableType.ASSIGNMENT,
        plannable: Plannable = createPlannable(id = plannableId),
        courseId: Long? = null,
        groupId: Long? = null,
        userId: Long? = null,
        contextName: String? = null,
        plannableItemDetails: PlannerItemDetails? = null,
        submissionState: com.instructure.canvasapi2.models.SubmissionState? = null,
        plannerOverride: com.instructure.canvasapi2.models.PlannerOverride? = null,
        htmlUrl: String? = null
    ): PlannerItem {
        return PlannerItem(
            courseId = courseId,
            groupId = groupId,
            userId = userId,
            contextType = if (courseId != null) "Course" else null,
            contextName = contextName,
            plannableType = plannableType,
            plannable = plannable,
            plannableDate = Date(),
            htmlUrl = htmlUrl,
            submissionState = submissionState,
            newActivity = null,
            plannerOverride = plannerOverride,
            plannableItemDetails = plannableItemDetails
        )
    }
}
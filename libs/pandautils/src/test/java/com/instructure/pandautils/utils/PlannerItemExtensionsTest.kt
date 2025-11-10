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

    @Before
    fun setup() {
        mockkObject(ApiPrefs)
        every { ApiPrefs.fullDomain } returns "https://test.instructure.com"

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
    fun `getDateTextForPlannerItem returns due date text for ASSIGNMENT with dueAt`() {
        val plannable = createPlannable(dueAt = TEST_DATE)
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            plannable = plannable
        )

        every { DateHelper.getFormattedTime(context, TEST_DATE) } returns "2:30 PM"
        every { context.getString(R.string.widgetDueDate, "2:30 PM") } returns "Due: 2:30 PM"

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertEquals("Due: 2:30 PM", result)
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

    // Helper functions to create test objects with default values
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
        userId: Long? = null,
        contextName: String? = null,
        plannableItemDetails: PlannerItemDetails? = null
    ): PlannerItem {
        return PlannerItem(
            courseId = courseId,
            groupId = null,
            userId = userId,
            contextType = if (courseId != null) "Course" else null,
            contextName = contextName,
            plannableType = plannableType,
            plannable = plannable,
            plannableDate = Date(),
            htmlUrl = null,
            submissionState = null,
            newActivity = null,
            plannerOverride = null,
            plannableItemDetails = plannableItemDetails
        )
    }
}
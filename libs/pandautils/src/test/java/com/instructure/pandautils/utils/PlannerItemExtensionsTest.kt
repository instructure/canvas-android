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
package com.instructure.pandautils.utils

import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class PlannerItemExtensionsTest {

    private lateinit var apiPrefs: ApiPrefs

    @Before
    fun setup() {
        apiPrefs = mockk(relaxed = true)
        every { apiPrefs.fullDomain } returns "https://example.instructure.com"
    }

    private fun createPlannerItem(
        courseId: Long? = null,
        groupId: Long? = null,
        userId: Long? = null,
        plannableType: PlannableType,
        plannableId: Long,
        htmlUrl: String? = null
    ): PlannerItem {
        val plannable = Plannable(
            id = plannableId,
            title = "Test Item",
            courseId = courseId,
            groupId = groupId,
            userId = userId,
            pointsPossible = null,
            dueAt = null,
            assignmentId = null,
            todoDate = null,
            startAt = null,
            endAt = null,
            details = null,
            allDay = null
        )

        return PlannerItem(
            courseId = courseId,
            groupId = groupId,
            userId = userId,
            contextType = null,
            contextName = null,
            plannableType = plannableType,
            plannable = plannable,
            plannableDate = Date(),
            htmlUrl = htmlUrl,
            submissionState = null,
            newActivity = null
        )
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
    fun `getDateTextForPlannerItem returns formatted time for ASSIGNMENT with dueAt`() {
        val plannable = createPlannable(dueAt = TEST_DATE)
        val plannerItem = createPlannerItem(
            courseId = 123,
            plannableType = PlannableType.ASSIGNMENT,
            plannableId = 456,
            htmlUrl = null
        )

        every { DateHelper.getFormattedTime(context, TEST_DATE) } returns "2:30 PM"

        val result = plannerItem.getDateTextForPlannerItem(context)

        assertEquals("2:30 PM", result)
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
        userId: Long? = null,
        contextName: String? = null,
        plannableItemDetails: PlannerItemDetails? = null,
        submissionState: com.instructure.canvasapi2.models.SubmissionState? = null,
        plannerOverride: com.instructure.canvasapi2.models.PlannerOverride? = null
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
            submissionState = submissionState,
            newActivity = null,
            plannerOverride = plannerOverride,
            plannableItemDetails = plannableItemDetails
        )
    }
}
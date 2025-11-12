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

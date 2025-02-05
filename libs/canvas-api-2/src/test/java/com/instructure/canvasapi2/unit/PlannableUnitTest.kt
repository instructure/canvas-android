/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.util.Date

class PlannableUnitTest {
    @Test
    fun `Test Plannable to PlannableItem conversation with a course item`() {
        val plannable = Plannable(
            id = 1,
            title = "Plannable 1",
            courseId = 1,
            groupId = null,
            userId = null,
            pointsPossible = null,
            dueAt = Date(),
            assignmentId = null,
            todoDate = Date().toApiString(),
            startAt = Date(),
            endAt = Date(),
            details = "Details",
            allDay = false,
        )

        assertEquals(CanvasContext.Type.COURSE, plannable.contextType)

        val plannableItem = plannable.toPlannableItem()
        assertEquals(plannable.courseId, plannableItem.courseId)
        assertEquals(plannable.groupId, plannableItem.groupId)
        assertEquals(plannable.userId, plannableItem.userId)
        assertEquals(plannable.contextType.apiString, plannableItem.contextType)
        assertEquals(null, plannableItem.contextName)
        assertEquals(plannable, plannableItem.plannable)
        assertEquals(plannable.todoDate.toDate(), plannableItem.plannableDate)
        assertEquals(null, plannableItem.htmlUrl)
        assertEquals(null, plannableItem.submissionState)
        assertEquals(false, plannableItem.newActivity)
        assertEquals(null, plannableItem.plannerOverride)
    }

    @Test
    fun `Test Plannable to PlannableItem conversation with a group item`() {
        val plannable = Plannable(
            id = 1,
            title = "Plannable 1",
            courseId = null,
            groupId = 1,
            userId = null,
            pointsPossible = null,
            dueAt = Date(),
            assignmentId = null,
            todoDate = Date().toApiString(),
            startAt = Date(),
            endAt = Date(),
            details = "Details",
            allDay = false,
        )

        assertEquals(CanvasContext.Type.GROUP, plannable.contextType)

        val plannableItem = plannable.toPlannableItem()
        assertEquals(plannable.courseId, plannableItem.courseId)
        assertEquals(plannable.groupId, plannableItem.groupId)
        assertEquals(plannable.userId, plannableItem.userId)
        assertEquals(plannable.contextType.apiString, plannableItem.contextType)
        assertEquals(null, plannableItem.contextName)
        assertEquals(plannable, plannableItem.plannable)
        assertEquals(plannable.todoDate.toDate(), plannableItem.plannableDate)
        assertEquals(null, plannableItem.htmlUrl)
        assertEquals(null, plannableItem.submissionState)
        assertEquals(false, plannableItem.newActivity)
        assertEquals(null, plannableItem.plannerOverride)
    }

    @Test
    fun `Test Plannable to PlannableItem conversation with a user item`() {
        val plannable = Plannable(
            id = 1,
            title = "Plannable 1",
            courseId = null,
            groupId = null,
            userId = 1,
            pointsPossible = null,
            dueAt = Date(),
            assignmentId = null,
            todoDate = Date().toApiString(),
            startAt = Date(),
            endAt = Date(),
            details = "Details",
            allDay = false,
        )

        assertEquals(CanvasContext.Type.USER, plannable.contextType)

        val plannableItem = plannable.toPlannableItem()
        assertEquals(plannable.courseId, plannableItem.courseId)
        assertEquals(plannable.groupId, plannableItem.groupId)
        assertEquals(plannable.userId, plannableItem.userId)
        assertEquals(plannable.contextType.apiString, plannableItem.contextType)
        assertEquals(null, plannableItem.contextName)
        assertEquals(plannable, plannableItem.plannable)
        assertEquals(plannable.todoDate.toDate(), plannableItem.plannableDate)
        assertEquals(null, plannableItem.htmlUrl)
        assertEquals(null, plannableItem.submissionState)
        assertEquals(false, plannableItem.newActivity)
        assertEquals(null, plannableItem.plannerOverride)
    }
}
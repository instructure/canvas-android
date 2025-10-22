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

package com.instructure.student.test.adapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.student.adapter.TodoListRecyclerAdapter
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TodoListRecyclerAdapterTest {

    private lateinit var adapter: TodoListRecyclerAdapter

    class TestTodoListRecyclerAdapter(context: Context) : TodoListRecyclerAdapter(context)

    @Before
    fun setup() {
        adapter = TestTodoListRecyclerAdapter(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `isComplete returns true for submitted assignment`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            submissionState = SubmissionState(submitted = true)
        )

        assertTrue(adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for unsubmitted assignment`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.ASSIGNMENT,
            submissionState = SubmissionState(submitted = false)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns true for submitted discussion topic`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.DISCUSSION_TOPIC,
            submissionState = SubmissionState(submitted = true)
        )

        assertTrue(adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for unsubmitted discussion topic`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.DISCUSSION_TOPIC,
            submissionState = SubmissionState(submitted = false)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns true for submitted sub assignment`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.SUB_ASSIGNMENT,
            submissionState = SubmissionState(submitted = true)
        )

        assertTrue(adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for unsubmitted sub assignment`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.SUB_ASSIGNMENT,
            submissionState = SubmissionState(submitted = false)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns true for submitted quiz`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.QUIZ,
            submissionState = SubmissionState(submitted = true)
        )

        assertTrue(adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for unsubmitted quiz`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.QUIZ,
            submissionState = SubmissionState(submitted = false)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for quiz with null submission state`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.QUIZ,
            submissionState = null
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for calendar event`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.CALENDAR_EVENT,
            submissionState = SubmissionState(submitted = true)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for planner note`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.PLANNER_NOTE,
            submissionState = SubmissionState(submitted = true)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for wiki page`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.WIKI_PAGE,
            submissionState = SubmissionState(submitted = true)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    @Test
    fun `isComplete returns false for todo`() {
        val plannerItem = createPlannerItem(
            plannableType = PlannableType.TODO,
            submissionState = SubmissionState(submitted = true)
        )

        assertEquals(false, adapter.isComplete(plannerItem))
    }

    private fun createPlannerItem(
        plannableType: PlannableType,
        submissionState: SubmissionState? = null
    ): PlannerItem {
        val plannable = Plannable(
            id = 1L,
            title = "Test Item",
            courseId = 1L,
            groupId = null,
            userId = null,
            pointsPossible = null,
            dueAt = null,
            assignmentId = 1L,
            todoDate = null,
            startAt = null,
            endAt = null,
            details = null,
            allDay = null
        )

        return PlannerItem(
            courseId = 1L,
            groupId = null,
            userId = null,
            contextType = "Course",
            contextName = "Test Course",
            plannableType = plannableType,
            plannable = plannable,
            plannableDate = Date(),
            htmlUrl = "/courses/1",
            submissionState = submissionState,
            newActivity = false,
            plannerOverride = null,
            plannableItemDetails = null,
            isChecked = false
        )
    }
}

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
package com.instructure.pandautils.compose.composables.todo

import android.content.Context
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.ApiPrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class ToDoStateMapperTest {

    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var mapper: ToDoStateMapper

    @Before
    fun setUp() {
        every { apiPrefs.user } returns mockk(relaxed = true)
        every { apiPrefs.fullDomain } returns "test.instructure.com"
        mapper = ToDoStateMapper(context, apiPrefs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `mapToUiState maps ASSIGNMENT type correctly`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Assignment",
            plannableType = PlannableType.ASSIGNMENT
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals(ToDoItemType.ASSIGNMENT, result.itemType)
    }

    @Test
    fun `mapToUiState maps QUIZ type correctly`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Quiz",
            plannableType = PlannableType.QUIZ
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals(ToDoItemType.QUIZ, result.itemType)
    }

    @Test
    fun `mapToUiState maps DISCUSSION_TOPIC type correctly`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Discussion",
            plannableType = PlannableType.DISCUSSION_TOPIC
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals(ToDoItemType.DISCUSSION, result.itemType)
    }

    @Test
    fun `mapToUiState maps CALENDAR_EVENT type correctly`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Calendar Event",
            plannableType = PlannableType.CALENDAR_EVENT
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals(ToDoItemType.CALENDAR_EVENT, result.itemType)
    }

    @Test
    fun `mapToUiState maps PLANNER_NOTE type correctly`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Planner Note",
            plannableType = PlannableType.PLANNER_NOTE
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals(ToDoItemType.PLANNER_NOTE, result.itemType)
    }

    @Test
    fun `mapToUiState maps SUB_ASSIGNMENT type correctly`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Sub Assignment",
            plannableType = PlannableType.SUB_ASSIGNMENT
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals(ToDoItemType.SUB_ASSIGNMENT, result.itemType)
    }

    @Test
    fun `mapToUiState maps unknown type to CALENDAR_EVENT`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Unknown",
            plannableType = PlannableType.ANNOUNCEMENT
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals(ToDoItemType.CALENDAR_EVENT, result.itemType)
    }

    @Test
    fun `mapToUiState marks Account-level calendar events as not clickable`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Account Event",
            plannableType = PlannableType.CALENDAR_EVENT
        ).copy(contextType = "Account")

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertFalse(result.isClickable)
        assertEquals(ToDoItemType.CALENDAR_EVENT, result.itemType)
    }

    @Test
    fun `mapToUiState marks Course-level calendar events as clickable`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Course Event",
            courseId = 1L,
            plannableType = PlannableType.CALENDAR_EVENT
        ).copy(contextType = "Course")

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertTrue(result.isClickable)
        assertEquals(ToDoItemType.CALENDAR_EVENT, result.itemType)
    }

    @Test
    fun `mapToUiState marks User-level calendar events as clickable`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "User Event",
            plannableType = PlannableType.CALENDAR_EVENT
        ).copy(contextType = "User")

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertTrue(result.isClickable)
        assertEquals(ToDoItemType.CALENDAR_EVENT, result.itemType)
    }

    @Test
    fun `mapToUiState marks Account-level calendar events as not clickable (case insensitive)`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Account Event",
            plannableType = PlannableType.CALENDAR_EVENT
        ).copy(contextType = "account")

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertFalse(result.isClickable)
    }

    @Test
    fun `mapToUiState marks non-calendar events as clickable regardless of context`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Assignment",
            plannableType = PlannableType.ASSIGNMENT
        ).copy(contextType = "Account")

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertTrue(result.isClickable)
        assertEquals(ToDoItemType.ASSIGNMENT, result.itemType)
    }

    @Test
    fun `mapToUiState sets isChecked true when plannerOverride is marked complete`() {
        val plannerOverride = PlannerOverride(
            id = 100L,
            plannableId = 1L,
            plannableType = PlannableType.ASSIGNMENT,
            markedComplete = true
        )
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Assignment",
            plannableType = PlannableType.ASSIGNMENT
        ).copy(plannerOverride = plannerOverride)

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertTrue(result.isChecked)
    }

    @Test
    fun `mapToUiState sets isChecked true when assignment is submitted`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Submitted Assignment",
            plannableType = PlannableType.ASSIGNMENT,
            submitted = true
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertTrue(result.isChecked)
    }

    @Test
    fun `mapToUiState sets isChecked false when not submitted and no override`() {
        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Unsubmitted Assignment",
            plannableType = PlannableType.ASSIGNMENT,
            submitted = false
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertFalse(result.isChecked)
    }

    @Test
    fun `mapToUiState populates all required fields`() {
        val date = Date(1704067200000L)
        val plannerItem = createPlannerItem(
            id = 42L,
            title = "Test Assignment",
            plannableType = PlannableType.ASSIGNMENT,
            plannableDate = date
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )

        assertEquals("42", result.id)
        assertEquals("Test Assignment", result.title)
        assertEquals(date, result.date)
        assertEquals(ToDoItemType.ASSIGNMENT, result.itemType)
    }

    @Test
    fun `mapToUiState passes callbacks correctly`() {
        var swipeCallbackInvoked = false
        var checkboxCallbackInvoked = false
        var checkboxValue = false

        val plannerItem = createPlannerItem(
            id = 1L,
            title = "Assignment",
            plannableType = PlannableType.ASSIGNMENT
        )

        val result = mapper.mapToUiState(
            plannerItem = plannerItem,
            courseMap = emptyMap(),
            onSwipeToDone = { swipeCallbackInvoked = true },
            onCheckboxToggle = { value ->
                checkboxCallbackInvoked = true
                checkboxValue = value
            }
        )

        result.onSwipeToDone()
        assertTrue(swipeCallbackInvoked)

        result.onCheckboxToggle(true)
        assertTrue(checkboxCallbackInvoked)
        assertTrue(checkboxValue)
    }

    // Helper function
    private fun createPlannerItem(
        id: Long,
        title: String,
        courseId: Long? = null,
        plannableType: PlannableType = PlannableType.ASSIGNMENT,
        plannableDate: Date = Date(),
        submitted: Boolean = false
    ): PlannerItem {
        return PlannerItem(
            courseId = courseId,
            groupId = null,
            userId = null,
            contextType = if (courseId != null) "Course" else null,
            contextName = null,
            plannableType = plannableType,
            plannable = Plannable(
                id = id,
                title = title,
                courseId = courseId,
                groupId = null,
                userId = null,
                pointsPossible = null,
                dueAt = plannableDate,
                assignmentId = null,
                todoDate = null,
                startAt = null,
                endAt = null,
                details = null,
                allDay = null,
                subAssignmentTag = null
            ),
            plannableDate = plannableDate,
            htmlUrl = null,
            submissionState = if (submitted) SubmissionState(submitted = true) else null,
            newActivity = null,
            plannerOverride = null,
            plannableItemDetails = null
        )
    }
}
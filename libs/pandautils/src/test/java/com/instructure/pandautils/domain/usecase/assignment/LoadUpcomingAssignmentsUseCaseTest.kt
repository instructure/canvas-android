/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.assignment

import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.planner.PlannerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class LoadUpcomingAssignmentsUseCaseTest {

    private val repository: PlannerRepository = mockk()
    private lateinit var useCase: LoadUpcomingAssignmentsUseCase

    @Before
    fun setUp() {
        useCase = LoadUpcomingAssignmentsUseCase(repository)
    }

    private fun createPlannerItem(
        id: Long,
        type: PlannableType,
        title: String = "Item $id"
    ): PlannerItem {
        return PlannerItem(
            courseId = 1L,
            groupId = null,
            userId = null,
            contextType = "Course",
            contextName = "Test Course",
            plannableType = type,
            plannable = Plannable(
                id = id,
                title = title,
                courseId = 1L,
                groupId = null,
                userId = null,
                pointsPossible = 100.0,
                dueAt = null,
                assignmentId = id,
                todoDate = null,
                startAt = null,
                endAt = null,
                details = null,
                allDay = null
            ),
            plannableDate = Date(),
            htmlUrl = "https://example.com/item/$id",
            submissionState = null,
            newActivity = false,
            plannerOverride = null
        )
    }

    @Test
    fun `execute returns only assignment items`() = runTest {
        val assignment1 = createPlannerItem(1, PlannableType.ASSIGNMENT)
        val assignment2 = createPlannerItem(2, PlannableType.ASSIGNMENT)
        val quiz = createPlannerItem(3, PlannableType.QUIZ)
        val discussion = createPlannerItem(4, PlannableType.DISCUSSION_TOPIC)
        val announcement = createPlannerItem(5, PlannableType.ANNOUNCEMENT)

        val allItems = listOf(assignment1, quiz, assignment2, discussion, announcement)

        coEvery {
            repository.getPlannerItems("2025-01-01", "2025-01-07", emptyList(), false)
        } returns DataResult.Success(allItems)

        val result = useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07"
            )
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.plannableType == PlannableType.ASSIGNMENT })
        assertEquals(1L, result[0].plannable.id)
        assertEquals(2L, result[1].plannable.id)
    }

    @Test
    fun `execute returns sub-assignments along with assignments`() = runTest {
        val assignment = createPlannerItem(1, PlannableType.ASSIGNMENT)
        val subAssignment = createPlannerItem(2, PlannableType.SUB_ASSIGNMENT)
        val quiz = createPlannerItem(3, PlannableType.QUIZ)

        val allItems = listOf(assignment, subAssignment, quiz)

        coEvery {
            repository.getPlannerItems("2025-01-01", "2025-01-07", emptyList(), false)
        } returns DataResult.Success(allItems)

        val result = useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07"
            )
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.plannableType == PlannableType.ASSIGNMENT })
        assertTrue(result.any { it.plannableType == PlannableType.SUB_ASSIGNMENT })
    }

    @Test
    fun `execute returns empty list when no assignments found`() = runTest {
        val quiz = createPlannerItem(1, PlannableType.QUIZ)
        val discussion = createPlannerItem(2, PlannableType.DISCUSSION_TOPIC)

        val allItems = listOf(quiz, discussion)

        coEvery {
            repository.getPlannerItems("2025-01-01", "2025-01-07", emptyList(), false)
        } returns DataResult.Success(allItems)

        val result = useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07"
            )
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute returns empty list when repository returns empty`() = runTest {
        coEvery {
            repository.getPlannerItems("2025-01-01", "2025-01-07", emptyList(), false)
        } returns DataResult.Success(emptyList())

        val result = useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07"
            )
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute passes context codes to repository`() = runTest {
        val contextCodes = listOf("course_1", "course_2")

        coEvery {
            repository.getPlannerItems("2025-01-01", "2025-01-07", contextCodes, false)
        } returns DataResult.Success(emptyList())

        useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07",
                contextCodes = contextCodes
            )
        )

        coVerify {
            repository.getPlannerItems("2025-01-01", "2025-01-07", contextCodes, false)
        }
    }

    @Test
    fun `execute passes forceRefresh to repository`() = runTest {
        coEvery {
            repository.getPlannerItems("2025-01-01", "2025-01-07", emptyList(), true)
        } returns DataResult.Success(emptyList())

        useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07",
                forceRefresh = true
            )
        )

        coVerify {
            repository.getPlannerItems("2025-01-01", "2025-01-07", emptyList(), true)
        }
    }

    @Test(expected = Exception::class)
    fun `execute throws when repository fails`() = runTest {
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Fail()

        useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07"
            )
        )
    }

    @Test
    fun `execute filters out all non-assignment types`() = runTest {
        val assignment = createPlannerItem(1, PlannableType.ASSIGNMENT)
        val quiz = createPlannerItem(2, PlannableType.QUIZ)
        val discussion = createPlannerItem(3, PlannableType.DISCUSSION_TOPIC)
        val announcement = createPlannerItem(4, PlannableType.ANNOUNCEMENT)
        val calendarEvent = createPlannerItem(5, PlannableType.CALENDAR_EVENT)
        val todo = createPlannerItem(6, PlannableType.PLANNER_NOTE)
        val wiki = createPlannerItem(7, PlannableType.WIKI_PAGE)
        val subAssignment = createPlannerItem(8, PlannableType.SUB_ASSIGNMENT)

        val allItems = listOf(assignment, quiz, discussion, announcement, calendarEvent, todo, wiki, subAssignment)

        coEvery {
            repository.getPlannerItems("2025-01-01", "2025-01-07", emptyList(), false)
        } returns DataResult.Success(allItems)

        val result = useCase(
            LoadUpcomingAssignmentsParams(
                startDate = "2025-01-01",
                endDate = "2025-01-07"
            )
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.plannableType == PlannableType.ASSIGNMENT })
        assertTrue(result.any { it.plannableType == PlannableType.SUB_ASSIGNMENT })
    }
}
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
package com.instructure.pandautils.domain.usecase.planner

import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.planner.PlannerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class LoadPlannerItemsUseCaseTest {

    private val repository: PlannerRepository = mockk()
    private lateinit var useCase: LoadPlannerItemsUseCase

    @Before
    fun setup() {
        useCase = LoadPlannerItemsUseCase(repository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `invoke loads planner items and filters announcements`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, type = PlannableType.ASSIGNMENT),
            createPlannerItem(id = 2L, type = PlannableType.ANNOUNCEMENT),
            createPlannerItem(id = 3L, type = PlannableType.QUIZ)
        )
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(plannerItems)

        val result = useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                forceNetwork = false
            )
        )

        assertEquals(2, result.size)
        assertEquals(1L, result[0].plannable.id)
        assertEquals(3L, result[1].plannable.id)
    }

    @Test
    fun `invoke loads planner items and filters assessment requests`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, type = PlannableType.ASSIGNMENT),
            createPlannerItem(id = 2L, type = PlannableType.ASSESSMENT_REQUEST),
            createPlannerItem(id = 3L, type = PlannableType.DISCUSSION_TOPIC)
        )
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(plannerItems)

        val result = useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                forceNetwork = false
            )
        )

        assertEquals(2, result.size)
        assertEquals(1L, result[0].plannable.id)
        assertEquals(3L, result[1].plannable.id)
    }

    @Test
    fun `invoke filters both announcements and assessment requests`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, type = PlannableType.ASSIGNMENT),
            createPlannerItem(id = 2L, type = PlannableType.ANNOUNCEMENT),
            createPlannerItem(id = 3L, type = PlannableType.QUIZ),
            createPlannerItem(id = 4L, type = PlannableType.ASSESSMENT_REQUEST),
            createPlannerItem(id = 5L, type = PlannableType.CALENDAR_EVENT)
        )
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(plannerItems)

        val result = useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                forceNetwork = false
            )
        )

        assertEquals(3, result.size)
        assertEquals(1L, result[0].plannable.id)
        assertEquals(3L, result[1].plannable.id)
        assertEquals(5L, result[2].plannable.id)
    }

    @Test
    fun `invoke returns empty list when all items are filtered`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, type = PlannableType.ANNOUNCEMENT),
            createPlannerItem(id = 2L, type = PlannableType.ASSESSMENT_REQUEST)
        )
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(plannerItems)

        val result = useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                forceNetwork = false
            )
        )

        assertEquals(0, result.size)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        val result = useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                forceNetwork = false
            )
        )

        assertEquals(0, result.size)
    }

    @Test
    fun `invoke passes correct parameters to repository`() = runTest {
        val startDate = "2025-02-01"
        val endDate = "2025-02-28"
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = startDate,
                endDate = endDate,
                forceNetwork = false
            )
        )

        coVerify {
            repository.getPlannerItems(
                startDate = startDate,
                endDate = endDate,
                contextCodes = emptyList(),
                forceRefresh = false
            )
        }
    }

    @Test
    fun `invoke uses forceNetwork parameter`() = runTest {
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                forceNetwork = true
            )
        )

        coVerify {
            repository.getPlannerItems(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                contextCodes = emptyList(),
                forceRefresh = true
            )
        }
    }

    @Test
    fun `invoke includes all valid planner item types`() = runTest {
        val plannerItems = listOf(
            createPlannerItem(id = 1L, type = PlannableType.ASSIGNMENT),
            createPlannerItem(id = 2L, type = PlannableType.QUIZ),
            createPlannerItem(id = 3L, type = PlannableType.DISCUSSION_TOPIC),
            createPlannerItem(id = 4L, type = PlannableType.CALENDAR_EVENT),
            createPlannerItem(id = 5L, type = PlannableType.PLANNER_NOTE),
            createPlannerItem(id = 6L, type = PlannableType.SUB_ASSIGNMENT)
        )
        coEvery {
            repository.getPlannerItems(any(), any(), any(), any())
        } returns DataResult.Success(plannerItems)

        val result = useCase(
            LoadPlannerItemsUseCase.Params(
                startDate = "2025-01-01",
                endDate = "2025-01-31",
                forceNetwork = false
            )
        )

        assertEquals(6, result.size)
        assertEquals(PlannableType.ASSIGNMENT, result[0].plannableType)
        assertEquals(PlannableType.QUIZ, result[1].plannableType)
        assertEquals(PlannableType.DISCUSSION_TOPIC, result[2].plannableType)
        assertEquals(PlannableType.CALENDAR_EVENT, result[3].plannableType)
        assertEquals(PlannableType.PLANNER_NOTE, result[4].plannableType)
        assertEquals(PlannableType.SUB_ASSIGNMENT, result[5].plannableType)
    }

    private fun createPlannerItem(
        id: Long,
        type: PlannableType,
        title: String = "Test Item"
    ): PlannerItem {
        return PlannerItem(
            courseId = null,
            groupId = null,
            userId = null,
            contextType = null,
            contextName = null,
            plannableType = type,
            plannable = Plannable(
                id = id,
                title = title,
                courseId = null,
                groupId = null,
                userId = null,
                pointsPossible = null,
                dueAt = Date(),
                assignmentId = null,
                todoDate = null,
                startAt = null,
                endAt = null,
                details = null,
                allDay = null,
                subAssignmentTag = null
            ),
            plannableDate = Date(),
            htmlUrl = null,
            submissionState = null,
            newActivity = null,
            plannerOverride = null,
            plannableItemDetails = null
        )
    }
}
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

import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class LoadPlannerItemsUseCaseTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk()
    private lateinit var useCase: LoadPlannerItemsUseCase

    @Before
    fun setup() {
        useCase = LoadPlannerItemsUseCase(plannerApi)
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
        val dataResult = DataResult.Success(plannerItems, LinkHeaders())
        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns dataResult

        val result = useCase("2025-01-01", "2025-01-31", false)

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
        val dataResult = DataResult.Success(plannerItems, LinkHeaders())
        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns dataResult

        val result = useCase("2025-01-01", "2025-01-31", false)

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
        val dataResult = DataResult.Success(plannerItems, LinkHeaders())
        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns dataResult

        val result = useCase("2025-01-01", "2025-01-31", false)

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
        val dataResult = DataResult.Success(plannerItems, LinkHeaders())
        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns dataResult

        val result = useCase("2025-01-01", "2025-01-31", false)

        assertEquals(0, result.size)
    }

    @Test
    fun `invoke returns empty list when API returns empty`() = runTest {
        val dataResult = DataResult.Success(emptyList<PlannerItem>(), LinkHeaders())
        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns dataResult

        val result = useCase("2025-01-01", "2025-01-31", false)

        assertEquals(0, result.size)
    }

    @Test
    fun `invoke passes correct parameters to API`() = runTest {
        val startDate = "2025-02-01"
        val endDate = "2025-02-28"
        val dataResult = DataResult.Success(emptyList<PlannerItem>(), LinkHeaders())
        val restParamsSlot = slot<RestParams>()

        coEvery {
            plannerApi.getPlannerItems(
                startDate,
                endDate,
                emptyList(),
                null,
                capture(restParamsSlot)
            )
        } returns dataResult

        useCase(startDate, endDate, false)

        coVerify {
            plannerApi.getPlannerItems(
                startDate,
                endDate,
                emptyList(),
                null,
                any()
            )
        }
        assertEquals(false, restParamsSlot.captured.isForceReadFromNetwork)
        assertEquals(true, restParamsSlot.captured.usePerPageQueryParam)
    }

    @Test
    fun `invoke uses forceNetwork parameter`() = runTest {
        val dataResult = DataResult.Success(emptyList<PlannerItem>(), LinkHeaders())
        val restParamsSlot = slot<RestParams>()

        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), capture(restParamsSlot))
        } returns dataResult

        useCase("2025-01-01", "2025-01-31", true)

        assertEquals(true, restParamsSlot.captured.isForceReadFromNetwork)
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
        val dataResult = DataResult.Success(plannerItems, LinkHeaders())
        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns dataResult

        val result = useCase("2025-01-01", "2025-01-31", false)

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
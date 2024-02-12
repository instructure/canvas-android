/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.calendar

import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class CalendarRepositoryTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)

    private val calendarRepository: CalendarRepository = CalendarRepository(plannerApi)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when request fails`() = runTest {
        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Fail()

        calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)
    }

    @Test
    fun `Return results from the api and filter announcments on successful request`() = runTest {
        val filteredItem = createPlannerItem(1, 3, PlannableType.ANNOUNCEMENT)
        val plannerItems = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT),
            createPlannerItem(1, 2, PlannableType.QUIZ),
            filteredItem,
            createPlannerItem(1, 4, PlannableType.DISCUSSION_TOPIC),
            createPlannerItem(2, 5, PlannableType.PLANNER_NOTE),
            createPlannerItem(2, 6, PlannableType.CALENDAR_EVENT)
        )

        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Success(plannerItems)

        val result = calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)

        assertEquals(plannerItems.minus(filteredItem), result)
    }

    @Test
    fun `Return depaginated result when has next page`() = runTest {
        val plannerItems1 = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT),
            createPlannerItem(1, 2, PlannableType.QUIZ),
        )

        val plannerItems2 = listOf(
            createPlannerItem(2, 5, PlannableType.PLANNER_NOTE),
            createPlannerItem(2, 6, PlannableType.CALENDAR_EVENT)
        )

        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Success(
            plannerItems1,
            linkHeaders = LinkHeaders(nextUrl = "next")
        )
        coEvery { plannerApi.nextPagePlannerItems(eq("next"), any()) } returns DataResult.Success(plannerItems2)

        val result = calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)

        assertEquals(plannerItems1.plus(plannerItems2), result)
    }

    private fun createPlannerItem(
        courseId: Long,
        plannableId: Long,
        plannableType: PlannableType
    ): PlannerItem {
        val plannable = Plannable(
            id = plannableId,
            title = "Plannable $plannableId",
            courseId,
            null,
            null,
            null,
            null,
            plannableId,
            null,
            null,
            null,
            null
        )
        return PlannerItem(
            courseId,
            null,
            null,
            null,
            null,
            plannableType,
            plannable,
            Date(),
            null,
            SubmissionState(submitted = false),
            plannerOverride = PlannerOverride(plannableType = plannableType, plannableId = plannableId),
            newActivity = false
        )
    }
}
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

import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.todolist.ToDoListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdatePlannerOverrideUseCaseTest {

    private val repository: ToDoListRepository = mockk()
    private lateinit var useCase: UpdatePlannerOverrideUseCase

    @Before
    fun setup() {
        useCase = UpdatePlannerOverrideUseCase(repository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute updates planner override to marked as complete`() = runTest {
        val plannerOverrideId = 123L
        val plannerOverride = PlannerOverride(
            id = plannerOverrideId,
            plannableId = 456L,
            plannableType = PlannableType.ASSIGNMENT,
            markedComplete = true
        )
        coEvery {
            repository.updatePlannerOverride(plannerOverrideId, true)
        } returns DataResult.Success(plannerOverride)

        val params = UpdatePlannerOverrideUseCase.Params(
            plannerOverrideId = plannerOverrideId,
            markedComplete = true
        )
        val result = useCase(params)

        assertEquals(plannerOverrideId, result.id)
        assertTrue(result.markedComplete)
        coVerify { repository.updatePlannerOverride(plannerOverrideId, true) }
    }

    @Test
    fun `execute updates planner override to marked as incomplete`() = runTest {
        val plannerOverrideId = 789L
        val plannerOverride = PlannerOverride(
            id = plannerOverrideId,
            plannableId = 101L,
            plannableType = PlannableType.QUIZ,
            markedComplete = false
        )
        coEvery {
            repository.updatePlannerOverride(plannerOverrideId, false)
        } returns DataResult.Success(plannerOverride)

        val params = UpdatePlannerOverrideUseCase.Params(
            plannerOverrideId = plannerOverrideId,
            markedComplete = false
        )
        val result = useCase(params)

        assertEquals(plannerOverrideId, result.id)
        assertFalse(result.markedComplete)
        coVerify { repository.updatePlannerOverride(plannerOverrideId, false) }
    }

    @Test
    fun `execute updates planner override with different override IDs`() = runTest {
        val plannerOverrideId1 = 111L
        val plannerOverrideId2 = 222L

        val plannerOverride1 = PlannerOverride(
            id = plannerOverrideId1,
            plannableId = 333L,
            plannableType = PlannableType.DISCUSSION_TOPIC,
            markedComplete = true
        )
        val plannerOverride2 = PlannerOverride(
            id = plannerOverrideId2,
            plannableId = 444L,
            plannableType = PlannableType.CALENDAR_EVENT,
            markedComplete = false
        )

        coEvery {
            repository.updatePlannerOverride(plannerOverrideId1, true)
        } returns DataResult.Success(plannerOverride1)
        coEvery {
            repository.updatePlannerOverride(plannerOverrideId2, false)
        } returns DataResult.Success(plannerOverride2)

        val params1 = UpdatePlannerOverrideUseCase.Params(plannerOverrideId1, true)
        val result1 = useCase(params1)
        assertEquals(plannerOverrideId1, result1.id)
        assertTrue(result1.markedComplete)

        val params2 = UpdatePlannerOverrideUseCase.Params(plannerOverrideId2, false)
        val result2 = useCase(params2)
        assertEquals(plannerOverrideId2, result2.id)
        assertFalse(result2.markedComplete)

        coVerify { repository.updatePlannerOverride(plannerOverrideId1, true) }
        coVerify { repository.updatePlannerOverride(plannerOverrideId2, false) }
    }

    @Test
    fun `execute returns updated planner override with all properties`() = runTest {
        val plannerOverrideId = 555L
        val plannableId = 666L
        val plannerOverride = PlannerOverride(
            id = plannerOverrideId,
            plannableId = plannableId,
            plannableType = PlannableType.PLANNER_NOTE,
            markedComplete = true
        )
        coEvery {
            repository.updatePlannerOverride(plannerOverrideId, true)
        } returns DataResult.Success(plannerOverride)

        val params = UpdatePlannerOverrideUseCase.Params(
            plannerOverrideId = plannerOverrideId,
            markedComplete = true
        )
        val result = useCase(params)

        assertEquals(plannerOverrideId, result.id)
        assertEquals(plannableId, result.plannableId)
        assertEquals(PlannableType.PLANNER_NOTE, result.plannableType)
        assertTrue(result.markedComplete)
        coVerify { repository.updatePlannerOverride(plannerOverrideId, true) }
    }

    @Test
    fun `execute toggles planner override from complete to incomplete`() = runTest {
        val plannerOverrideId = 777L
        val completedOverride = PlannerOverride(
            id = plannerOverrideId,
            plannableId = 888L,
            plannableType = PlannableType.ASSIGNMENT,
            markedComplete = true
        )
        val incompletedOverride = completedOverride.copy(markedComplete = false)

        coEvery {
            repository.updatePlannerOverride(plannerOverrideId, true)
        } returns DataResult.Success(completedOverride)
        coEvery {
            repository.updatePlannerOverride(plannerOverrideId, false)
        } returns DataResult.Success(incompletedOverride)

        // Mark as complete
        val paramsComplete = UpdatePlannerOverrideUseCase.Params(plannerOverrideId, true)
        val resultComplete = useCase(paramsComplete)
        assertTrue(resultComplete.markedComplete)

        // Mark as incomplete
        val paramsIncomplete = UpdatePlannerOverrideUseCase.Params(plannerOverrideId, false)
        val resultIncomplete = useCase(paramsIncomplete)
        assertFalse(resultIncomplete.markedComplete)

        coVerify { repository.updatePlannerOverride(plannerOverrideId, true) }
        coVerify { repository.updatePlannerOverride(plannerOverrideId, false) }
    }
}
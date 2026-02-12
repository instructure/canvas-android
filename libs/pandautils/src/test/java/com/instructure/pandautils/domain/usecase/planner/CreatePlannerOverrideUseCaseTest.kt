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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreatePlannerOverrideUseCaseTest {

    private val repository: ToDoListRepository = mockk()
    private lateinit var useCase: CreatePlannerOverrideUseCase

    @Before
    fun setup() {
        useCase = CreatePlannerOverrideUseCase(repository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute creates planner override marked as complete`() = runTest {
        val plannableId = 123L
        val plannableType = PlannableType.ASSIGNMENT
        val plannerOverride = PlannerOverride(
            id = 456L,
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )
        coEvery {
            repository.createPlannerOverride(plannableId, plannableType, true)
        } returns DataResult.Success(plannerOverride)

        val params = CreatePlannerOverrideUseCase.Params(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )
        val result = useCase(params)

        assertEquals(456L, result.id)
        assertEquals(plannableId, result.plannableId)
        assertEquals(plannableType, result.plannableType)
        assertTrue(result.markedComplete)
        coVerify { repository.createPlannerOverride(plannableId, plannableType, true) }
    }

    @Test
    fun `execute creates planner override marked as incomplete`() = runTest {
        val plannableId = 789L
        val plannableType = PlannableType.QUIZ
        val plannerOverride = PlannerOverride(
            id = 101L,
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = false
        )
        coEvery {
            repository.createPlannerOverride(plannableId, plannableType, false)
        } returns DataResult.Success(plannerOverride)

        val params = CreatePlannerOverrideUseCase.Params(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = false
        )
        val result = useCase(params)

        assertEquals(101L, result.id)
        assertEquals(plannableId, result.plannableId)
        assertEquals(plannableType, result.plannableType)
        assertEquals(false, result.markedComplete)
        coVerify { repository.createPlannerOverride(plannableId, plannableType, false) }
    }

    @Test
    fun `execute creates planner override for DISCUSSION_TOPIC type`() = runTest {
        val plannableId = 222L
        val plannableType = PlannableType.DISCUSSION_TOPIC
        val plannerOverride = PlannerOverride(
            id = 333L,
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )
        coEvery {
            repository.createPlannerOverride(plannableId, plannableType, true)
        } returns DataResult.Success(plannerOverride)

        val params = CreatePlannerOverrideUseCase.Params(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )
        val result = useCase(params)

        assertEquals(333L, result.id)
        assertEquals(plannableType, result.plannableType)
        coVerify { repository.createPlannerOverride(plannableId, plannableType, true) }
    }

    @Test
    fun `execute creates planner override for CALENDAR_EVENT type`() = runTest {
        val plannableId = 444L
        val plannableType = PlannableType.CALENDAR_EVENT
        val plannerOverride = PlannerOverride(
            id = 555L,
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = false
        )
        coEvery {
            repository.createPlannerOverride(plannableId, plannableType, false)
        } returns DataResult.Success(plannerOverride)

        val params = CreatePlannerOverrideUseCase.Params(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = false
        )
        val result = useCase(params)

        assertEquals(555L, result.id)
        assertEquals(plannableType, result.plannableType)
        coVerify { repository.createPlannerOverride(plannableId, plannableType, false) }
    }

    @Test
    fun `execute creates planner override for PLANNER_NOTE type`() = runTest {
        val plannableId = 666L
        val plannableType = PlannableType.PLANNER_NOTE
        val plannerOverride = PlannerOverride(
            id = 777L,
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )
        coEvery {
            repository.createPlannerOverride(plannableId, plannableType, true)
        } returns DataResult.Success(plannerOverride)

        val params = CreatePlannerOverrideUseCase.Params(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )
        val result = useCase(params)

        assertEquals(777L, result.id)
        assertEquals(plannableType, result.plannableType)
        coVerify { repository.createPlannerOverride(plannableId, plannableType, true) }
    }
}
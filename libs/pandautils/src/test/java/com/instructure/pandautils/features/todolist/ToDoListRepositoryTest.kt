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
package com.instructure.pandautils.features.todolist

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class ToDoListRepositoryTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private lateinit var repository: ToDoListRepository

    @Before
    fun setup() {
        repository = ToDoListRepository(plannerApi, courseApi)
    }

    // getPlannerItems tests
    @Test
    fun `getPlannerItems returns success with data`() = runTest {
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"
        val plannerItems = listOf(
            createPlannerItem(id = 1L, title = "Assignment 1"),
            createPlannerItem(id = 2L, title = "Assignment 2")
        )

        coEvery {
            plannerApi.getPlannerItems(
                startDate = startDate,
                endDate = endDate,
                contextCodes = emptyList(),
                restParams = any()
            )
        } returns DataResult.Success(plannerItems)

        val result = repository.getPlannerItems(startDate, endDate, forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertEquals(2, result.dataOrNull?.size)
        assertEquals("Assignment 1", result.dataOrNull?.get(0)?.plannable?.title)
    }

    @Test
    fun `getPlannerItems returns failure when API call fails`() = runTest {
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"

        coEvery {
            plannerApi.getPlannerItems(
                startDate = startDate,
                endDate = endDate,
                contextCodes = emptyList(),
                restParams = any()
            )
        } returns DataResult.Fail()

        val result = repository.getPlannerItems(startDate, endDate, forceRefresh = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getPlannerItems uses correct RestParams when forceRefresh is true`() = runTest {
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"

        coEvery {
            plannerApi.getPlannerItems(
                startDate = any(),
                endDate = any(),
                contextCodes = any(),
                restParams = any()
            )
        } returns DataResult.Success(emptyList())

        repository.getPlannerItems(startDate, endDate, forceRefresh = true)

        coVerify {
            plannerApi.getPlannerItems(
                startDate = startDate,
                endDate = endDate,
                contextCodes = emptyList(),
                restParams = match<RestParams> { it.isForceReadFromNetwork && it.usePerPageQueryParam }
            )
        }
    }

    @Test
    fun `getPlannerItems uses correct RestParams when forceRefresh is false`() = runTest {
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"

        coEvery {
            plannerApi.getPlannerItems(
                startDate = any(),
                endDate = any(),
                contextCodes = any(),
                restParams = any()
            )
        } returns DataResult.Success(emptyList())

        repository.getPlannerItems(startDate, endDate, forceRefresh = false)

        coVerify {
            plannerApi.getPlannerItems(
                startDate = startDate,
                endDate = endDate,
                contextCodes = emptyList(),
                restParams = match<RestParams> { !it.isForceReadFromNetwork && it.usePerPageQueryParam }
            )
        }
    }

    // getCourses tests
    @Test
    fun `getCourses returns success with data`() = runTest {
        val courses = listOf(
            Course(id = 1L, name = "Course 1", courseCode = "CS101"),
            Course(id = 2L, name = "Course 2", courseCode = "MATH201")
        )

        coEvery {
            courseApi.getFirstPageCourses(any())
        } returns DataResult.Success(courses)

        val result = repository.getCourses(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertEquals(2, result.dataOrNull?.size)
        assertEquals("Course 1", result.dataOrNull?.get(0)?.name)
    }

    @Test
    fun `getCourses returns failure when API call fails`() = runTest {
        coEvery {
            courseApi.getFirstPageCourses(any())
        } returns DataResult.Fail()

        val result = repository.getCourses(forceRefresh = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getCourses uses correct RestParams when forceRefresh is true`() = runTest {
        coEvery {
            courseApi.getFirstPageCourses(any())
        } returns DataResult.Success(emptyList())

        repository.getCourses(forceRefresh = true)

        coVerify {
            courseApi.getFirstPageCourses(
                match<RestParams> { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getCourses uses correct RestParams when forceRefresh is false`() = runTest {
        coEvery {
            courseApi.getFirstPageCourses(any())
        } returns DataResult.Success(emptyList())

        repository.getCourses(forceRefresh = false)

        coVerify {
            courseApi.getFirstPageCourses(
                match<RestParams> { !it.isForceReadFromNetwork }
            )
        }
    }

    // updatePlannerOverride tests
    @Test
    fun `updatePlannerOverride returns success with updated override`() = runTest {
        val overrideId = 123L
        val override = PlannerOverride(
            id = overrideId,
            plannableId = 1L,
            plannableType = PlannableType.ASSIGNMENT,
            markedComplete = true
        )

        coEvery {
            plannerApi.updatePlannerOverride(
                plannerOverrideId = overrideId,
                complete = true,
                params = any()
            )
        } returns DataResult.Success(override)

        val result = repository.updatePlannerOverride(overrideId, markedComplete = true)

        assertTrue(result is DataResult.Success)
        assertEquals(overrideId, result.dataOrNull?.id)
        assertEquals(true, result.dataOrNull?.markedComplete)
    }

    @Test
    fun `updatePlannerOverride returns failure when API call fails`() = runTest {
        val overrideId = 123L

        coEvery {
            plannerApi.updatePlannerOverride(
                plannerOverrideId = overrideId,
                complete = false,
                params = any()
            )
        } returns DataResult.Fail()

        val result = repository.updatePlannerOverride(overrideId, markedComplete = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `updatePlannerOverride always uses forceRefresh`() = runTest {
        val overrideId = 123L

        coEvery {
            plannerApi.updatePlannerOverride(
                plannerOverrideId = any(),
                complete = any(),
                params = any()
            )
        } returns DataResult.Success(mockk(relaxed = true))

        repository.updatePlannerOverride(overrideId, markedComplete = true)

        coVerify {
            plannerApi.updatePlannerOverride(
                plannerOverrideId = overrideId,
                complete = true,
                params = match<RestParams> { it.isForceReadFromNetwork }
            )
        }
    }

    // createPlannerOverride tests
    @Test
    fun `createPlannerOverride returns success with created override`() = runTest {
        val plannableId = 456L
        val plannableType = PlannableType.ASSIGNMENT
        val override = PlannerOverride(
            id = 789L,
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )

        coEvery {
            plannerApi.createPlannerOverride(
                plannerOverride = any(),
                params = any()
            )
        } returns DataResult.Success(override)

        val result = repository.createPlannerOverride(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = true
        )

        assertTrue(result is DataResult.Success)
        assertEquals(plannableId, result.dataOrNull?.plannableId)
        assertEquals(plannableType, result.dataOrNull?.plannableType)
        assertEquals(true, result.dataOrNull?.markedComplete)
    }

    @Test
    fun `createPlannerOverride returns failure when API call fails`() = runTest {
        coEvery {
            plannerApi.createPlannerOverride(
                plannerOverride = any(),
                params = any()
            )
        } returns DataResult.Fail()

        val result = repository.createPlannerOverride(
            plannableId = 456L,
            plannableType = PlannableType.QUIZ,
            markedComplete = false
        )

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `createPlannerOverride passes correct parameters to API`() = runTest {
        val plannableId = 456L
        val plannableType = PlannableType.DISCUSSION_TOPIC

        coEvery {
            plannerApi.createPlannerOverride(
                plannerOverride = any(),
                params = any()
            )
        } returns DataResult.Success(mockk(relaxed = true))

        repository.createPlannerOverride(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = false
        )

        coVerify {
            plannerApi.createPlannerOverride(
                plannerOverride = match<PlannerOverride> {
                    it.plannableId == plannableId &&
                            it.plannableType == plannableType && !it.markedComplete
                },
                params = match<RestParams> { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `createPlannerOverride always uses forceRefresh`() = runTest {
        coEvery {
            plannerApi.createPlannerOverride(
                plannerOverride = any(),
                params = any()
            )
        } returns DataResult.Success(mockk(relaxed = true))

        repository.createPlannerOverride(
            plannableId = 456L,
            plannableType = PlannableType.PLANNER_NOTE,
            markedComplete = true
        )

        coVerify {
            plannerApi.createPlannerOverride(
                plannerOverride = any(),
                params = match<RestParams> { it.isForceReadFromNetwork }
            )
        }
    }

    // Helper function to create test PlannerItem
    private fun createPlannerItem(
        id: Long,
        title: String,
        plannableType: PlannableType = PlannableType.ASSIGNMENT
    ): PlannerItem {
        return PlannerItem(
            courseId = 1L,
            groupId = null,
            userId = null,
            contextType = "Course",
            contextName = "Test Course",
            plannableType = plannableType,
            plannable = Plannable(
                id = id,
                title = title,
                courseId = 1L,
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
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

package com.instructure.student.widget.todo

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date


class ToDoWidgetRepositoryTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupsApi: GroupAPI.GroupInterface = mockk(relaxed = true)

    private val repository: ToDoWidgetRepository = ToDoWidgetRepository(plannerApi, coursesApi, groupsApi)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when planner api request fails`() = runTest {
        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Fail()

        repository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)
    }

    @Test
    fun `Return results from the planner api and filter announcements and assessment requests on successful request`() = runTest {
        val filteredItem = createPlannerItem(1, 3, PlannableType.ANNOUNCEMENT)
        val filteredItem2 = createPlannerItem(1, 7, PlannableType.ASSESSMENT_REQUEST)
        val plannerItems = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT),
            createPlannerItem(1, 2, PlannableType.QUIZ),
            filteredItem,
            createPlannerItem(1, 4, PlannableType.DISCUSSION_TOPIC),
            createPlannerItem(2, 5, PlannableType.PLANNER_NOTE),
            createPlannerItem(2, 6, PlannableType.CALENDAR_EVENT)
        )

        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Success(plannerItems)

        val result = repository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)

        assertEquals(plannerItems.minus(listOf(filteredItem, filteredItem2).toSet()), result)
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

        val result = repository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)

        assertEquals(plannerItems1.plus(plannerItems2), result)
    }

    @Test
    fun `Returns empty list when course call fails`() = runTest {
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Fail()

        val result = repository.getFavouriteCourses(true)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Returns favourite courses only when the call is successful`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = false),
            Course(id = 3, name = "Course 3", isFavorite = true)
        )

        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)

        val result = repository.getFavouriteCourses(true)

        assertEquals(courses.filter { it.isFavorite }, result)
    }

    @Test
    fun `Returns all courses when theres no favorite course and the call is successful`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = false),
            Course(id = 2, name = "Course 2", isFavorite = false),
            Course(id = 3, name = "Course 3", isFavorite = false)
        )

        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)

        val result = repository.getFavouriteCourses(true)

        assertEquals(courses, result)
    }

    @Test
    fun `Returns courses depaginated`() = runTest {
        val page1 = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true)
        )
        val page2 = listOf(
            Course(id = 2, name = "Course 2", isFavorite = true)
        )

        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(page1, LinkHeaders(nextUrl = "next"))
        coEvery { coursesApi.next(any(), any()) } returns DataResult.Success(page2)

        val result = repository.getFavouriteCourses(true)

        assertEquals(page1 + page2, result)
    }

    @Test
    fun `Returns empty list when groups call fails`() = runTest {
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Fail()

        val result = repository.getFavouriteGroups(true)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Returns favourite groups only when the call is successful`() = runTest {
        val groups = listOf(
            Group(id = 1, name = "Group 1", isFavorite = true),
            Group(id = 2, name = "Group 2", isFavorite = false),
            Group(id = 3, name = "Group 3", isFavorite = true)
        )

        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val result = repository.getFavouriteGroups(true)

        assertEquals(groups.filter { it.isFavorite }, result)
    }

    @Test
    fun `Returns all groups when theres no favorite group and the call is successful`() = runTest {
        val groups = listOf(
            Group(id = 1, name = "Group 1", isFavorite = false),
            Group(id = 2, name = "Group 2", isFavorite = false),
            Group(id = 3, name = "Group 3", isFavorite = false)
        )

        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val result = repository.getFavouriteGroups(true)

        assertEquals(groups, result)
    }

    @Test
    fun `Returns groups depaginated`() = runTest {
        val page1 = listOf(
            Group(id = 1, name = "Group 1", isFavorite = true)
        )
        val page2 = listOf(
            Group(id = 2, name = "Group 2", isFavorite = true)
        )

        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(page1, LinkHeaders(nextUrl = "next"))
        coEvery { groupsApi.getNextPageGroups(any(), any()) } returns DataResult.Success(page2)

        val result = repository.getFavouriteGroups(true)

        assertEquals(page1 + page2, result)
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

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
package com.instructure.horizon.features.dashboard.widget.timespent

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.journey.GetWidgetDataQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class DashboardTimeSpentRepositoryTest {

    private val getWidgetsManager: GetWidgetsManager = mockk(relaxed = true)
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var repository: DashboardTimeSpentRepository

    @Before
    fun setup() {
        val user = User(id = 1L)
        every { apiPrefs.user } returns user
        repository = DashboardTimeSpentRepository(getWidgetsManager, getCoursesManager, apiPrefs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getTimeSpentData returns data successfully`() = runTest {
        val graphqlData = GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf(
                    "date" to "2025-10-08",
                    "user_id" to 1.0,
                    "course_id" to 101.0,
                    "course_name" to "Test Course",
                    "minutes_per_day" to 120.0
                )
            )
        )

        coEvery { getWidgetsManager.getTimeSpentWidgetData(null, false) } returns graphqlData

        val result = repository.getTimeSpentData(null, false)

        assertEquals(graphqlData.lastModifiedDate, result.lastModifiedDate)
        assertEquals(1, result.data.size)
        assertEquals(101L, result.data[0].courseId)
        assertEquals("Test Course", result.data[0].courseName)
        assertEquals(120, result.data[0].minutesPerDay)
        coVerify { getWidgetsManager.getTimeSpentWidgetData(null, false) }
    }

    @Test
    fun `getTimeSpentData with courseId passes courseId to manager`() = runTest {
        val courseId = 123L
        val graphqlData = GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf(
                    "date" to "2025-10-08",
                    "course_id" to 123.0,
                    "course_name" to "Filtered Course",
                    "minutes_per_day" to 90.0
                )
            )
        )

        coEvery { getWidgetsManager.getTimeSpentWidgetData(courseId, false) } returns graphqlData

        val result = repository.getTimeSpentData(courseId, false)

        assertEquals(graphqlData.lastModifiedDate, result.lastModifiedDate)
        assertEquals(1, result.data.size)
        assertEquals(123L, result.data[0].courseId)
        coVerify { getWidgetsManager.getTimeSpentWidgetData(courseId, false) }
    }

    @Test
    fun `getTimeSpentData with forceNetwork true uses network`() = runTest {
        val graphqlData = GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf(
                    "minutes_per_day" to 150.0
                )
            )
        )

        coEvery { getWidgetsManager.getTimeSpentWidgetData(null, true) } returns graphqlData

        val result = repository.getTimeSpentData(null, true)

        assertEquals(graphqlData.lastModifiedDate, result.lastModifiedDate)
        assertEquals(150, result.data[0].minutesPerDay)
        coVerify { getWidgetsManager.getTimeSpentWidgetData(null, true) }
    }

    @Test
    fun `getCourses returns courses successfully`() = runTest {
        val userId = 1L
        val courses = listOf(
            CourseWithProgress(courseId = 1L, courseName = "Course 1", courseImageUrl = null, courseSyllabus = null, progress = 0.5),
            CourseWithProgress(courseId = 2L, courseName = "Course 2", courseImageUrl = null, courseSyllabus = null, progress = 0.8)
        )
        val dataResult = DataResult.Success(courses)

        coEvery { getCoursesManager.getCoursesWithProgress(userId, false) } returns dataResult

        val result = repository.getCourses(false)

        assertEquals(courses, result)
        coVerify { getCoursesManager.getCoursesWithProgress(userId, false) }
    }

    @Test
    fun `getCourses with forceNetwork true uses network`() = runTest {
        val userId = 1L
        val courses = listOf(
            CourseWithProgress(courseId = 3L, courseName = "Course 3", courseImageUrl = null, courseSyllabus = null, progress = 0.9)
        )
        val dataResult = DataResult.Success(courses)

        coEvery { getCoursesManager.getCoursesWithProgress(userId, true) } returns dataResult

        val result = repository.getCourses(true)

        assertEquals(courses, result)
        coVerify { getCoursesManager.getCoursesWithProgress(userId, true) }
    }

    @Test
    fun `getCourses returns empty list when no courses`() = runTest {
        val userId = 1L
        val courses = emptyList<CourseWithProgress>()
        val dataResult = DataResult.Success(courses)

        coEvery { getCoursesManager.getCoursesWithProgress(userId, false) } returns dataResult

        val result = repository.getCourses(false)

        assertEquals(emptyList<CourseWithProgress>(), result)
    }

    @Test(expected = Exception::class)
    fun `getCourses throws exception on failure`() = runTest {
        val userId = 1L

        coEvery { getCoursesManager.getCoursesWithProgress(userId, false) } returns DataResult.Fail()

        repository.getCourses(false)
    }

    @Test(expected = Exception::class)
    fun `getTimeSpentData propagates exceptions`() = runTest {
        coEvery { getWidgetsManager.getTimeSpentWidgetData(null, false) } throws Exception("Network error")

        repository.getTimeSpentData(null, false)
    }
}

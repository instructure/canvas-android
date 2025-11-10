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
package com.instructure.horizon.features.home

import com.instructure.canvasapi2.apis.ThemeAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HomeRepositoryTest {
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val themeApi: ThemeAPI.ThemeInterface = mockk(relaxed = true)
    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)

    private val userId = 1L

    @Before
    fun setup() {
        every { apiPrefs.user } returns User(id = userId, name = "Test User")
    }

    @Test
    fun `Test successful theme retrieval`() = runTest {
        val theme = CanvasTheme("", "", "", "", "", "", "", "")
        coEvery { themeApi.getTheme(any()) } returns DataResult.Success(theme)

        val result = getRepository().getTheme()

        assertEquals(theme, result)
    }

    @Test
    fun `Test failed theme retrieval returns null`() = runTest {
        coEvery { themeApi.getTheme(any()) } returns DataResult.Fail()

        val result = getRepository().getTheme()

        assertNull(result)
    }

    @Test
    fun `Test successful user retrieval`() = runTest {
        val user = User(id = userId, name = "Test User", locale = "en")
        coEvery { userApi.getSelf(any()) } returns DataResult.Success(user)

        val result = getRepository().getSelf()

        assertEquals(user, result)
    }

    @Test
    fun `Test failed user retrieval returns null`() = runTest {
        coEvery { userApi.getSelf(any()) } returns DataResult.Fail()

        val result = getRepository().getSelf()

        assertNull(result)
    }

    @Test
    fun `Test successful courses retrieval`() = runTest {
        val courses = listOf(
            CourseWithProgress(
                courseId = 1L,
                courseName = "Course 1",
                courseSyllabus = "",
                progress = 50.0
            ),
            CourseWithProgress(
                courseId = 2L,
                courseName = "Course 2",
                courseSyllabus = "",
                progress = 75.0
            )
        )
        coEvery { getCoursesManager.getCoursesWithProgress(userId, false) } returns DataResult.Success(courses)

        val result = getRepository().getCourses()

        assertEquals(2, result.size)
        assertEquals(courses, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed courses retrieval throws exception`() = runTest {
        coEvery { getCoursesManager.getCoursesWithProgress(userId, false) } returns DataResult.Fail()

        getRepository().getCourses()
    }

    private fun getRepository(): HomeRepository {
        return HomeRepository(apiPrefs, themeApi, userApi, getCoursesManager)
    }
}

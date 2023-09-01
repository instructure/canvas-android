/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.navigation.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@ExperimentalCoroutinesApi
class NavigationNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val dataSource = NavigationNetworkDataSource(courseApi)

    @Test
    fun `Get course successfully returns data`() = runTest {
        val expected = Course(1)

        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getCourse(1, true)

        assertEquals(expected, result)
    }

    fun `Get course failure returns null`() = runTest {
        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Fail()

        val result = dataSource.getCourse(1, true)

        assertNull(result)
    }
}

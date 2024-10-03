/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.courses.details

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class CourseDetailsRepositoryTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val tabApi: TabAPI.TabsInterface = mockk(relaxed = true)

    private val repository = CourseDetailsRepository(courseApi, tabApi)

    @Test
    fun `Get course details successfully returns data`() = runTest {
        val expected = Course(id = 1L)

        coEvery { courseApi.getCourseWithSyllabus(1L, RestParams(isForceReadFromNetwork = false)) } returns DataResult.Success(expected)

        val result = repository.getCourse(1L, false)
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course details throws exception when fails`() = runTest {
        coEvery { courseApi.getCourseWithSyllabus(1L, RestParams(isForceReadFromNetwork = true)) } returns DataResult.Fail()

        repository.getCourse(1L, true)
    }

    @Test
    fun `Get course tabs successfully returns data`() = runTest {
        val expected = listOf(Tab("tabId1"), Tab("tabId2"))

        coEvery {
            tabApi.getTabs(
                1L,
                CanvasContext.Type.COURSE.apiString,
                RestParams(isForceReadFromNetwork = false)
            )
        } returns DataResult.Success(
            expected
        )

        val result = repository.getCourseTabs(1L, false)
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course tabs throws exception when fails`() = runTest {
        coEvery {
            tabApi.getTabs(
                1L,
                CanvasContext.Type.COURSE.apiString,
                RestParams(isForceReadFromNetwork = true)
            )
        } returns DataResult.Fail()

        repository.getCourseTabs(1L, true)
    }
}

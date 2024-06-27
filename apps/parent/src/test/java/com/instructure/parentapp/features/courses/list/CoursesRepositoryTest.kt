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
 *
 */

package com.instructure.parentapp.features.courses.list

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class CoursesRepositoryTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val repository = CoursesRepository(courseApi)

    @Test
    fun `Get courses successfully returns data`() = runTest {
        val expected = listOf(Course(id = 1L, enrollments = mutableListOf(Enrollment(userId = 1L))))

        coEvery { courseApi.firstPageObserveeCourses(RestParams(isForceReadFromNetwork = false)) } returns DataResult.Success(expected)

        val result = repository.getCourses(1L, false)
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Get courses with pagination successfully returns data`() = runTest {
        val page1 = listOf(Course(id = 1L, enrollments = mutableListOf(Enrollment(userId = 1L))))
        val page2 = listOf(Course(id = 2L, enrollments = mutableListOf(Enrollment(userId = 1L))))

        coEvery { courseApi.firstPageObserveeCourses(RestParams(isForceReadFromNetwork = true)) } returns DataResult.Success(
            page1,
            linkHeaders = LinkHeaders(nextUrl = "page_2_url")
        )
        coEvery { courseApi.next("page_2_url", any()) } returns DataResult.Success(page2)

        val result = repository.getCourses(1L, true)
        Assert.assertEquals(page1 + page2, result)
    }

    @Test
    fun `Get courses filters out courses not belonging to student`() = runTest {
        val expected = listOf(Course(id = 1L, enrollments = mutableListOf(Enrollment(userId = 1L))))
        val unexpected = listOf(Course(id = 2L, enrollments = mutableListOf(Enrollment(userId = 2L))))

        coEvery { courseApi.firstPageObserveeCourses(any()) } returns DataResult.Success(expected + unexpected)

        val result = repository.getCourses(1L, true)
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Get courses throws exception when call fails`() = runTest {
        coEvery { courseApi.firstPageObserveeCourses(any()) } throws IllegalArgumentException()

        repository.getCourses(1L, true)
    }
}

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
 *
 */
package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CourseRepositoryTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private lateinit var repository: CourseRepository

    @Before
    fun setup() {
        repository = CourseRepositoryImpl(courseApi)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getCourse returns success with course`() = runTest {
        val course = Course(id = 100L, name = "Test Course")
        val expected = DataResult.Success(course)
        coEvery {
            courseApi.getCourse(any(), any())
        } returns expected

        val result = repository.getCourse(
            courseId = 100L,
            forceRefresh = false
        )

        assertEquals(expected, result)
        coVerify {
            courseApi.getCourse(
                100L,
                match { !it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getCourse with forceRefresh passes correct params`() = runTest {
        val course = Course(id = 200L, name = "Another Course")
        val expected = DataResult.Success(course)
        coEvery {
            courseApi.getCourse(any(), any())
        } returns expected

        val result = repository.getCourse(
            courseId = 200L,
            forceRefresh = true
        )

        assertEquals(expected, result)
        coVerify {
            courseApi.getCourse(
                200L,
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getCourse returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            courseApi.getCourse(any(), any())
        } returns expected

        val result = repository.getCourse(
            courseId = 100L,
            forceRefresh = false
        )

        assertEquals(expected, result)
    }
}
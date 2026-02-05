/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.data.repository.coursenickname

import com.instructure.canvasapi2.apis.CourseNicknameAPI
import com.instructure.canvasapi2.models.CourseNickname
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

class CourseNicknameRepositoryTest {

    private val courseNicknameApi: CourseNicknameAPI.NicknameInterface = mockk(relaxed = true)
    private lateinit var repository: CourseNicknameRepository

    @Before
    fun setup() {
        repository = CourseNicknameRepositoryImpl(courseNicknameApi)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `setCourseNickname returns success`() = runTest {
        val courseId = 123L
        val nickname = "My Favorite Course"
        val courseNickname = CourseNickname(id = courseId, nickname = nickname, name = "Course Name")
        val expected = DataResult.Success(courseNickname)
        coEvery {
            courseNicknameApi.setNickname(courseId, nickname, any())
        } returns expected

        val result = repository.setCourseNickname(courseId, nickname)

        assertEquals(expected, result)
        coVerify {
            courseNicknameApi.setNickname(courseId, nickname, match { it.isForceReadFromNetwork })
        }
    }

    @Test
    fun `setCourseNickname returns failure`() = runTest {
        val courseId = 123L
        val nickname = "My Favorite Course"
        val expected = DataResult.Fail()
        coEvery {
            courseNicknameApi.setNickname(courseId, nickname, any())
        } returns expected

        val result = repository.setCourseNickname(courseId, nickname)

        assertEquals(expected, result)
    }

    @Test
    fun `setCourseNickname handles empty nickname`() = runTest {
        val courseId = 123L
        val nickname = ""
        val courseNickname = CourseNickname(id = courseId, nickname = nickname, name = "Course Name")
        val expected = DataResult.Success(courseNickname)
        coEvery {
            courseNicknameApi.setNickname(courseId, nickname, any())
        } returns expected

        val result = repository.setCourseNickname(courseId, nickname)

        assertEquals(expected, result)
        assertEquals("", (result as DataResult.Success).data.nickname)
    }

    @Test
    fun `deleteCourseNickname returns success`() = runTest {
        val courseId = 123L
        val courseNickname = CourseNickname(id = courseId, nickname = "", name = "Course Name")
        val expected = DataResult.Success(courseNickname)
        coEvery {
            courseNicknameApi.deleteNickname(courseId, any())
        } returns expected

        val result = repository.deleteCourseNickname(courseId)

        assertEquals(expected, result)
        coVerify {
            courseNicknameApi.deleteNickname(courseId, match { it.isForceReadFromNetwork })
        }
    }

    @Test
    fun `deleteCourseNickname returns failure`() = runTest {
        val courseId = 123L
        val expected = DataResult.Fail()
        coEvery {
            courseNicknameApi.deleteNickname(courseId, any())
        } returns expected

        val result = repository.deleteCourseNickname(courseId)

        assertEquals(expected, result)
    }
}
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
package com.instructure.pandautils.data.repository.user

import com.instructure.canvasapi2.apis.CourseNicknameAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.ColorChangeResponse
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

class UserRepositoryTest {

    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)
    private val courseNicknameApi: CourseNicknameAPI.NicknameInterface = mockk(relaxed = true)
    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        repository = UserRepositoryImpl(userApi, courseNicknameApi)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getAccount returns success with account data`() = runTest {
        val account = Account(id = 1L, name = "Test Institution")
        val expected = DataResult.Success(account)
        coEvery {
            userApi.getAccount(any())
        } returns expected

        val result = repository.getAccount(forceRefresh = false)

        assertEquals(expected, result)
        coVerify {
            userApi.getAccount(match { !it.isForceReadFromNetwork })
        }
    }

    @Test
    fun `getAccount with forceRefresh passes correct params`() = runTest {
        val account = Account(id = 1L, name = "Test Institution")
        val expected = DataResult.Success(account)
        coEvery {
            userApi.getAccount(any())
        } returns expected

        repository.getAccount(forceRefresh = true)

        coVerify {
            userApi.getAccount(match { it.isForceReadFromNetwork })
        }
    }

    @Test
    fun `getAccount returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            userApi.getAccount(any())
        } returns expected

        val result = repository.getAccount(forceRefresh = false)

        assertEquals(expected, result)
    }

    @Test
    fun `getAccount with forceRefresh false uses cache`() = runTest {
        val account = Account(id = 1L, name = "Cached Institution")
        val expected = DataResult.Success(account)
        coEvery {
            userApi.getAccount(any())
        } returns expected

        val result = repository.getAccount(forceRefresh = false)

        assertEquals(expected, result)
        coVerify(exactly = 1) {
            userApi.getAccount(match { !it.isForceReadFromNetwork })
        }
    }

    @Test
    fun `getAccount handles account with empty name`() = runTest {
        val account = Account(id = 1L, name = "")
        val expected = DataResult.Success(account)
        coEvery {
            userApi.getAccount(any())
        } returns expected

        val result = repository.getAccount(forceRefresh = false)

        assertEquals(expected, result)
        assertEquals("", (result as DataResult.Success).data.name)
    }

    @Test
    fun `getAccount handles multiple consecutive calls`() = runTest {
        val account1 = Account(id = 1L, name = "First Call")
        val account2 = Account(id = 1L, name = "Second Call")
        coEvery {
            userApi.getAccount(any())
        } returnsMany listOf(DataResult.Success(account1), DataResult.Success(account2))

        val result1 = repository.getAccount(forceRefresh = false)
        val result2 = repository.getAccount(forceRefresh = true)

        assertEquals("First Call", (result1 as DataResult.Success).data.name)
        assertEquals("Second Call", (result2 as DataResult.Success).data.name)
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

    @Test
    fun `setCourseColor returns success`() = runTest {
        val contextId = "course_123"
        val color = 0xFF2573DF.toInt()
        val colorResponse = ColorChangeResponse(hexCode = "#2573DF")
        val expected = DataResult.Success(colorResponse)
        coEvery {
            userApi.setColor(contextId, any(), any())
        } returns expected

        val result = repository.setCourseColor(contextId, color)

        assertEquals(expected, result)
        coVerify {
            userApi.setColor(contextId, any(), match { it.isForceReadFromNetwork })
        }
    }

    @Test
    fun `setCourseColor returns failure`() = runTest {
        val contextId = "course_123"
        val color = 0xFF2573DF.toInt()
        val expected = DataResult.Fail()
        coEvery {
            userApi.setColor(contextId, any(), any())
        } returns expected

        val result = repository.setCourseColor(contextId, color)

        assertEquals(expected, result)
    }

    @Test
    fun `setCourseColor handles different color formats`() = runTest {
        val contextId = "course_123"
        val color = 0xFFE71F63.toInt()
        val colorResponse = ColorChangeResponse(hexCode = "#E71F63")
        val expected = DataResult.Success(colorResponse)
        coEvery {
            userApi.setColor(contextId, any(), any())
        } returns expected

        val result = repository.setCourseColor(contextId, color)

        assertEquals(expected, result)
        assertEquals("#E71F63", (result as DataResult.Success).data.hexCode)
    }
}
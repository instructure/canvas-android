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
 */
package com.instructure.pandautils.domain.usecase.course

import com.instructure.canvasapi2.models.ColorChangeResponse
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.user.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class SetCourseColorUseCaseTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val useCase = SetCourseColorUseCase(userRepository)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute calls setCourseColor with correct parameters`() = runTest {
        val contextId = "course_123"
        val color = 0xFF00FF00.toInt()
        val params = SetCourseColorParams(contextId, color)

        coEvery { userRepository.setCourseColor(contextId, color) } returns DataResult.Success(ColorChangeResponse(hexCode = "#00FF00"))

        useCase(params)

        coVerify(exactly = 1) { userRepository.setCourseColor(contextId, color) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when setCourseColor fails`() = runTest {
        val contextId = "course_123"
        val color = 0xFF00FF00.toInt()
        val params = SetCourseColorParams(contextId, color)

        coEvery { userRepository.setCourseColor(contextId, color) } returns DataResult.Fail()

        useCase(params)
    }

    @Test
    fun `execute handles different color values`() = runTest {
        val contextId = "course_456"
        val color = 0xFFFF0000.toInt()
        val params = SetCourseColorParams(contextId, color)

        coEvery { userRepository.setCourseColor(contextId, color) } returns DataResult.Success(ColorChangeResponse(hexCode = "#FF0000"))

        useCase(params)

        coVerify(exactly = 1) { userRepository.setCourseColor(contextId, color) }
    }

    @Test
    fun `execute handles different context IDs`() = runTest {
        val contextId = "course_789"
        val color = 0xFF0000FF.toInt()
        val params = SetCourseColorParams(contextId, color)

        coEvery { userRepository.setCourseColor(contextId, color) } returns DataResult.Success(ColorChangeResponse(hexCode = "#0000FF"))

        useCase(params)

        coVerify(exactly = 1) { userRepository.setCourseColor(contextId, color) }
    }
}
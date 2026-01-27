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

import com.instructure.canvasapi2.models.CourseNickname
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.coursenickname.CourseNicknameRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class SetCourseNicknameUseCaseTest {

    private val courseNicknameRepository: CourseNicknameRepository = mockk(relaxed = true)
    private val useCase = SetCourseNicknameUseCase(courseNicknameRepository)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute with non-empty nickname calls setCourseNickname`() = runTest {
        val courseId = 1L
        val nickname = "My Course"
        val params = SetCourseNicknameUseCase.Params(courseId, nickname)

        coEvery { courseNicknameRepository.setCourseNickname(courseId, nickname) } returns DataResult.Success(CourseNickname())

        useCase(params)

        coVerify(exactly = 1) { courseNicknameRepository.setCourseNickname(courseId, nickname) }
        coVerify(exactly = 0) { courseNicknameRepository.deleteCourseNickname(any()) }
    }

    @Test
    fun `execute with empty nickname calls deleteCourseNickname`() = runTest {
        val courseId = 1L
        val nickname = ""
        val params = SetCourseNicknameUseCase.Params(courseId, nickname)

        coEvery { courseNicknameRepository.deleteCourseNickname(courseId) } returns DataResult.Success(CourseNickname())

        useCase(params)

        coVerify(exactly = 1) { courseNicknameRepository.deleteCourseNickname(courseId) }
        coVerify(exactly = 0) { courseNicknameRepository.setCourseNickname(any(), any()) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when setCourseNickname fails`() = runTest {
        val courseId = 1L
        val nickname = "My Course"
        val params = SetCourseNicknameUseCase.Params(courseId, nickname)

        coEvery { courseNicknameRepository.setCourseNickname(courseId, nickname) } returns DataResult.Fail()

        useCase(params)
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when deleteCourseNickname fails`() = runTest {
        val courseId = 1L
        val nickname = ""
        val params = SetCourseNicknameUseCase.Params(courseId, nickname)

        coEvery { courseNicknameRepository.deleteCourseNickname(courseId) } returns DataResult.Fail()

        useCase(params)
    }
}
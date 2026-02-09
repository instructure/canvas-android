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

import com.instructure.pandautils.data.repository.coursenickname.CourseNicknameRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class SetCourseNicknameUseCase @Inject constructor(
    private val courseNicknameRepository: CourseNicknameRepository
) : BaseUseCase<SetCourseNicknameUseCase.Params, Unit>() {

    data class Params(
        val courseId: Long,
        val nickname: String
    )

    override suspend fun execute(params: Params) {
        if (params.nickname.isEmpty()) {
            courseNicknameRepository.deleteCourseNickname(params.courseId).dataOrThrow
        } else {
            courseNicknameRepository.setCourseNickname(
                courseId = params.courseId,
                nickname = params.nickname
            ).dataOrThrow
        }
    }
}

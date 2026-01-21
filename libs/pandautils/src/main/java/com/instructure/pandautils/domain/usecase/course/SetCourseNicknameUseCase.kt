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

import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class SetCourseNicknameParams(
    val courseId: Long,
    val nickname: String
)

class SetCourseNicknameUseCase @Inject constructor(
    private val userRepository: UserRepository
) : BaseUseCase<SetCourseNicknameParams, Unit>() {

    override suspend fun execute(params: SetCourseNicknameParams) {
        if (params.nickname.isEmpty()) {
            userRepository.deleteCourseNickname(params.courseId).dataOrThrow
        } else {
            userRepository.setCourseNickname(
                courseId = params.courseId,
                nickname = params.nickname
            ).dataOrThrow
        }
    }
}

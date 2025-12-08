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
package com.instructure.pandautils.domain.usecase.enrollment

import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class HandleCourseInvitationParams(
    val courseId: Long,
    val enrollmentId: Long,
    val accept: Boolean
)

class HandleCourseInvitationUseCase @Inject constructor(
    private val enrollmentRepository: EnrollmentRepository
) : BaseUseCase<HandleCourseInvitationParams, Unit>() {

    override suspend fun execute(params: HandleCourseInvitationParams) {
        enrollmentRepository.handleInvitation(
            courseId = params.courseId,
            enrollmentId = params.enrollmentId,
            accept = params.accept
        ).dataOrThrow
    }
}
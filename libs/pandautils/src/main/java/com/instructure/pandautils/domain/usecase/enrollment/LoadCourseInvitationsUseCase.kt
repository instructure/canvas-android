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

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import com.instructure.pandautils.domain.usecase.BaseUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class LoadCourseInvitationsParams(
    val forceRefresh: Boolean = false
)

class LoadCourseInvitationsUseCase @Inject constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository
) : BaseUseCase<LoadCourseInvitationsParams, List<CourseInvitation>>() {

    override suspend fun execute(params: LoadCourseInvitationsParams): List<CourseInvitation> {
        val enrollments = enrollmentRepository.getSelfEnrollments(
            types = null,
            states = listOf(EnrollmentAPI.STATE_INVITED, EnrollmentAPI.STATE_CURRENT_AND_FUTURE),
            forceRefresh = params.forceRefresh
        ).dataOrThrow.filter { it.enrollmentState == EnrollmentAPI.STATE_INVITED }

        return coroutineScope {
            enrollments.map { enrollment ->
                async {
                    val course = courseRepository.getCourse(enrollment.courseId, params.forceRefresh).dataOrThrow
                    if (course.isValidTerm() && !course.accessRestrictedByDate && course.isEnrollmentBeforeEndDateOrNotRestricted()) {
                        CourseInvitation(
                            enrollmentId = enrollment.id,
                            courseId = enrollment.courseId,
                            courseName = course.name,
                            userId = enrollment.userId
                        )
                    } else null
                }
            }.awaitAll().filterNotNull()
        }
    }
}
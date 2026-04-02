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
package com.instructure.horizon.domain.usecase

import com.instructure.canvasapi2.managers.graphql.horizon.DashboardEnrollment
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.horizon.model.DashboardNextModuleItem
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class DashboardCoursesData(
    val enrollments: List<DashboardEnrollment>,
    val programs: List<Program>,
    val unenrolledPrograms: List<Program>,
    val nextModuleItemByCourseId: Map<Long, DashboardNextModuleItem?>,
)

class GetDashboardCoursesUseCase @Inject constructor(
    private val getEnrollmentsUseCase: GetEnrollmentsUseCase,
    private val getProgramsUseCase: GetProgramsUseCase,
    private val getNextModuleItemUseCase: GetNextModuleItemUseCase,
    private val acceptCourseInviteUseCase: AcceptCourseInviteUseCase,
) : BaseUseCase<Unit, DashboardCoursesData>() {

    suspend operator fun invoke() = invoke(Unit)

    override suspend fun execute(params: Unit): DashboardCoursesData {
        var enrollments = getEnrollmentsUseCase()
        val programs = getProgramsUseCase()

        val invitations = enrollments.filter { it.enrollmentState == DashboardEnrollment.STATE_INVITED }
        if (invitations.isNotEmpty()) {
            invitations.forEach { enrollment ->
                acceptCourseInviteUseCase(
                    AcceptCourseInviteParams(
                        courseId = enrollment.courseId,
                        enrollmentId = enrollment.enrollmentId,
                    )
                )
            }
            enrollments = getEnrollmentsUseCase()
        }

        val nextModuleItemByCourseId = enrollments
            .filter { it.enrollmentState == DashboardEnrollment.STATE_ACTIVE }
            .associate { enrollment ->
                enrollment.courseId to getNextModuleItemUseCase(enrollment.courseId)
            }

        val unenrolledPrograms = programs.filter { program ->
            program.sortedRequirements.none { it.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED }
        }

        return DashboardCoursesData(
            enrollments = enrollments,
            programs = programs,
            unenrolledPrograms = unenrolledPrograms,
            nextModuleItemByCourseId = nextModuleItemByCourseId,
        )
    }
}

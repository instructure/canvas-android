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

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.horizon.data.repository.CourseScoreRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class CourseScoreData(
    val assignmentGroups: List<AssignmentGroup>,
    val enrollments: List<Enrollment>,
)

class GetCourseScoreDataUseCase @Inject constructor(
    private val repository: CourseScoreRepository,
) : BaseUseCase<GetCourseScoreDataUseCase.Params, CourseScoreData>() {

    data class Params(
        val courseId: Long,
        val forceRefresh: Boolean = false,
    )

    override suspend fun execute(params: Params): CourseScoreData {
        val assignmentGroups = repository.getAssignmentGroups(params.courseId, params.forceRefresh)
        val enrollments = repository.getEnrollments(params.courseId, params.forceRefresh)
        repository.saveScoreData(params.courseId, assignmentGroups, enrollments)
        return CourseScoreData(assignmentGroups, enrollments)
    }
}

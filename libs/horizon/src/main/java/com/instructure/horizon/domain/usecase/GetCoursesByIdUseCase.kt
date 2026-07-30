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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.horizon.data.repository.ProgramRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetCoursesByIdUseCase @Inject constructor(
    private val repository: ProgramRepository,
) : BaseUseCase<GetCoursesByIdUseCase.Params, List<CourseWithModuleItemDurations>>() {

    data class Params(
        val courseIds: List<Long>,
        val forceRefresh: Boolean = false,
    )

    override suspend fun execute(params: Params): List<CourseWithModuleItemDurations> {
        return repository.getCoursesById(params.courseIds, params.forceRefresh)
    }
}

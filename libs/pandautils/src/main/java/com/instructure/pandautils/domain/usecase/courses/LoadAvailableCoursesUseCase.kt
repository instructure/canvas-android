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
 */
package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadAvailableCoursesParams(
    val forceRefresh: Boolean = false
)

class LoadAvailableCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) : BaseUseCase<LoadAvailableCoursesParams, List<Course>>() {

    override suspend fun execute(params: LoadAvailableCoursesParams): List<Course> {
        val courses = courseRepository.getCourses(params.forceRefresh).dataOrThrow

        // Filter courses - exclude access restricted and invited courses
        return courses.filter {
            !it.accessRestrictedByDate && !it.isInvited()
        }
    }
}
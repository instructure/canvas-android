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
package com.instructure.pandautils.domain.usecase.notes

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * The courses the current user is enrolled in as a student - the course list the Notebook Global level
 * fans its notes fetch out over, and the source of course names for Global's section headers. Always
 * reads cache-first - the user's course list rarely changes, so there's no need to force a network
 * round-trip here.
 */
class GetEligibleCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) : BaseUseCase<Unit, List<Course>>() {

    override suspend fun execute(params: Unit): List<Course> {
        return courseRepository.getCourses(forceRefresh = false).dataOrThrow.filter { it.isStudent }
    }
}

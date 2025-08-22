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
package com.instructure.horizon.features.learn.programs

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.JourneyApiManager
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.models.Course
import javax.inject.Inject

class ProgramDetailsRepository @Inject constructor(
    private val journeyApiManager: JourneyApiManager,
    private val courseApi: CourseAPI.CoursesInterface
) {
    suspend fun getProgramDetails(programId: String, forceNetwork: Boolean = true): Program {
        val program = journeyApiManager.getPrograms(forceNetwork).find { it.id == programId }
        // TODO Error handling
        return program!!
    }

    suspend fun getCoursesById(courseIds: List<Long>, forceNetwork: Boolean = true): List<Course> {
        // TODO We might want to optimize the fetching here
        return courseIds.mapNotNull {
            courseApi.getCourse(it, RestParams(isForceReadFromNetwork = forceNetwork)).dataOrNull
        }
    }
}
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
package com.instructure.horizon.features.learn.program

import com.instructure.canvasapi2.managers.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.JourneyApiManager
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.utils.DataResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ProgramDetailsRepository @Inject constructor(
    private val journeyApiManager: JourneyApiManager,
    private val getCoursesManager: HorizonGetCoursesManager
) {
    suspend fun getProgramDetails(programId: String, forceNetwork: Boolean = false): Program {
        val program = journeyApiManager.getPrograms(forceNetwork).find { it.id == programId }
            ?: throw IllegalArgumentException("Program with id $programId not found")
        return program
    }

    suspend fun getCoursesById(courseIds: List<Long>, forceNetwork: Boolean = false): List<CourseWithModuleItemDurations> = coroutineScope {
        courseIds.map { id ->
            async { getCoursesManager.getProgramCourses(id, forceNetwork).dataOrThrow }
        }.awaitAll()
    }

    suspend fun enrollCourse(progressId: String): DataResult<Unit> {
        return journeyApiManager.enrollCourse(progressId)
    }
}
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
package com.instructure.horizon.features.learn

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.utils.ApiPrefs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LearnRepository @Inject constructor(
    private val horizonGetCoursesManager: HorizonGetCoursesManager,
    private val getProgramsManager: GetProgramsManager,
    private val apiPrefs: ApiPrefs
) {
    suspend fun getCoursesWithProgress(forceNetwork: Boolean): List<CourseWithProgress> {
        val courseWithProgress = horizonGetCoursesManager.getCoursesWithProgress(apiPrefs.user?.id ?: -1, forceNetwork).dataOrThrow
        return courseWithProgress
    }

    suspend fun getPrograms(forceNetwork: Boolean = false): List<Program> {
        return getProgramsManager.getPrograms(forceNetwork)
    }

    suspend fun getCoursesById(courseIds: List<Long>, forceNetwork: Boolean = false): List<CourseWithModuleItemDurations> = coroutineScope {
        courseIds.map { id ->
            async { horizonGetCoursesManager.getProgramCourses(id, forceNetwork).dataOrThrow }
        }.awaitAll()
    }
}
/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.learn.program.list

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LearnProgramListRepository @Inject constructor(
    private val getProgramsManager: GetProgramsManager,
    private val getCoursesManager: HorizonGetCoursesManager
) {
    suspend fun getPrograms(forceRefresh: Boolean): List<Program> {
        return getProgramsManager.getPrograms(forceRefresh)
    }

    suspend fun getCoursesById(courseIds: List<Long>, forceNetwork: Boolean = false): List<CourseWithModuleItemDurations> = coroutineScope {
        courseIds.map { id ->
            async { getCoursesManager.getProgramCourses(id, forceNetwork).dataOrThrow }
        }.awaitAll()
    }
}
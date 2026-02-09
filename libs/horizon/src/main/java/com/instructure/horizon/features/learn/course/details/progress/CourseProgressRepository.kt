/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.course.details.progress

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class CourseProgressRepository @Inject constructor(
    private val moduleApi: ModuleAPI.ModuleInterface
) {
    suspend fun getModuleItems(
        courseId: Long,
        forceRefresh: Boolean,
    ): List<ModuleObject> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return moduleApi.getFirstPageModulesWithItems(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            params,
            includes = listOf("estimated_durations")
        )
            .depaginate { moduleApi.getNextPageModuleObjectList(it, params) }
            .dataOrThrow
    }
}
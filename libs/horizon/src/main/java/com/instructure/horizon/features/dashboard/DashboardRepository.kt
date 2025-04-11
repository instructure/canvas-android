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
package com.instructure.horizon.features.dashboard

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.managers.GetCoursesManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val getCoursesManager: GetCoursesManager,
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val apiPrefs: ApiPrefs
) {
    suspend fun getCoursesWithProgress(forceNetwork: Boolean): DataResult<List<CourseWithProgress>> {
        return getCoursesManager.getCoursesWithProgress(apiPrefs.user?.id ?: -1, forceNetwork)
    }

    suspend fun getNextModule(courseId: Long, moduleId: Long, forceNetwork: Boolean): DataResult<ModuleObject> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return moduleApi.getModuleObject(CanvasContext.Type.COURSE.apiString, courseId, moduleId, params)
    }

    suspend fun getNextModuleItem(courseId: Long, moduleId: Long, moduleItemId: Long, forceNetwork: Boolean): DataResult<ModuleItem> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return moduleApi.getModuleItem(CanvasContext.Type.COURSE.apiString, courseId, moduleId, moduleItemId, params)
    }
}
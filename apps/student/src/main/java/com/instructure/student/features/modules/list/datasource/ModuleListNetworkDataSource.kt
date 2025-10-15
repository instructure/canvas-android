/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.modules.list.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.ModuleItemWithCheckpoints
import com.instructure.canvasapi2.managers.graphql.ModuleManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class ModuleListNetworkDataSource(
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val tabApi: TabAPI.TabsInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val moduleManager: ModuleManager) : ModuleListDataSource {

    override suspend fun getAllModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return moduleApi.getFirstPageModuleObjects(canvasContext.apiContext(), canvasContext.id, params).depaginate {
            moduleApi.getNextPageModuleObjectList(it, params)
        }
    }

    override suspend fun getFirstPageModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return moduleApi.getFirstPageModuleObjects(canvasContext.apiContext(), canvasContext.id, params)
    }

    suspend fun getNextPageModuleObjects(nextUrl: String, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return moduleApi.getNextPageModuleObjectList(nextUrl, params)
    }

    override suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<Tab>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return tabApi.getTabs(canvasContext.id, canvasContext.apiContext(), params)
    }

    override suspend fun getFirstPageModuleItems(canvasContext: CanvasContext, moduleId: Long, forceNetwork: Boolean): DataResult<List<ModuleItem>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return moduleApi.getFirstPageModuleItems(canvasContext.apiContext(), canvasContext.id, moduleId, params)
    }

    suspend fun getNextPageModuleItems(nextUrl: String, forceNetwork: Boolean): DataResult<List<ModuleItem>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return moduleApi.getNextPageModuleItemList(nextUrl, params)
    }

    override suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourseSettings(courseId, restParams).dataOrNull
    }

    suspend fun getModuleItemCheckpoints(courseId: String, forceNetwork: Boolean): List<ModuleItemWithCheckpoints> {
        return moduleManager.getModuleItemCheckpoints(courseId, forceNetwork)
    }
}
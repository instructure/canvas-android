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

import com.instructure.canvasapi2.managers.graphql.ModuleItemWithCheckpoints
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult

interface ModuleListDataSource {

    suspend fun getAllModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>>

    suspend fun getFirstPageModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>>

    suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<Tab>>

    suspend fun getFirstPageModuleItems(canvasContext: CanvasContext, moduleId: Long, forceNetwork: Boolean): DataResult<List<ModuleItem>>

    suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings?

    suspend fun getModuleItemCheckpoints(courseId: String, forceNetwork: Boolean): List<ModuleItemWithCheckpoints>
}
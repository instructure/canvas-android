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
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CheckpointDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.pandautils.utils.orDefault

class ModuleListLocalDataSource(
    private val tabDao: TabDao,
    private val moduleFacade: ModuleFacade,
    private val courseSettingsDao: CourseSettingsDao,
    private val checkpointDao: CheckpointDao
) : ModuleListDataSource {

    override suspend fun getAllModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        val moduleObjects = moduleFacade.getModuleObjects(canvasContext.id)
        return DataResult.Success(moduleObjects, apiType = ApiType.DB)
    }

    override suspend fun getFirstPageModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        val moduleObjects = moduleFacade.getModuleObjects(canvasContext.id)
        return DataResult.Success(moduleObjects, apiType = ApiType.DB)
    }

    override suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<Tab>> {
        return DataResult.Success(tabDao.findByCourseId(canvasContext.id).map { it.toApiModel() }, apiType = ApiType.DB)
    }

    override suspend fun getFirstPageModuleItems(
        canvasContext: CanvasContext,
        moduleId: Long,
        forceNetwork: Boolean
    ): DataResult<List<ModuleItem>> {
        val moduleItems = moduleFacade.getModuleItems(moduleId)
        return DataResult.Success(moduleItems, apiType = ApiType.DB)
    }

    override suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return courseSettingsDao.findByCourseId(courseId)?.toApiModel()
    }

    override suspend fun getModuleItemCheckpoints(courseId: String, forceNetwork: Boolean): List<ModuleItemWithCheckpoints> {
        val checkpointEntities = checkpointDao.findByCourseIdWithModuleItem(courseId.toLongOrNull().orDefault())

        return checkpointEntities
            .filter { it.moduleItemId != null }
            .groupBy { it.moduleItemId }
            .map { (moduleItemId, checkpoints) ->
                ModuleItemWithCheckpoints(
                    moduleItemId = moduleItemId.toString(),
                    checkpoints = checkpoints.map { it.toModuleItemCheckpoint() }
                )
            }
    }
}
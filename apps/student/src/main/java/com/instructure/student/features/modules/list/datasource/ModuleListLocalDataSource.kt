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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.facade.ModuleFacade

class ModuleListLocalDataSource(private val tabDao: TabDao, private val moduleFacade: ModuleFacade) : ModuleListDataSource {

    override suspend fun getAllModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        TODO("Not yet implemented")
    }

    override suspend fun getFirstPageModuleObjects(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNextPageModuleObjects(nextUrl: String, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<Tab>> {
        TODO("Not yet implemented")
    }

    override suspend fun getFirstPageModuleItems(canvasContext: CanvasContext, moduleId: Long, forceNetwork: Boolean): DataResult<List<ModuleItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNextPageModuleItems(nextUrl: String, forceNetwork: Boolean): DataResult<List<ModuleItem>> {
        TODO("Not yet implemented")
    }
}
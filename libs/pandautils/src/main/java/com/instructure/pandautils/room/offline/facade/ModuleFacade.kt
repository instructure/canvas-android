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
package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.daos.ModuleItemDao
import com.instructure.pandautils.room.offline.daos.ModuleObjectDao
import com.instructure.pandautils.room.offline.entities.ModuleItemEntity
import com.instructure.pandautils.room.offline.entities.ModuleObjectEntity

class ModuleFacade(private val moduleObjectDao: ModuleObjectDao, private val moduleItemDao: ModuleItemDao) {

    suspend fun insertModules(moduleObjects: List<ModuleObject>, courseId: Long) {
        moduleObjects.forEach { moduleObject ->
            moduleObjectDao.insert(ModuleObjectEntity(moduleObject, courseId))
            moduleObject.items
                .map { moduleItem -> ModuleItemEntity(moduleItem, moduleObject.id) }
                .let { entities -> moduleItemDao.insertAll(entities) }
        }
    }

    private suspend fun createModuleObjectApiModel(moduleObjectEntity: ModuleObjectEntity): ModuleObject {
        val moduleItems = moduleItemDao.findByModuleId(moduleObjectEntity.id).map { it.toApiModel() }
        return moduleObjectEntity.toApiModel(moduleItems)
    }
}
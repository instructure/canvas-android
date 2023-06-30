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

import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.daos.ModuleCompletionRequirementDao
import com.instructure.pandautils.room.offline.daos.ModuleContentDetailsDao
import com.instructure.pandautils.room.offline.daos.ModuleItemDao
import com.instructure.pandautils.room.offline.daos.ModuleObjectDao
import com.instructure.pandautils.room.offline.entities.ModuleCompletionRequirementEntity
import com.instructure.pandautils.room.offline.entities.ModuleContentDetailsEntity
import com.instructure.pandautils.room.offline.entities.ModuleItemEntity
import com.instructure.pandautils.room.offline.entities.ModuleObjectEntity

class ModuleFacade(
    private val moduleObjectDao: ModuleObjectDao,
    private val moduleItemDao: ModuleItemDao,
    private val completionRequirementDao: ModuleCompletionRequirementDao,
    private val moduleContentDetailsDao: ModuleContentDetailsDao,
    private val lockInfoFacade: LockInfoFacade) {

    suspend fun insertModules(moduleObjects: List<ModuleObject>, courseId: Long) {
        moduleObjects.forEach { moduleObject ->
            moduleObjectDao.insert(ModuleObjectEntity(moduleObject, courseId))
            moduleObject.items
                .forEach { moduleItem ->
                    val modultItemEntity = ModuleItemEntity(moduleItem, moduleObject.id)
                    moduleItemDao.insert(modultItemEntity)
                    moduleItem.completionRequirement?.let {
                        completionRequirementDao.insert(ModuleCompletionRequirementEntity(it, modultItemEntity.id))
                    }
                    moduleItem.moduleDetails?.let { moduleDetails ->
                        moduleContentDetailsDao.insert(ModuleContentDetailsEntity(moduleDetails, modultItemEntity.id))
                        moduleDetails.lockInfo?.let { lockInfo ->
                            lockInfoFacade.insertLockInfoForModule(lockInfo, modultItemEntity.id)
                        }
                    }
                }
        }
    }

    suspend fun getModuleObjects(courseId: Long): List<ModuleObject> {
        val moduleObjects = moduleObjectDao.findByCourseId(courseId)
        return moduleObjects.map { moduleObjectEntity -> createModuleObjectApiModel(moduleObjectEntity) }
    }

    private suspend fun createModuleObjectApiModel(moduleObjectEntity: ModuleObjectEntity): ModuleObject {
        val moduleItems = moduleItemDao.findByModuleId(moduleObjectEntity.id).map { createModuleItemApiModel(it) }
        return moduleObjectEntity.toApiModel(moduleItems)
    }

    suspend fun getModuleItems(moduleId: Long): List<ModuleItem> {
        val moduleItemEntities = moduleItemDao.findByModuleId(moduleId)
        return moduleItemEntities.map { moduleItemEntity -> createModuleItemApiModel(moduleItemEntity) }
    }

    private suspend fun createModuleItemApiModel(moduleItemEntity: ModuleItemEntity): ModuleItem {
        val completionRequirement = completionRequirementDao.findByModuleId(moduleItemEntity.id).firstOrNull()?.toApiModel()
        val lockInfo = lockInfoFacade.getLockInfoByModuleId(moduleItemEntity.id)
        val moduleContentDetails = moduleContentDetailsDao.findByModuleId(moduleItemEntity.id)?.toApiModel(lockInfo)
        return moduleItemEntity.toApiModel(completionRequirement, moduleContentDetails)
    }
}
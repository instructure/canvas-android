/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.LockInfo
import com.instructure.pandautils.room.offline.daos.LockInfoDao
import com.instructure.pandautils.room.offline.daos.LockedModuleDao
import com.instructure.pandautils.room.offline.daos.ModuleCompletionRequirementDao
import com.instructure.pandautils.room.offline.daos.ModuleNameDao
import com.instructure.pandautils.room.offline.entities.LockInfoEntity
import com.instructure.pandautils.room.offline.entities.LockedModuleEntity
import com.instructure.pandautils.room.offline.entities.ModuleCompletionRequirementEntity
import com.instructure.pandautils.room.offline.entities.ModuleNameEntity

class LockInfoFacade(
    private val lockInfoDao: LockInfoDao,
    private val lockedModuleDao: LockedModuleDao,
    private val moduleNameDao: ModuleNameDao,
    private val completionRequirementDao: ModuleCompletionRequirementDao
) {

    suspend fun insertLockInfoForAssignment(lockInfo: LockInfo, assignmentId: Long) {
        insertLockInfo(lockInfo, assignmentId = assignmentId)
    }

    suspend fun insertLockInfoForModule(lockInfo: LockInfo, moduleId: Long) {
        insertLockInfo(lockInfo, moduleId = moduleId)
    }

    suspend fun insertLockInfoForPage(lockInfo: LockInfo, pageId: Long) {
        insertLockInfo(lockInfo, pageId = pageId)
    }

    private suspend fun insertLockInfo(lockInfo: LockInfo, assignmentId: Long? = null, moduleId: Long? = null, pageId: Long? = null) {
        val rowId = lockInfoDao.insert(LockInfoEntity(lockInfo, assignmentId, moduleId, pageId))
        val lockInfoEntity = lockInfoDao.findByRowId(rowId)
        lockInfo.contextModule?.let { lockedModule ->
            lockedModuleDao.insert(LockedModuleEntity(lockedModule, lockInfoEntity?.id))
            moduleNameDao.insertAll(lockedModule.prerequisites?.map { ModuleNameEntity(it, lockedModule.id) }.orEmpty())
            lockedModule.completionRequirements.forEach {
                val oldEntity = completionRequirementDao.findById(it.id)
                if (oldEntity != null) {
                    val newEntity = oldEntity.copy(minScore = it.minScore, maxScore = it.maxScore, moduleId = lockedModule.id)
                    completionRequirementDao.insert(newEntity)
                } else {
                    completionRequirementDao.insert(ModuleCompletionRequirementEntity(it, lockedModule.id, lockedModule.contextId))
                }
            }
        }
    }

    suspend fun getLockInfoByAssignmentId(assignmentId: Long): LockInfo? {
        val lockInfoEntity = lockInfoDao.findByAssignmentId(assignmentId)
        return createFullLockInfoApiModel(lockInfoEntity)
    }

    suspend fun getLockInfoByModuleId(moduleId: Long): LockInfo? {
        val lockInfoEntity = lockInfoDao.findByModuleId(moduleId)
        return createFullLockInfoApiModel(lockInfoEntity)
    }

    suspend fun getLockInfoByPageId(pageId: Long): LockInfo? {
        val lockInfoEntity = lockInfoDao.findByPageId(pageId)
        return createFullLockInfoApiModel(lockInfoEntity)
    }

    private suspend fun createFullLockInfoApiModel(lockInfoEntity: LockInfoEntity?): LockInfo? {
        val lockedModuleEntity = lockInfoEntity?.lockedModuleId?.let { lockedModuleDao.findById(it) }
        val moduleNameEntities = lockedModuleEntity?.id?.let { moduleNameDao.findByLockModuleId(it) }
        val completionRequirementEntities = lockedModuleEntity?.id?.let { completionRequirementDao.findByModuleId(it) }

        return lockInfoEntity?.toApiModel(
            lockedModule = lockedModuleEntity?.toApiModel(
                prerequisites = moduleNameEntities?.map { it.toApiModel() },
                completionRequirements = completionRequirementEntities?.map { it.toApiModel() }.orEmpty()
            )
        )
    }
}
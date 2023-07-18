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
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*

class LockInfoFacade(
    private val lockInfoDao: LockInfoDao,
    private val lockedModuleDao: LockedModuleDao,
    private val moduleNameDao: ModuleNameDao,
    private val completionRequirementDao: ModuleCompletionRequirementDao,
    private val lockInfoLockedModuleDao: LockInfoLockedModuleDao
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
        val lockInfoId = lockInfoDao.insert(LockInfoEntity(lockInfo, assignmentId, moduleId, pageId))
        lockInfo.contextModule?.let { lockedModule ->
            lockedModuleDao.insert(LockedModuleEntity(lockedModule))
            lockInfoLockedModuleDao.insert(LockInfoLockedModuleEntity(lockInfoId, lockedModule.id))
            moduleNameDao.insertAll(lockedModule.prerequisites?.map { ModuleNameEntity(it, lockedModule.id) }.orEmpty())
            completionRequirementDao.insertAll(lockedModule.completionRequirements.map {
                ModuleCompletionRequirementEntity(it, lockedModule.id)
            })
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
        val lockedInfoLockedModuleEntity = lockInfoEntity?.id?.let { lockInfoLockedModuleDao.findByLockInfoId(it) }
        val lockedModuleEntity = lockedInfoLockedModuleEntity?.lockedModuleId?.let { lockedModuleDao.findById(it) }
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
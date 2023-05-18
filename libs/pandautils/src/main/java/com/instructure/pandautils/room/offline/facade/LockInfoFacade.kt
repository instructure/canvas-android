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

    suspend fun insertLockInfo(lockInfo: LockInfo, assignmentId: Long) {
        val lockInfoId = lockInfoDao.insert(LockInfoEntity(lockInfo, assignmentId))
        lockInfo.contextModule?.let { lockedModule ->
            lockedModuleDao.insert(LockedModuleEntity(lockedModule, lockInfoId))
            moduleNameDao.insertAll(lockedModule.prerequisites?.map { ModuleNameEntity(it, lockedModule.id) }.orEmpty())
            completionRequirementDao.insertAll(lockedModule.completionRequirements.map {
                ModuleCompletionRequirementEntity(it, lockedModule.id)
            })
        }
    }

    suspend fun getLockInfoByAssignmentId(assignmentId: Long): LockInfo? {
        val lockInfoEntity = lockInfoDao.findByAssignmentId(assignmentId)
        val lockedModuleEntity = lockInfoEntity?.id?.let { lockedModuleDao.findByLockInfoId(it) }
        val moduleNameEntities = lockedModuleEntity?.id?.let { moduleNameDao.findByLockModuleId(it) }
        val completionRequirementEntities = lockedModuleEntity?.id?.let { completionRequirementDao.findByLockModuleId(it) }

        return lockInfoEntity?.toApiModel(
            lockedModule = lockedModuleEntity?.toApiModel(
                prerequisites = moduleNameEntities?.map { it.toApiModel() },
                completionRequirements = completionRequirementEntities?.map { it.toApiModel() }.orEmpty()
            )
        )
    }
}
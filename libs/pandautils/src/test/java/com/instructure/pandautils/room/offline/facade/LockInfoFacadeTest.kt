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
import com.instructure.canvasapi2.models.LockedModule
import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleName
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.room.offline.daos.LockInfoDao
import com.instructure.pandautils.room.offline.daos.LockedModuleDao
import com.instructure.pandautils.room.offline.daos.ModuleCompletionRequirementDao
import com.instructure.pandautils.room.offline.daos.ModuleNameDao
import com.instructure.pandautils.room.offline.entities.LockInfoEntity
import com.instructure.pandautils.room.offline.entities.LockedModuleEntity
import com.instructure.pandautils.room.offline.entities.ModuleCompletionRequirementEntity
import com.instructure.pandautils.room.offline.entities.ModuleNameEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.util.Date

class LockInfoFacadeTest {

    private val lockInfoDao: LockInfoDao = mockk(relaxed = true)
    private val lockedModuleDao: LockedModuleDao = mockk(relaxed = true)
    private val moduleNameDao: ModuleNameDao = mockk(relaxed = true)
    private val completionRequirementDao: ModuleCompletionRequirementDao = mockk(relaxed = true)

    private val facade = LockInfoFacade(lockInfoDao, lockedModuleDao, moduleNameDao, completionRequirementDao)

    @Test
    fun `Calling insertLockInfoForAssignment should insert lock info and related entities`() = runTest {
        val assignmentId = 1L
        val prerequisites = listOf(ModuleName("Module name 1"))
        val completionRequirements = listOf(ModuleCompletionRequirement(id = 1L))
        val lockedModule = LockedModule(id = 1L, prerequisites = prerequisites, completionRequirements = completionRequirements, contextId = 1)
        val lockInfo = LockInfo(modulePrerequisiteNames = arrayListOf("1", "2"), contextModule = lockedModule, unlockAt = Date().toApiString())

        coEvery { completionRequirementDao.findById(any()) } returns null

        val lockInfoEntity = LockInfoEntity(lockInfo, assignmentId)
        coEvery { lockInfoDao.findByRowId(any()) } returns lockInfoEntity

        facade.insertLockInfoForAssignment(lockInfo, assignmentId)

        coVerify { lockInfoDao.insert(LockInfoEntity(lockInfo, assignmentId)) }
        coVerify { lockedModuleDao.insert(LockedModuleEntity(lockedModule, lockInfoEntity.id)) }
        coVerify { moduleNameDao.insertAll(prerequisites.map { ModuleNameEntity(it, 1L) }) }
        coVerify { completionRequirementDao.insert(ModuleCompletionRequirementEntity(completionRequirements.first(), 1L, 1L)) }
    }

    @Test
    fun `Calling insertLockInfoForModule should insert lock info and related entities`() = runTest {
        val moduleId = 1L
        val prerequisites = listOf(ModuleName("Module name 1"))
        val completionRequirements = listOf(ModuleCompletionRequirement(id = 1L))
        val lockedModule = LockedModule(id = 1L, prerequisites = prerequisites, completionRequirements = completionRequirements, contextId = 1)
        val lockInfo = LockInfo(modulePrerequisiteNames = arrayListOf("1", "2"), contextModule = lockedModule, unlockAt = Date().toApiString())

        coEvery { completionRequirementDao.findById(any()) } returns null

        val lockInfoEntity = LockInfoEntity(lockInfo, moduleId = moduleId)
        coEvery { lockInfoDao.findByRowId(any()) } returns lockInfoEntity

        facade.insertLockInfoForModule(lockInfo, moduleId)

        coVerify { lockInfoDao.insert(lockInfoEntity) }
        coVerify { lockedModuleDao.insert(LockedModuleEntity(lockedModule, lockInfoEntity.id)) }
        coVerify { moduleNameDao.insertAll(prerequisites.map { ModuleNameEntity(it, 1L) }) }
        coVerify { completionRequirementDao.insert(ModuleCompletionRequirementEntity(completionRequirements.first(), 1L, 1L)) }
    }

    @Test
    fun `Calling getLockInfoByAssignmentId should return the lock info with the specified assignment ID`() = runTest {
        val assignmentId = 1L
        val prerequisites = listOf(ModuleName("Module name 1"))
        val completionRequirements = listOf(ModuleCompletionRequirement(id = 1L))
        val lockedModule = LockedModule(id = 1L, prerequisites = prerequisites, completionRequirements = completionRequirements, contextId = 1)
        val lockInfo = LockInfo(modulePrerequisiteNames = arrayListOf("1", "2"), contextModule = lockedModule, unlockAt = Date().toApiString())

        val lockInfoEntity = LockInfoEntity(lockInfo, assignmentId)

        coEvery { lockInfoDao.findByAssignmentId(assignmentId) } returns lockInfoEntity
        coEvery { lockedModuleDao.findById(any()) } returns LockedModuleEntity(lockedModule, lockInfoEntity.id)
        coEvery { moduleNameDao.findByLockModuleId(any()) } returns prerequisites.map { ModuleNameEntity(it, 1L) }
        coEvery { completionRequirementDao.findByModuleId(any()) } returns completionRequirements.map {
            ModuleCompletionRequirementEntity(it, 1L, 1L)
        }

        val result = facade.getLockInfoByAssignmentId(assignmentId)

        Assert.assertEquals(lockedModule, result?.contextModule)
        Assert.assertEquals(lockInfo, result)
    }

    @Test
    fun `Calling getLockInfoByModuleId should return the lock info with the specified module ID`() = runTest {
        val moduleId = 1L
        val prerequisites = listOf(ModuleName("Module name 1"))
        val completionRequirements = listOf(ModuleCompletionRequirement(id = 1L))
        val lockedModule = LockedModule(id = 1L, prerequisites = prerequisites, completionRequirements = completionRequirements, contextId = 1)
        val lockInfo = LockInfo(modulePrerequisiteNames = arrayListOf("1", "2"), contextModule = lockedModule, unlockAt = Date().toApiString())

        val lockInfoEntity = LockInfoEntity(lockInfo, moduleId = moduleId)

        coEvery { lockInfoDao.findByModuleId(moduleId) } returns lockInfoEntity
        coEvery { lockedModuleDao.findById(any()) } returns LockedModuleEntity(lockedModule, lockInfoEntity.id)
        coEvery { moduleNameDao.findByLockModuleId(any()) } returns prerequisites.map { ModuleNameEntity(it, 1L) }
        coEvery { completionRequirementDao.findByModuleId(any()) } returns completionRequirements.map {
            ModuleCompletionRequirementEntity(it, 1L, 1L)
        }

        val result = facade.getLockInfoByModuleId(moduleId)

        Assert.assertEquals(lockedModule, result?.contextModule)
        Assert.assertEquals(lockInfo, result)
    }
}
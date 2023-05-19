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

import com.instructure.pandautils.room.offline.daos.LockInfoDao
import com.instructure.pandautils.room.offline.daos.LockedModuleDao
import com.instructure.pandautils.room.offline.daos.ModuleCompletionRequirementDao
import com.instructure.pandautils.room.offline.daos.ModuleNameDao
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class LockInfoFacadeTest {

    private val lockInfoDao: LockInfoDao = mockk(relaxed = true)
    private val lockedModuleDao: LockedModuleDao = mockk(relaxed = true)
    private val moduleNameDao: ModuleNameDao = mockk(relaxed = true)
    private val completionRequirementDao: ModuleCompletionRequirementDao = mockk(relaxed = true)

    private val facade = LockInfoFacade(lockInfoDao, lockedModuleDao, moduleNameDao, completionRequirementDao)

    @Test
    fun `Calling insertLockInfo should insert lock info and related entities`() = runTest {

    }

    @Test
    fun `Calling getLockInfoByAssignmentId should return the lock info with the specified assignment ID`() = runTest {

    }
}
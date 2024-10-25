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

import androidx.lifecycle.LiveData
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.room.offline.daos.SyncSettingsDao
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class SyncSettingsFacadeTest {

    private val syncSettingsDao: SyncSettingsDao = mockk(relaxed = true)

    private val facade = SyncSettingsFacade(syncSettingsDao)

    @Test
    fun `getSyncSettingsListenable should create default settings if value is null`() = runTest {
        val liveData = mockk<LiveData<SyncSettingsEntity?>> { every { value } returns null }

        coEvery { syncSettingsDao.findSyncSettings() } returns null
        every { syncSettingsDao.findSyncSettingsLiveData() } returns liveData
        coEvery { syncSettingsDao.insert(any()) } just Runs

        facade.getSyncSettingsListenable()

        coVerify { syncSettingsDao.insert(any()) }
    }

    @Test
    fun `getSyncSettingsListenable should not create default settings if value is not null`() = runTest {
        val liveData = mockk<LiveData<SyncSettingsEntity?>> { every { value } returns mockk() }

        every { syncSettingsDao.findSyncSettingsLiveData() } returns liveData
        coEvery { syncSettingsDao.insert(any()) } just Runs

        facade.getSyncSettingsListenable()

        coVerify(exactly = 0) { syncSettingsDao.insert(any()) }
    }

    @Test
    fun `getSyncSettings should return default settings if null`() = runTest {
        coEvery { syncSettingsDao.findSyncSettings() } returns null
        coEvery { syncSettingsDao.insert(any()) } just Runs

        val result = facade.getSyncSettings()

        Assert.assertEquals(true, result.autoSyncEnabled)
        Assert.assertEquals(SyncFrequency.DAILY, result.syncFrequency)
        Assert.assertEquals(true, result.wifiOnly)

        coVerify { syncSettingsDao.insert(any()) }
    }

    @Test
    fun `getSyncSettings should return existing settings if not null`() = runTest {
        val settings = SyncSettingsEntity(id = 1L, autoSyncEnabled = false, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = false)

        coEvery { syncSettingsDao.findSyncSettings() } returns settings
        coEvery { syncSettingsDao.insert(any()) } just Runs

        val result = facade.getSyncSettings()

        Assert.assertEquals(settings, result)
        coVerify(exactly = 0) { syncSettingsDao.insert(any()) }
    }

    @Test
    fun `update should call syncSettingsDao update`() = runTest {
        val settings = mockk<SyncSettingsEntity>()

        facade.update(settings)

        coVerify { syncSettingsDao.update(settings) }
    }
}
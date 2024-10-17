/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.RemoteFileEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteFileDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var remoteFileDao: RemoteFileDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        remoteFileDao = db.remoteFileDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val remoteFileEntity = RemoteFileEntity(RemoteFile(1L))
        val updated = remoteFileEntity.copy(displayName = "new name")
        remoteFileDao.insert(remoteFileEntity)
        remoteFileDao.insert(updated)
        val result = remoteFileDao.findById(1)
        assertEquals(updated, result)
    }

    @Test
    fun testInsertAllReplace() = runTest {
        val remoteFileEntity1 = RemoteFileEntity(RemoteFile(1L))
        val remoteFileEntity2 = RemoteFileEntity(RemoteFile(2L))
        val updated = remoteFileEntity1.copy(displayName = "new name")
        remoteFileDao.insertAll(listOf(remoteFileEntity1, remoteFileEntity2))
        remoteFileDao.insertAll(listOf(updated))
        val result1 = remoteFileDao.findById(1)
        val result2 = remoteFileDao.findById(2)
        assertEquals(updated, result1)
        assertEquals(remoteFileEntity2, result2)
    }

    @Test
    fun testFindById() = runTest {
        val remoteFileEntity = RemoteFileEntity(RemoteFile(1L))
        val remoteFileEntity2 = RemoteFileEntity(RemoteFile(2L))
        remoteFileDao.insertAll(listOf(remoteFileEntity, remoteFileEntity2))
        val result = remoteFileDao.findById(1)
        assertEquals(remoteFileEntity, result)
    }

}
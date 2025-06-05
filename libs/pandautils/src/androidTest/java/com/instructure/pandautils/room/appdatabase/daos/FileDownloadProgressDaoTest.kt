/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.room.appdatabase.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileDownloadProgressDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var fileDownloadProgressDao: FileDownloadProgressDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        fileDownloadProgressDao = db.fileDownloadProgressDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun findCorrectEntityByWorkerId() = runTest {
        fileDownloadProgressDao.insert(FileDownloadProgressEntity(workerId = "2", fileName = "2", progress = 50, progressState = FileDownloadProgressState.IN_PROGRESS, filePath = "path2"))
        fileDownloadProgressDao.insert(FileDownloadProgressEntity(workerId = "1", fileName = "1", progress = 100, progressState = FileDownloadProgressState.COMPLETED, filePath = "path1"))
        fileDownloadProgressDao.insert(FileDownloadProgressEntity(workerId = "3", fileName = "3", progress = 75, progressState = FileDownloadProgressState.IN_PROGRESS, filePath = "path3"))

        val result = fileDownloadProgressDao.findByWorkerId("1")

        assertEquals(100, result!!.progress)
    }

    @Test
    fun deleteByWorkerId() = runTest {
        fileDownloadProgressDao.insert(FileDownloadProgressEntity(workerId = "1", fileName = "1", progress = 100, progressState = FileDownloadProgressState.COMPLETED, filePath = "path1"))
        fileDownloadProgressDao.insert(FileDownloadProgressEntity(workerId = "2", fileName = "2", progress = 50, progressState = FileDownloadProgressState.IN_PROGRESS, filePath = "path2"))

        fileDownloadProgressDao.deleteByWorkerId("1")

        val result = fileDownloadProgressDao.findByWorkerId("1")
        assertNull(result)
    }
}
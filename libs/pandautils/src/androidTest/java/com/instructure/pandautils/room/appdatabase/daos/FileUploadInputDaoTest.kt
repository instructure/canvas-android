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
package com.instructure.pandautils.room.appdatabase.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileUploadInputDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var fileUploadInputDao: FileUploadInputDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        fileUploadInputDao = db.fileUploadInputDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun findCorrectEntityByWorkerId() = runTest {
        fileUploadInputDao.insert(FileUploadInputEntity(workerId = "2", action = "upload 2", filePaths = emptyList()))
        fileUploadInputDao.insert(FileUploadInputEntity(workerId = "1", action = "upload", filePaths = emptyList()))
        fileUploadInputDao.insert(FileUploadInputEntity(workerId = "3", action = "upload 3", filePaths = emptyList()))

        val result = fileUploadInputDao.findByWorkerId("1")

        Assert.assertEquals("upload", result!!.action)
    }
}
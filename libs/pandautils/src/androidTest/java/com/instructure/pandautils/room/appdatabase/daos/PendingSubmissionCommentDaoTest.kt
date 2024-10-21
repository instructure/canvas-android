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
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingSubmissionCommentDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var pendingSubmissionCommentDao: PendingSubmissionCommentDao

    private lateinit var fileUploadInputDao: FileUploadInputDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        pendingSubmissionCommentDao = db.pendingSubmissionCommentDao()
        fileUploadInputDao = db.fileUploadInputDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindCorrectItemByWorkedId() = runTest {
        val itemToFind = PendingSubmissionCommentEntity(2, pageId = "12", workerId = "222")
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(1, pageId = "11", workerId = "111"))
        pendingSubmissionCommentDao.insert(itemToFind)
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(3, pageId = "12", workerId = "333"))

        val result = pendingSubmissionCommentDao.findByWorkerId("222")

        Assert.assertEquals(itemToFind, result)
    }

    @Test
    fun testFindCorrectItemById() = runTest {
        val itemToFind = PendingSubmissionCommentEntity(2, pageId = "12", workerId = "222")
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(1, pageId = "11", workerId = "111"))
        pendingSubmissionCommentDao.insert(itemToFind)
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(3, pageId = "12", workerId = "333"))

        val result = pendingSubmissionCommentDao.findById(2)

        Assert.assertEquals(itemToFind, result)
    }

    @Test
    fun testFindCorrectItemByWorkedIdWithInputData() = runTest {
        val itemToFind = PendingSubmissionCommentEntity(2, pageId = "12", workerId = "222")
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(1, pageId = "11", workerId = "111"))
        pendingSubmissionCommentDao.insert(itemToFind)
        fileUploadInputDao.insert(FileUploadInputEntity(workerId = "222", action = "File Upload", filePaths = emptyList()))

        val result = pendingSubmissionCommentDao.findByWorkerIdWithInputData("222")

        Assert.assertEquals(itemToFind, result!!.pendingSubmissionCommentEntity)
        Assert.assertEquals("File Upload", result.fileUploadInput!!.action)
    }

    @Test
    fun testFindCorrectItemByPageIdWithInputData() = runTest {
        val itemToFind = PendingSubmissionCommentEntity(2, pageId = "12", workerId = "222")
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(1, pageId = "11", workerId = "111"))
        pendingSubmissionCommentDao.insert(itemToFind)
        fileUploadInputDao.insert(FileUploadInputEntity(workerId = "222", action = "File Upload", filePaths = emptyList()))

        val result = pendingSubmissionCommentDao.findByPageId("12")

        Assert.assertEquals(1, result!!.size)
        Assert.assertEquals(itemToFind, result.first().pendingSubmissionCommentEntity)
        Assert.assertEquals("File Upload", result.first().fileUploadInput!!.action)
    }

    @Test
    fun testFindByStatusOnlyReturnsTheItemsWithCorrectStatusAndNonNullWorkerId() = runTest {
        val itemToFind = PendingSubmissionCommentEntity(2, status = "progress", pageId = "12", workerId = "222")
        val itemToFind2 = PendingSubmissionCommentEntity(4, status = "progress", pageId = "15", workerId = "333")
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(1, status = "finished", pageId = "11", workerId = "111"))
        pendingSubmissionCommentDao.insert(itemToFind)
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(3, status = "progress", pageId = "19"))
        pendingSubmissionCommentDao.insert(itemToFind2)
        fileUploadInputDao.insert(FileUploadInputEntity(workerId = "222", action = "File Upload", filePaths = emptyList()))
        fileUploadInputDao.insert(FileUploadInputEntity(workerId = "333", action = "File Upload 2", filePaths = emptyList()))

        val result = pendingSubmissionCommentDao.findByStatus("progress")

        Assert.assertEquals(2, result!!.size)
        Assert.assertEquals(itemToFind, result.first().pendingSubmissionCommentEntity)
        Assert.assertEquals(itemToFind2, result.get(1).pendingSubmissionCommentEntity)
        Assert.assertEquals("File Upload", result.first().fileUploadInput!!.action)
        Assert.assertEquals("File Upload 2", result.get(1).fileUploadInput!!.action)
    }
}
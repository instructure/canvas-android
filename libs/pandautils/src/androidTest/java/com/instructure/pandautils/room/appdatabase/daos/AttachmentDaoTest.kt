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
import com.instructure.pandautils.room.appdatabase.entities.AttachmentEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class AttachmentDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var attachmentDao: AttachmentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        attachmentDao = db.attachmentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndFindingByParentId() = runTest {
        val attachmentEntity = AttachmentEntity(
            id = 1, contentType = "image/jpg", filename = "image.jpg", displayName = "File",
            url = "file.com", createdAt = Date(), size = 10000, workerId = "123", submissionCommentId = 123
        )

        val attachmentEntity2 = attachmentEntity.copy(id = 2, workerId = "124", filename = "image2.jpg")

        attachmentDao.insertAll(listOf(attachmentEntity, attachmentEntity2))
        val result = attachmentDao.findByParentId("123")
        Assert.assertEquals(1, result!!.size)
        Assert.assertEquals(attachmentEntity, result.first())
    }

    @Test
    fun dontReturnAnyItemIfEntitiesAreDeleted() = runTest {
        val attachmentEntity = AttachmentEntity(
            id = 1, contentType = "image/jpg", filename = "image.jpg", displayName = "File",
            url = "file.com", createdAt = Date(), size = 10000, workerId = "123", submissionCommentId = 123
        )

        val attachmentEntity2 = attachmentEntity.copy(id = 2, workerId = "124", filename = "image2.jpg")

        attachmentDao.insertAll(listOf(attachmentEntity, attachmentEntity2))
        attachmentDao.deleteAll(listOf(attachmentEntity, attachmentEntity2))
        val result = attachmentDao.findByParentId("123")

        Assert.assertEquals(0, result!!.size)
    }
}
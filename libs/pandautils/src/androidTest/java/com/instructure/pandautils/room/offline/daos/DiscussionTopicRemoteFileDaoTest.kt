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
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicPermissionEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicRemoteFileEntity
import com.instructure.pandautils.room.offline.entities.RemoteFileEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscussionTopicRemoteFileDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var remoteFileDao: RemoteFileDao
    private lateinit var discussionTopicHeaderDao: DiscussionTopicHeaderDao
    private lateinit var discussionTopicRemoteFileDao: DiscussionTopicRemoteFileDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        remoteFileDao = db.remoteFileDao()
        discussionTopicRemoteFileDao = db.discussionTopicRemoteFileDao()
        discussionTopicHeaderDao = db.discussionTopicHeaderDao()

        db.courseDao().insert(CourseEntity(Course(1L)))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindByDiscussionId() = runTest {
        val remoteFileEntity = RemoteFileEntity(RemoteFile(1L))
        val discussionTopicEntity = DiscussionTopicHeaderEntity(DiscussionTopicHeader(2L), 1L)
        discussionTopicHeaderDao.insert(discussionTopicEntity)
        remoteFileDao.insert(remoteFileEntity)

        val discussionTopicRemoteFileEntity = DiscussionTopicRemoteFileEntity(discussionTopicEntity.id, remoteFileEntity.id)
        discussionTopicRemoteFileDao.insert(discussionTopicRemoteFileEntity)
        val result = discussionTopicRemoteFileDao.findByDiscussionId(discussionTopicEntity.id)
        assertEquals(listOf(discussionTopicRemoteFileEntity), result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertDiscussionForeignKey() = runTest {
        val remoteFileEntity = RemoteFileEntity(RemoteFile(1L))
        remoteFileDao.insert(remoteFileEntity)

        val discussionTopicRemoteFileEntity = DiscussionTopicRemoteFileEntity(1L, remoteFileEntity.id)
        discussionTopicRemoteFileDao.insert(discussionTopicRemoteFileEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertRemoteFileForeignKey() = runTest {
        val discussionTopicEntity = DiscussionTopicHeaderEntity(DiscussionTopicHeader(1L), 1L)
        discussionTopicHeaderDao.insert(discussionTopicEntity)

        val discussionTopicRemoteFileEntity = DiscussionTopicRemoteFileEntity(discussionTopicEntity.id, 1L)
        discussionTopicRemoteFileDao.insert(discussionTopicRemoteFileEntity)
    }

    @Test
    fun testRemoteFileCascade() = runTest {
        val remoteFileEntity = RemoteFileEntity(RemoteFile(1L))
        remoteFileDao.insert(remoteFileEntity)

        val discussionTopicEntity = DiscussionTopicHeaderEntity(DiscussionTopicHeader(2L), 1L)
        discussionTopicHeaderDao.insert(discussionTopicEntity)

        val discussionTopicRemoteFileEntity = DiscussionTopicRemoteFileEntity(discussionTopicEntity.id, remoteFileEntity.id)
        discussionTopicRemoteFileDao.insert(discussionTopicRemoteFileEntity)

        remoteFileDao.delete(remoteFileEntity)
        val result = discussionTopicRemoteFileDao.findByDiscussionId(discussionTopicEntity.id)
        assertEquals(emptyList<DiscussionTopicRemoteFileEntity>(), result)
    }

    @Test
    fun testDiscussionTopicCascade() = runTest {
        val remoteFileEntity = RemoteFileEntity(RemoteFile(1L))
        remoteFileDao.insert(remoteFileEntity)

        val discussionTopicEntity = DiscussionTopicHeaderEntity(DiscussionTopicHeader(2L), 1L)
        discussionTopicHeaderDao.insert(discussionTopicEntity)

        val discussionTopicRemoteFileEntity = DiscussionTopicRemoteFileEntity(discussionTopicEntity.id, remoteFileEntity.id)
        discussionTopicRemoteFileDao.insert(discussionTopicRemoteFileEntity)

        discussionTopicHeaderDao.delete(discussionTopicEntity)
        val result = discussionTopicRemoteFileDao.findByDiscussionId(discussionTopicEntity.id)
        assertEquals(emptyList<DiscussionTopicRemoteFileEntity>(), result)
    }

}
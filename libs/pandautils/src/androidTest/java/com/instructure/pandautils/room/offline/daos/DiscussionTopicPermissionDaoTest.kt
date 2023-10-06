package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.DiscussionTopicPermissionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DiscussionTopicPermissionDaoTest {
    private lateinit var db: OfflineDatabase

    private lateinit var discussionTopicPermissionDao: DiscussionTopicPermissionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        discussionTopicPermissionDao = db.discussionTopicPermissionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val discussionTopicPermissionEntity = DiscussionTopicPermissionEntity(DiscussionTopicPermission(true), 1L)
        discussionTopicPermissionDao.upsert(discussionTopicPermissionEntity)

        val updated = discussionTopicPermissionEntity.copy(attach = false)
        discussionTopicPermissionDao.upsert(updated)

        val result = discussionTopicPermissionDao.findById(1L)

        Assert.assertEquals(updated.attach, result?.attach)
    }

    @Test
    fun testInsertAllReplace() = runTest {
        val discussionEntryEntities = listOf(
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true, true), 1L),
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true), 2L)
        )
        discussionTopicPermissionDao.upsertAll(discussionEntryEntities)

        val updated = discussionEntryEntities.map { it.copy(attach = false) }
        discussionTopicPermissionDao.upsertAll(updated)

        val result0 = discussionTopicPermissionDao.findById(1L)
        val result1 = discussionTopicPermissionDao.findById(2L)

        Assert.assertEquals(updated[0].attach, result0?.attach)
        Assert.assertEquals(updated[1].attach, result1?.attach)
    }

    fun testFindById() = runTest {
        val discussionEntryEntities = listOf(
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true, true), 1L),
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true), 2L)
        )
        discussionTopicPermissionDao.upsertAll(discussionEntryEntities)

        val result = discussionTopicPermissionDao.findById(1L)

        Assert.assertEquals(discussionEntryEntities[0], result)
    }
}
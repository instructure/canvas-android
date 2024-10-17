package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.DiscussionTopicEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscussionTopicDaoTest {
    private lateinit var db: OfflineDatabase

    private lateinit var discussionTopicDao: DiscussionTopicDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        discussionTopicDao = db.discussionTopicDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val discussionTopicEntity = DiscussionTopicEntity(DiscussionTopic(mutableListOf(1L, 2L)), emptyList(), emptyList(), 1L)
        discussionTopicDao.insert(discussionTopicEntity)

        val updated = discussionTopicEntity.copy(unreadEntries = mutableListOf(3L, 4L))
        discussionTopicDao.insert(updated)

        val result = discussionTopicDao.findById(1L)

        Assert.assertEquals(updated.unreadEntries, result?.unreadEntries)
    }

    @Test
    fun testInsertAllReplace() = runTest {
        val discussionTopicEntities = listOf(
            DiscussionTopicEntity(DiscussionTopic(mutableListOf(1L, 2L)), emptyList(), emptyList(), 1L),
            DiscussionTopicEntity(DiscussionTopic(mutableListOf(1L, 2L)), emptyList(), emptyList(), 2L)
        )
        discussionTopicDao.insertAll(discussionTopicEntities)

        val updated = discussionTopicEntities.map { it.copy(unreadEntries = mutableListOf(3L, 4L)) }
        discussionTopicDao.insertAll(updated)

        val result0 = discussionTopicDao.findById(1L)
        val result1 = discussionTopicDao.findById(2L)

        Assert.assertEquals(updated[0].unreadEntries, result0?.unreadEntries)
        Assert.assertEquals(updated[1].unreadEntries, result1?.unreadEntries)
    }

    fun testFindById() = runTest {
        val discussionTopicEntities = listOf(
            DiscussionTopicEntity(DiscussionTopic(mutableListOf(1L, 2L)), emptyList(), emptyList(), 1L),
            DiscussionTopicEntity(DiscussionTopic(mutableListOf(1L, 2L)), emptyList(), emptyList(), 2L)
        )
        discussionTopicDao.insertAll(discussionTopicEntities)

        val result = discussionTopicDao.findById(1L)

        Assert.assertEquals(discussionTopicEntities[0], result)
    }
}
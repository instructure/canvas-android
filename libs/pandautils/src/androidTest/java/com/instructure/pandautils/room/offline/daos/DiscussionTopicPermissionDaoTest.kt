package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicPermissionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscussionTopicPermissionDaoTest {
    private lateinit var db: OfflineDatabase

    private lateinit var discussionTopicPermissionDao: DiscussionTopicPermissionDao
    private lateinit var discussionTopicHeaderDao: DiscussionTopicHeaderDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        discussionTopicPermissionDao = db.discussionTopicPermissionDao()
        discussionTopicHeaderDao = db.discussionTopicHeaderDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(DiscussionTopicHeader(1L), 1L))

        val discussionTopicPermissionEntity = DiscussionTopicPermissionEntity(DiscussionTopicPermission(true), 1L)
        val id = discussionTopicPermissionDao.insert(discussionTopicPermissionEntity)

        val updated = discussionTopicPermissionEntity.copy(id = id, attach = false)
        discussionTopicPermissionDao.insert(updated)

        val result = discussionTopicPermissionDao.findByDiscussionTopicHeaderId(1L)

        Assert.assertEquals(updated.attach, result?.attach)
    }

    @Test
    fun testInsertAllReplace() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(DiscussionTopicHeader(1L), 1L))
        discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(DiscussionTopicHeader(2L), 1L))

        val discussionEntryEntities = listOf(
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true, true), 1L),
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true), 2L)
        )
        val ids = discussionTopicPermissionDao.insertAll(discussionEntryEntities)

        val updated = discussionEntryEntities.mapIndexed { index, entity -> entity.copy(id = ids[index], attach = false) }
        discussionTopicPermissionDao.insertAll(updated)

        val result0 = discussionTopicPermissionDao.findByDiscussionTopicHeaderId(1L)
        val result1 = discussionTopicPermissionDao.findByDiscussionTopicHeaderId(2L)

        Assert.assertEquals(updated[0].attach, result0?.attach)
        Assert.assertEquals(updated[1].attach, result1?.attach)
    }

    @Test
    fun testFindById() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(DiscussionTopicHeader(1L), 1L))
        discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(DiscussionTopicHeader(2L), 1L))

        val discussionEntryEntities = listOf(
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true, true), 1L),
            DiscussionTopicPermissionEntity(DiscussionTopicPermission(true), 2L)
        )
        val ids = discussionTopicPermissionDao.insertAll(discussionEntryEntities)

        val result = discussionTopicPermissionDao.findByDiscussionTopicHeaderId(1L)

        Assert.assertEquals(discussionEntryEntities[0].copy(id = ids[0]), result)
    }
}
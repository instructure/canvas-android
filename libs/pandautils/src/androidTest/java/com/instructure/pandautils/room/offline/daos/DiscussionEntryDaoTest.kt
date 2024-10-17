package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.DiscussionEntryEntity
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscussionEntryDaoTest {
    private lateinit var db: OfflineDatabase

    private lateinit var discussionEntryDao: DiscussionEntryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        discussionEntryDao = db.discussionEntryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val discussionEntryEntity = DiscussionEntryEntity(DiscussionEntry(id = 1L, message = "Discussion 1"))
        discussionEntryDao.insert(discussionEntryEntity)

        val updated = discussionEntryEntity.copy(message = "updated")
        discussionEntryDao.insert(updated)

        val result = discussionEntryDao.findById(1L)

        Assert.assertEquals(updated.message, result?.message)
    }

    @Test
    fun testInsertAllReplace() = runTest {
        val discussionEntryEntities = listOf(DiscussionEntryEntity(DiscussionEntry(id = 1L, message = "Discussion 1")), DiscussionEntryEntity(DiscussionEntry(id = 2L, message = "Discussion 2")))
        discussionEntryDao.insertAll(discussionEntryEntities)

        val updated = discussionEntryEntities.map { it.copy(message = "updated") }
        discussionEntryDao.insertAll(updated)

        val result0 = discussionEntryDao.findById(1L)
        val result1 = discussionEntryDao.findById(2L)

        Assert.assertEquals(updated[0].message, result0?.message)
        Assert.assertEquals(updated[1].message, result1?.message)
    }

    fun testFindById() = runTest {
        val discussionEntryEntities = listOf(DiscussionEntryEntity(DiscussionEntry(id = 1L, message = "Discussion 1")), DiscussionEntryEntity(DiscussionEntry(id = 2L, message = "Discussion 2")))
        discussionEntryDao.insertAll(discussionEntryEntities)

        val result = discussionEntryDao.findById(1L)

        Assert.assertEquals(discussionEntryEntities[0], result)
    }
}
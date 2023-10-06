package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.DiscussionEntryEntity
import com.instructure.pandautils.room.offline.entities.GroupUserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class GroupUserDaoTest {
    private lateinit var db: OfflineDatabase

    private lateinit var groupUserDao: GroupUserDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        groupUserDao = db.groupUserDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val groupUserEntity = GroupUserEntity(1L, 1L)
        groupUserDao.insert(groupUserEntity)

        val updated = groupUserEntity.copy(groupId = 2L)
        groupUserDao.insert(updated)

        val result = groupUserDao.findByUserId(1L)

        Assert.assertEquals(updated.groupId, result?.get(0))
    }

    @Test
    fun testInsertAllReplace() = runTest {
        val discussionEntryEntities = listOf(
            GroupUserEntity(1L, 1L),
            GroupUserEntity(1L, 2L)
        )
        groupUserDao.insertAll(discussionEntryEntities)

        val updated = discussionEntryEntities.map { it.copy(groupId = 2L) }
        groupUserDao.insertAll(updated)

        val result0 = groupUserDao.findByUserId(1L)
        val result1 = groupUserDao.findByUserId(2L)

        Assert.assertEquals(updated[0].groupId, result0?.get(0))
        Assert.assertEquals(updated[1].groupId, result1?.get(0))
    }

    fun testFindByUserId() = runTest {
        val discussionEntryEntities = listOf(
            GroupUserEntity(1L, 1L),
            GroupUserEntity(2L, 2L)
        )
        groupUserDao.insertAll(discussionEntryEntities)

        val result = groupUserDao.findByUserId(2L)

        Assert.assertEquals(discussionEntryEntities[1].groupId, result?.get(0))
    }
}
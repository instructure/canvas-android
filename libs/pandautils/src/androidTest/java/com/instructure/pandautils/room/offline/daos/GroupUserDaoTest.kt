package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.GroupEntity
import com.instructure.pandautils.room.offline.entities.GroupUserEntity
import com.instructure.pandautils.room.offline.entities.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupUserDaoTest {
    private lateinit var db: OfflineDatabase

    private lateinit var groupUserDao: GroupUserDao
    private lateinit var groupDao: GroupDao
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        groupUserDao = db.groupUserDao()
        groupDao = db.groupDao()
        userDao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        groupDao.insert(GroupEntity(Group(1L)))
        groupDao.insert(GroupEntity(Group(2L)))
        userDao.insert(UserEntity(User(1L)))

        val groupUserEntity = GroupUserEntity(1L, 1L)
        val id = groupUserDao.insert(groupUserEntity)

        val updated = groupUserEntity.copy(id = id, groupId = 2L)
        groupUserDao.insert(updated)

        val result = groupUserDao.findByUserId(1L)

        Assert.assertEquals(updated.groupId, result?.get(0))
    }

    @Test
    fun testInsertAllReplace() = runTest {
        groupDao.insert(GroupEntity(Group(1L)))
        groupDao.insert(GroupEntity(Group(2L)))
        userDao.insert(UserEntity(User(1L)))
        userDao.insert(UserEntity(User(2L)))

        val discussionEntryEntities = listOf(
            GroupUserEntity(1L, 1L),
            GroupUserEntity(1L, 2L)
        )
        val ids = groupUserDao.insertAll(discussionEntryEntities)

        val updated = discussionEntryEntities.mapIndexed { index, entity ->  entity.copy(id = ids[index], groupId = 2L) }
        groupUserDao.insertAll(updated)

        val result0 = groupUserDao.findByUserId(1L)
        val result1 = groupUserDao.findByUserId(2L)

        Assert.assertEquals(updated[0].groupId, result0?.get(0))
        Assert.assertEquals(updated[1].groupId, result1?.get(0))
    }

    @Test
    fun testFindByUserId() = runTest {
        groupDao.insert(GroupEntity(Group(1L)))
        groupDao.insert(GroupEntity(Group(2L)))
        userDao.insert(UserEntity(User(1L)))
        userDao.insert(UserEntity(User(2L)))

        val discussionEntryEntities = listOf(
            GroupUserEntity(1L, 1L),
            GroupUserEntity(2L, 2L)
        )
        groupUserDao.insertAll(discussionEntryEntities)
        
        val result = groupUserDao.findByUserId(2L)

        Assert.assertEquals(discussionEntryEntities[1].groupId, result?.get(0))
    }
}
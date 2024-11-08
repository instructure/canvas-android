package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.UserEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var db: OfflineDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        userDao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testUserReplacementOnInsert() = runTest {
        val newUserName = "New User 1"
        val userEntity = UserEntity(User(id = 1, name = "User 1"))
        val newUserEntity = UserEntity(User(id = 1, name = newUserName))

        userDao.insert(userEntity)
        userDao.insert(newUserEntity)

        val result = userDao.findById(1L)

        assertEquals(newUserName, result?.name)
    }

    @Test
    fun testUserFindById() = runTest {
        val users = listOf(
            UserEntity(User(id = 1, name = "User 1")),
            UserEntity(User(id = 2, name = "User 2")),
            UserEntity(User(id = 3, name = "User 3")),
        )

        users.forEach { userDao.insert(it) }

        val result = userDao.findById(2L)

        assertEquals(users[1], result)
    }

    @Test
    fun testUserFindByIdNoContent() = runTest {
        val users = listOf(
                UserEntity(User(id = 1, name = "User 1")),
                UserEntity(User(id = 2, name = "User 2")),
        )

        users.forEach { userDao.insert(it) }

        val result = userDao.findById(5L)

        assertEquals(null, result)
    }
}
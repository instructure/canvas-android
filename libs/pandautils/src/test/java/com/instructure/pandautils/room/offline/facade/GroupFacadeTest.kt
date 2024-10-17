package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.GroupDao
import com.instructure.pandautils.room.offline.daos.GroupUserDao
import com.instructure.pandautils.room.offline.daos.UserDao
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GroupFacadeTest {
    private val userDao: UserDao = mockk(relaxed = true)
    private val groupDao: GroupDao = mockk(relaxed = true)
    private val groupUserDao: GroupUserDao = mockk(relaxed = true)

    private val facade = GroupFacade(groupUserDao, groupDao, userDao)

    @Test
    fun `Insert groups for users`() = runTest {
        val group = Group(1L)
        val user = User(1L)

        facade.insertGroupWithUser(group, user)

        coVerify(exactly = 1) { groupDao.insert(any()) }
        coVerify(exactly = 1) { userDao.insert(any()) }
        coVerify(exactly = 1) { groupUserDao.insert(any()) }
    }

    @Test
    fun `Get groups for users`() = runTest {
        facade.getGroupsByUserId(1L)

        coVerify(exactly = 1) { groupUserDao.findByUserId(any()) }
    }
}
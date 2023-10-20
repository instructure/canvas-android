package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.GroupDao
import com.instructure.pandautils.room.offline.daos.GroupUserDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.GroupEntity
import com.instructure.pandautils.room.offline.entities.GroupUserEntity
import com.instructure.pandautils.room.offline.entities.UserEntity

class GroupFacade(
    private val groupUserDao: GroupUserDao,
    private val groupDao: GroupDao,
    private val userDao: UserDao,
) {
    suspend fun insertGroupWithUser(group: Group, user: User) {
        groupDao.insert(GroupEntity(group))
        userDao.insert(UserEntity(user))
        groupUserDao.insert(GroupUserEntity(group.id, user.id))
    }

    suspend fun getGroupsByUserId(userId: Long): List<Group> {
        val groupIds =  groupUserDao.findByUserId(userId)
        val groups = mutableListOf<Group>()
        groupIds?.forEach { groupId ->
            val group = groupDao.findById(groupId)
            group?.let { groups.add(it.toApiModel()) }
        }
        return groups
    }
}
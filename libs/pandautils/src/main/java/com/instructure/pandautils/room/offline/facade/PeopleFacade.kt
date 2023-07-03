package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.EnrollmentDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.UserEntity

class PeopleFacade(
        private val userDao: UserDao,
        private val enrollmentDao: EnrollmentDao,
) {
    suspend fun insertPeople(peopleList: List<User>) {
        peopleList.forEach { user ->
            userDao.insert(UserEntity(user))
            user.enrollments.forEach { enrollment ->
                enrollment.observedUser?.let { userDao.insert(UserEntity(it)) }

                enrollmentDao.insert(EnrollmentEntity(
                        enrollment,
                        courseId = enrollment.courseId,
                        observedUserId = enrollment.observedUser?.id,
                ))
            }
        }
    }

    suspend fun getPeopleByCourseId(courseId: Long): List<User> {
        val enrollments = enrollmentDao.findByCourseId(courseId)
        val users = enrollments.mapNotNull { enrollment ->
            userDao.findById(enrollment.userId)?.toApiModel(enrollments.map { it.toApiModel() }.filter { it.userId == enrollment.userId })
        }
        return users
    }
}
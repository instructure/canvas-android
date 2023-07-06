/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.EnrollmentDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.UserEntity

class UserFacade(
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
        val users = enrollments.groupBy { it.userId }.keys.mapNotNull { userId ->
            userDao.findById(userId)?.toApiModel(enrollments.map { it.toApiModel() }.filter { it.userId == userId })
        }
        return users
    }

    suspend fun getPeopleByCourseIdAndRole(courseId: Long, role: Enrollment.EnrollmentType): List<User> {
        val enrollments = enrollmentDao.findByCourseIdAndRole(courseId, role.name)
        val users = enrollments.groupBy { it.userId }.keys.mapNotNull { userId ->
            userDao.findById(userId)?.toApiModel(enrollments.map { it.toApiModel() }.filter { it.userId == userId })
        }
        return users
    }
}
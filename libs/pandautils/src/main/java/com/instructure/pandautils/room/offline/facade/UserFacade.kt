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
import com.instructure.pandautils.room.offline.daos.GradesDao
import com.instructure.pandautils.room.offline.daos.SectionDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.GradesEntity
import com.instructure.pandautils.room.offline.entities.UserEntity

class UserFacade(
    private val userDao: UserDao,
    private val enrollmentDao: EnrollmentDao,
    private val gradesDao: GradesDao,
    private val sectionDao: SectionDao,
) {
    suspend fun insertUsers(userList: List<User>, courseId: Long) {
        userList.forEach { user ->
            userDao.insert(UserEntity(user))
            user.enrollments.forEach { enrollment ->
                enrollment.observedUser?.let { userDao.insert(UserEntity(it)) }

                val courseSectionIds = sectionDao.findByCourseId(courseId).map { it.id }
                val enrollmentId = if (courseSectionIds.contains(enrollment.courseSectionId)) {
                    enrollmentDao.insert(EnrollmentEntity(
                            enrollment,
                            courseId = courseId,
                            observedUserId = null
                    ))
                }
                else {
                    enrollmentDao.insert(EnrollmentEntity(
                            enrollment.copy(courseSectionId = 0L),
                            courseId = courseId,
                            courseSectionId = null,
                            observedUserId = null,
                    ))
                }


                enrollment.grades?.let { grades -> gradesDao.insert(GradesEntity(grades, enrollmentId)) }
            }
        }
    }

    suspend fun getUsersByCourseId(courseId: Long): List<User> {
        val enrollments = enrollmentDao.findByCourseId(courseId)
        return getUsersFromEnrollment(enrollments)
    }

    suspend fun getUsersByCourseIdAndRole(courseId: Long, role: Enrollment.EnrollmentType): List<User> {
        val enrollments = enrollmentDao.findByCourseIdAndRole(courseId, role.name)
        return getUsersFromEnrollment(enrollments)
    }

    private suspend fun getUsersFromEnrollment(enrollments: List<EnrollmentEntity>): List<User> {
        return enrollments.groupBy { it.userId }.keys.mapNotNull { userId ->
            userDao.findById(userId)?.toApiModel(enrollments.map { it.toApiModel() }.filter { it.userId == userId })
        }
    }

    suspend fun getUserById(userId: Long): User? {
        enrollmentDao.findByUserId(userId)?.let {
            return getUsersFromEnrollment(listOf(it)).firstOrNull()
        }
        return null
    }
}
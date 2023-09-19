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

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.pandautils.room.offline.daos.EnrollmentDao
import com.instructure.pandautils.room.offline.daos.GradesDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.GradesEntity
import com.instructure.pandautils.room.offline.entities.UserEntity

class EnrollmentFacade(
    private val userDao: UserDao,
    private val enrollmentDao: EnrollmentDao,
    private val gradesDao: GradesDao,
    private val userApi: UserAPI.UsersInterface
) {

    suspend fun insertEnrollment(enrollment: Enrollment, courseId: Long) {
        if (enrollment.userId != 0L) {
            val user = enrollment.user ?: userApi.getUser(
                enrollment.userId,
                RestParams(isForceReadFromNetwork = true)
            ).dataOrThrow
            userDao.insertOrUpdate(UserEntity(user))
        }

        enrollment.observedUser?.let { observedUser ->
            userDao.insertOrUpdate(UserEntity(observedUser))
        }

        val enrollmentId = enrollmentDao.insertOrUpdate(
            EnrollmentEntity(
                enrollment,
                courseId = courseId,
                observedUserId = enrollment.observedUser?.id
            )
        )

        enrollment.grades?.let { grades -> gradesDao.insert(GradesEntity(grades, enrollmentId)) }
    }

    suspend fun getEnrollmentsByCourseId(id: Long): List<Enrollment> {
        val enrollmentEntities = enrollmentDao.findByCourseId(id)
        return enrollmentEntities.map { enrollmentEntity ->
            val gradesEntity = gradesDao.findByEnrollmentId(enrollmentEntity.id)
            val observedUserEntity = enrollmentEntity.observedUserId?.let { userDao.findById(it) }
            val userEntity = userDao.findById(enrollmentEntity.userId)

            enrollmentEntity.toApiModel(
                grades = gradesEntity?.toApiModel(),
                observedUser = observedUserEntity?.toApiModel(),
                user = userEntity?.toApiModel()
            )
        }
    }

    suspend fun getAllEnrollments(): List<Enrollment> {
        val enrollmentEntities = enrollmentDao.findAll()
        return enrollmentEntities.map { createFullApiModelFromEntity(it) }
    }

    suspend fun getEnrollmentsByGradingPeriodId(gradingPeriodId: Long): List<Enrollment> {
        val enrollmentEntities = enrollmentDao.findByGradingPeriodId(gradingPeriodId)
        return enrollmentEntities.map { createFullApiModelFromEntity(it) }
    }

    private suspend fun createFullApiModelFromEntity(enrollmentEntity: EnrollmentEntity): Enrollment {
        val gradesEntity = gradesDao.findByEnrollmentId(enrollmentEntity.id)
        val observedUserEntity = enrollmentEntity.observedUserId?.let { userDao.findById(it) }
        val userEntity = userDao.findById(enrollmentEntity.userId)

        return enrollmentEntity.toApiModel(
            grades = gradesEntity?.toApiModel(),
            observedUser = observedUserEntity?.toApiModel(),
            user = userEntity?.toApiModel()
        )
    }
}

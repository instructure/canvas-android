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

package com.instructure.student.features.offline

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.pandautils.room.daos.*
import com.instructure.pandautils.room.entities.*

class SyncManager(
    private val courseManager: CourseManager,
    private val courseDao: CourseDao,
    private val enrollmentDao: EnrollmentDao,
    private val gradesDao: GradesDao,
    private val gradingPeriodDao: GradingPeriodDao,
    private val sectionDao: SectionDao,
    private val termDao: TermDao,
    private val userDao: UserDao,
    private val courseGradingPeriodDao: CourseGradingPeriodDao,
    private val tabDao: TabDao,
    private val userApi: UserAPI.UsersInterface
) {

    suspend fun fetchCourseContent(courseId: Long) {
        val course = courseManager.getFullCourseContentAsync(courseId, true).await().dataOrThrow

        course.term?.let { term ->
            termDao.insert(TermEntity(term))
        }

        courseDao.insert(CourseEntity(course))

        course.enrollments?.forEach { enrollment ->
            if (enrollment.userId != 0L) {
                val user = enrollment.user ?: userApi.getUser(
                    enrollment.userId,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrThrow
                userDao.insert(UserEntity(user))
            }

            if (enrollment.associatedUserId != 0L) {
                val associatedUser = enrollment.user ?: userApi.getUser(
                    enrollment.associatedUserId,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrThrow
                userDao.insert(UserEntity(associatedUser))
            }

            enrollment.observedUser?.let { userDao.insert(UserEntity(it)) }
            val enrollmentId = enrollmentDao.insert(EnrollmentEntity(enrollment, courseId = courseId, observedUserId = enrollment.observedUser?.id))
            enrollment.grades?.let { gradesDao.insert(GradesEntity(it, enrollmentId)) }
        }

        course.gradingPeriods?.forEach { gradingPeriod ->
            gradingPeriodDao.insert(GradingPeriodEntity(gradingPeriod))
            courseGradingPeriodDao.insert(CourseGradingPeriodEntity(course.id, gradingPeriod.id))
        }

        course.sections.forEach { section ->
            sectionDao.insert(SectionEntity(section, courseId))
        }

        course.tabs?.forEach { tab ->
            tabDao.insert(TabEntity(tab, course.id))
        }

    }
}
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
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.EnrollmentDao
import com.instructure.pandautils.room.offline.daos.GradesDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.GradesEntity
import com.instructure.pandautils.room.offline.entities.UserEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EnrollmentFacadeTest {

    private val userDao: UserDao = mockk(relaxed = true)
    private val enrollmentDao: EnrollmentDao = mockk(relaxed = true)
    private val gradesDao: GradesDao = mockk(relaxed = true)

    private val facade = EnrollmentFacade(userDao, enrollmentDao, gradesDao)

    @Test
    fun `Calling insertEnrollment should insert enrollment and related entities`() = runTest {
        val courseId = 1L
        val enrollmentId = 1L
        val grades = Grades(finalGrade = "finalGrade")
        val enrollment = Enrollment(id = enrollmentId, userId = 1L, grades = grades, observedUser = User(id = 2L))

        facade.insertEnrollment(enrollment, courseId)

        coVerify { enrollmentDao.insertOrUpdate(EnrollmentEntity(enrollment, courseId, observedUserId = enrollment.observedUser?.id)) }
        coVerify { gradesDao.insert(any()) }
    }

    @Test
    fun `Calling getEnrollmentsByCourseId should return the enrollments with the specified course ID`() = runTest {
        val courseId = 1L
        val grades = Grades(htmlUrl = "htmlUrl")
        val user = User(id = 1L, name = "User")
        val enrollment = Enrollment(id = 1L, courseId = courseId, observedUser = user, user = user, grades = grades, courseSectionId = 1L)

        coEvery { enrollmentDao.findByCourseId(courseId) } returns listOf(enrollment).map {
            EnrollmentEntity(it, courseId, null, observedUserId = user.id)
        }
        coEvery { gradesDao.findByEnrollmentId(enrollment.id) } returns GradesEntity(grades, enrollment.id)
        coEvery { userDao.findById(any()) } returns UserEntity(user)

        val result = facade.getEnrollmentsByCourseId(courseId)

        assertEquals(grades, result.first().grades)
        assertEquals(user, result.first().observedUser)
        assertEquals(user, result.first().user)
        assertEquals(enrollment, result.first())
    }

    @Test
    fun `Calling getAllEnrollments should return all enrollments`() = runTest {
        val courseId = 1L
        val grades = Grades(htmlUrl = "htmlUrl")
        val user = User(id = 1L, name = "User")
        val enrollment = Enrollment(id = 1L, courseId = courseId, observedUser = user, user = user, grades = grades, courseSectionId = 1L)

        coEvery { enrollmentDao.findAll() } returns listOf(enrollment).map {
            EnrollmentEntity(it, courseId, null, observedUserId = user.id)
        }
        coEvery { gradesDao.findByEnrollmentId(enrollment.id) } returns GradesEntity(grades, enrollment.id)
        coEvery { userDao.findById(any()) } returns UserEntity(user)

        val result = facade.getAllEnrollments()

        assertEquals(1, result.size)
        assertEquals(grades, result.first().grades)
        assertEquals(user, result.first().observedUser)
        assertEquals(user, result.first().user)
        assertEquals(enrollment, result.first())
    }

    @Test
    fun `Calling getEnrollmentsByGradingPeriodId should return enrollments by grading period id`() = runTest {
        val gradingPeriodId = 1L
        val courseId = 1L
        val grades = Grades(htmlUrl = "htmlUrl")
        val user = User(id = 1L, name = "User")
        val enrollment = Enrollment(
            id = 1L,
            courseId = courseId,
            observedUser = user,
            user = user,
            grades = grades,
            courseSectionId = 1L,
            currentGradingPeriodId = gradingPeriodId
        )

        coEvery { enrollmentDao.findByGradingPeriodId(any()) } returns listOf(enrollment).map {
            EnrollmentEntity(it, courseId, null, observedUserId = user.id)
        }
        coEvery { gradesDao.findByEnrollmentId(enrollment.id) } returns GradesEntity(grades, enrollment.id)
        coEvery { userDao.findById(any()) } returns UserEntity(user)

        val result = facade.getEnrollmentsByGradingPeriodId(gradingPeriodId)

        assertEquals(1, result.size)
        assertEquals(grades, result.first().grades)
        assertEquals(user, result.first().observedUser)
        assertEquals(user, result.first().user)
        assertEquals(enrollment, result.first())
    }

    @Test
    fun `Calling getEnrollmentsForUserByCourseId should return enrollments by user id and course id`() = runTest {
        val courseId = 1L
        val userId = 2L
        val grades = Grades(htmlUrl = "htmlUrl")
        val user = User(id = userId, name = "User")
        val enrollment = Enrollment(id = 1L, courseId = courseId, userId = userId, user = user, grades = grades, courseSectionId = 1L)

        coEvery { enrollmentDao.findByCourseIdAndUserId(courseId, userId) } returns listOf(enrollment).map {
            EnrollmentEntity(it, courseId, null, observedUserId = null)
        }
        coEvery { gradesDao.findByEnrollmentId(enrollment.id) } returns GradesEntity(grades, enrollment.id)
        coEvery { userDao.findById(any()) } returns UserEntity(user)

        val result = facade.getEnrollmentsForUserByCourseId(courseId, userId)

        assertEquals(1, result.size)
        assertEquals(grades, result.first().grades)
        assertEquals(user, result.first().user)
        assertEquals(enrollment, result.first())
    }
}

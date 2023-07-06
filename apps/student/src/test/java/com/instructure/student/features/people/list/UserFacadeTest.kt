package com.instructure.student.features.people.list

import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.EnrollmentDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.UserEntity
import com.instructure.pandautils.room.offline.facade.UserFacade
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class UserFacadeTest {
    private val userDao: UserDao = mockk(relaxed = true)
    private val enrollmentDao: EnrollmentDao = mockk(relaxed = true)

    private val userFacade = UserFacade(userDao, enrollmentDao)

    @Test
    fun `Get users as api model`() = runTest {
        val expectedUsers = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L, userId = 1L), Enrollment(2L, userId = 1L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L, userId = 2L))),
        )
        val expectedEnrollments = expectedUsers.flatMap { it.enrollments }.map { EnrollmentEntity(it, it.courseId, it.courseSectionId, 0) }

        coEvery { enrollmentDao.findByCourseId(any()) } returns expectedEnrollments
        coEvery { userDao.findById(1L) } returns UserEntity(expectedUsers[0])
        coEvery { userDao.findById(2L) } returns UserEntity(expectedUsers[1])

        val users = userFacade.getPeopleByCourseId(1L)
        assertEquals(expectedUsers, users)

    }
}
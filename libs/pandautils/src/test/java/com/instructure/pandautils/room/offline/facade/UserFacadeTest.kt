package com.instructure.pandautils.room.offline.facade

import androidx.room.withTransaction
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.EnrollmentDao
import com.instructure.pandautils.room.offline.daos.SectionDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.UserEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserFacadeTest {
    private val userDao: UserDao = mockk(relaxed = true)
    private val enrollmentDao: EnrollmentDao = mockk(relaxed = true)
    private val enrollmentFacade: EnrollmentFacade = mockk(relaxed = true)
    private val sectionDao: SectionDao = mockk(relaxed = true)
    private val offlineDatabase: OfflineDatabase = mockk(relaxed = true)

    private val userFacade = UserFacade(userDao, enrollmentDao, sectionDao, enrollmentFacade, offlineDatabase)

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { offlineDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

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

        val users = userFacade.getUsersByCourseId(1L)
        assertEquals(expectedUsers, users)

    }

    @Test
    fun `Dao insert functions are called`() = runTest {
        val users = listOf(
            User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L, userId = 1L), Enrollment(2L, userId = 1L))),
            User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L, userId = 2L))),
        )

        userFacade.insertUsers(users, 1L)

        coVerify(exactly = 3) { enrollmentFacade.insertEnrollment(any(), 1L) }
    }

    @Test
    fun `Get users by course id`() = runTest {
        val expectedUsers = listOf(
            User(
                id = 1L,
                name = "User 1",
                enrollments = listOf(
                    Enrollment(1L, userId = 1L, role = Enrollment.EnrollmentType.Teacher),
                    Enrollment(2L, userId = 1L, role = Enrollment.EnrollmentType.Student)
                )
            ),
            User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L, userId = 2L, role = Enrollment.EnrollmentType.Student))),
        )
        val expectedEnrollments = expectedUsers.flatMap { it.enrollments }.map { EnrollmentEntity(it, it.courseId, it.courseSectionId, 0) }

        coEvery { enrollmentDao.findByCourseId(any()) } returns expectedEnrollments
        coEvery { userDao.findById(1) } returns UserEntity(expectedUsers[0])
        coEvery { userDao.findById(2) } returns UserEntity(expectedUsers[1])

        val result = userFacade.getUsersByCourseId(1L)

        assertEquals(expectedUsers, result)
    }

    @Test
    fun `Get teachers by course id`() = runTest {
        val teacherRole = Enrollment.EnrollmentType.Teacher
        val studentRole = Enrollment.EnrollmentType.Student
        val users = listOf(
            User(
                id = 1L,
                name = "User 1",
                enrollments = listOf(Enrollment(1L, userId = 1L, role = teacherRole), Enrollment(2L, userId = 1L, role = studentRole))
            ),
            User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L, userId = 2L, role = studentRole))),
        )
        val expectedEnrollments = users.flatMap { it.enrollments }.map { EnrollmentEntity(it, it.courseId, it.courseSectionId, 0) }
        val expectedUsers = users.filter { it.enrollments.any { enrollment -> enrollment.role == teacherRole } }

        coEvery { enrollmentDao.findByCourseIdAndRole(any(), teacherRole.name) } returns expectedEnrollments.filter { it.role == teacherRole.name }
        coEvery { enrollmentDao.findByCourseIdAndRole(any(), studentRole.name) } returns expectedEnrollments.filter { it.role == studentRole.name }
        coEvery { userDao.findById(1) } returns UserEntity(users[0])
        coEvery { userDao.findById(2) } returns UserEntity(users[1])

        val result = userFacade.getUsersByCourseIdAndRole(1L, teacherRole)

        assertEquals(expectedUsers, result)
    }

    @Test
    fun `Get students by course id`() = runTest {
        val teacherRole = Enrollment.EnrollmentType.Teacher
        val studentRole = Enrollment.EnrollmentType.Student
        val users = listOf(
            User(
                id = 1L,
                name = "User 1",
                enrollments = listOf(Enrollment(1L, userId = 1L, role = teacherRole), Enrollment(2L, userId = 1L, role = studentRole))
            ),
            User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L, userId = 2L, role = studentRole))),
        )
        val expectedEnrollments = users.flatMap { it.enrollments }.map { EnrollmentEntity(it, it.courseId, it.courseSectionId, 0) }
        val expectedUsers = users.filter { it.enrollments.any { enrollment -> enrollment.role == studentRole } }

        coEvery { enrollmentDao.findByCourseIdAndRole(any(), teacherRole.name) } returns expectedEnrollments.filter { it.role == teacherRole.name }
        coEvery { enrollmentDao.findByCourseIdAndRole(any(), studentRole.name) } returns expectedEnrollments.filter { it.role == studentRole.name }
        coEvery { userDao.findById(1) } returns UserEntity(users[0])
        coEvery { userDao.findById(2) } returns UserEntity(users[1])

        val result = userFacade.getUsersByCourseIdAndRole(1L, studentRole)

        assertEquals(expectedUsers, result)
    }
}
/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.room.offline.daos

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnrollmentDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var enrollmentDao: EnrollmentDao
    private lateinit var userDao: UserDao
    private lateinit var sectionDao: SectionDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        enrollmentDao = db.enrollmentDao()
        userDao = db.userDao()
        sectionDao = db.sectionDao()
        courseDao = db.courseDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(id = 1)))
            courseDao.insert(CourseEntity(Course(id = 2)))
            userDao.insert(UserEntity(User(id = 1)))
            userDao.insert(UserEntity(User(id = 2)))
            userDao.insert(UserEntity(User(id = 3)))
            sectionDao.insert(SectionEntity(Section(id = 1), 1))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindAllEntities() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 1), 1, 1, 1)
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val result = enrollmentDao.findAll()

        Assert.assertEquals(entities, result)
    }

    @Test
    fun testFindByCourseId() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 1), 2, 1, 1)
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val result = enrollmentDao.findByCourseId(1)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(entities.first(), result.first())
    }

    @Test
    fun testFindByGradingPeriodId() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1, currentGradingPeriodId = 1), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 1), 2, 1, 1)
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val result = enrollmentDao.findByGradingPeriodId(1)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(entities.first(), result.first())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testUserForeignKey() = runTest {
        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 1, 4)

        enrollmentDao.insert(enrollmentEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testObservedUserForeignKey() = runTest {
        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 1, 4)

        enrollmentDao.insert(enrollmentEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testSectionForeignKey() = runTest {
        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 2, 1)

        enrollmentDao.insert(enrollmentEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKey() = runTest {
        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 1), 3, 1, 1)

        enrollmentDao.insert(enrollmentEntity)
    }

    @Test
    fun testObservedUserSetNullOnDelete() = runTest {
        userDao.insert(UserEntity(User(id = 2)))

        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 1, 2)

        enrollmentDao.insert(enrollmentEntity)

        userDao.delete(UserEntity(User(id = 2)))

        val result = enrollmentDao.findAll()

        Assert.assertEquals(listOf(enrollmentEntity.copy(observedUserId = null)), result)
    }

    @Test
    fun testSectionSetNullOnDelete() = runTest {
        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 1, 1)

        enrollmentDao.insert(enrollmentEntity)

        sectionDao.delete(SectionEntity(Section(1)))

        val result = enrollmentDao.findAll()

        Assert.assertEquals(listOf(enrollmentEntity.copy(courseSectionId = null)), result)
    }

    @Test
    fun testCourseCascade() = runTest {
        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 1), 1, 1, 1)

        enrollmentDao.insert(enrollmentEntity)

        courseDao.delete(CourseEntity(Course(1)))

        val result = enrollmentDao.findAll()

        assert(result.isEmpty())
    }

    @Test
    fun testUserCascade() = runTest {
        userDao.insert(UserEntity(User(id = 4)))

        val enrollmentEntity = EnrollmentEntity(Enrollment(id = 1, userId = 4), 1, 1, 1)

        enrollmentDao.insert(enrollmentEntity)

        userDao.delete(UserEntity(User(id = 4)))

        val result = enrollmentDao.findAll()

        assert(result.isEmpty())
    }

    @Test
    fun testFindByCourseIdAndRoleTeachers() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1, role = Enrollment.EnrollmentType.Student), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 2, role = Enrollment.EnrollmentType.Teacher), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 3, userId = 3, role = Enrollment.EnrollmentType.Teacher), 1, 1, 1),
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val result = enrollmentDao.findByCourseIdAndRole(1, Enrollment.EnrollmentType.Teacher.name)

        Assert.assertEquals(entities.filter { it.role == Enrollment.EnrollmentType.Teacher.name }, result)
    }

    @Test
    fun testFindByCourseIdAndRoleStudents() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1, role = Enrollment.EnrollmentType.Student), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 2, role = Enrollment.EnrollmentType.Teacher), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 3, userId = 3, role = Enrollment.EnrollmentType.Student), 1, 1, 1),
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val result = enrollmentDao.findByCourseIdAndRole(1, Enrollment.EnrollmentType.Student.name)

        Assert.assertEquals(entities.filter { it.role == Enrollment.EnrollmentType.Student.name }, result)
    }

    @Test
    fun testFindByCourseIdAndRoleCourses() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1, role = Enrollment.EnrollmentType.Student), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 2, role = Enrollment.EnrollmentType.Teacher), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 3, userId = 3, role = Enrollment.EnrollmentType.Teacher), 2, 1, 1),
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val result = enrollmentDao.findByCourseIdAndRole(1, Enrollment.EnrollmentType.Teacher.name)

        Assert.assertEquals(entities.filter { it.role == Enrollment.EnrollmentType.Teacher.name && it.courseId == 1L }, result)
    }

    @Test
    fun testFindByUserId() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1, role = Enrollment.EnrollmentType.Student), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 2, role = Enrollment.EnrollmentType.Teacher), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 3, userId = 3, role = Enrollment.EnrollmentType.Teacher), 2, 1, 1),
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val expected = entities.first { it.userId == 1L }

        val result = enrollmentDao.findByUserId(1L)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun testFindUSerByNonExistingUserId() = runTest {
        val entities = listOf(
            EnrollmentEntity(Enrollment(id = 1, userId = 1, role = Enrollment.EnrollmentType.Student), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 2, userId = 2, role = Enrollment.EnrollmentType.Teacher), 1, 1, 1),
            EnrollmentEntity(Enrollment(id = 3, userId = 3, role = Enrollment.EnrollmentType.Teacher), 2, 1, 1),
        )
        entities.forEach {
            enrollmentDao.insert(it)
        }

        val expected = null

        val result = enrollmentDao.findByUserId(4L)

        Assert.assertEquals(expected, result)
    }
}

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
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.AssignmentRubricCriterionEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssignmentRubricCriterionDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var assignmentRubricCriterionDao: AssignmentRubricCriterionDao
    private lateinit var assignmentDao: AssignmentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        assignmentRubricCriterionDao = db.assignmentRubricCriterionDao()
        assignmentDao = db.assignmentDao()

        runBlocking {
            db.courseDao().insert(CourseEntity(Course(id = 1L)))
            db.assignmentGroupDao().insert(AssignmentGroupEntity(AssignmentGroup(id = 1L), 1L))
            assignmentDao.insert(
                AssignmentEntity(
                    Assignment(id = 1L, courseId = 1L, assignmentGroupId = 1L),
                    null,
                    null,
                    null,
                    null
                )
            )
            assignmentDao.insert(
                AssignmentEntity(
                    Assignment(id = 2L, courseId = 1L, assignmentGroupId = 1L),
                    null,
                    null,
                    null,
                    null
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityByAssignmentId() = runTest {
        val assignmentRubricCriterionEntity = AssignmentRubricCriterionEntity(1, "1")
        val assignmentRubricCriterionEntity2 = AssignmentRubricCriterionEntity(2, "2")
        assignmentRubricCriterionDao.insert(assignmentRubricCriterionEntity)
        assignmentRubricCriterionDao.insert(assignmentRubricCriterionEntity2)

        val result = assignmentRubricCriterionDao.findByAssignmentId(1)

        Assert.assertEquals(listOf(assignmentRubricCriterionEntity), result)
    }

    @Test
    fun testAssignmentCascade() = runTest {
        val assignmentRubricCriterionEntity = AssignmentRubricCriterionEntity(1, "1")
        assignmentRubricCriterionDao.insert(assignmentRubricCriterionEntity)

        assignmentDao.delete(
            AssignmentEntity(
                Assignment(id = 1L, courseId = 1L, assignmentGroupId = 1L),
                null,
                null,
                null,
                null
            )
        )

        val result = assignmentRubricCriterionDao.findByAssignmentId(1)

        assert(result.isEmpty())
    }
}

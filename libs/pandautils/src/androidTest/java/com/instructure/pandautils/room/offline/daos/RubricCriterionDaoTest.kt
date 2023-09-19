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
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.RubricCriterionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RubricCriterionDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var rubricCriterionDao: RubricCriterionDao
    private lateinit var assignmentDao: AssignmentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        rubricCriterionDao = db.rubricCriterionDao()
        assignmentDao = db.assignmentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        assignmentDao.insert(AssignmentEntity(Assignment(1L), null, null, null, null))
        val rubricCriterionEntity = RubricCriterionEntity(RubricCriterion("1"), 1L)
        val rubricCriterionEntity2 = RubricCriterionEntity(RubricCriterion("2"), 1L)
        rubricCriterionDao.insert(rubricCriterionEntity)
        rubricCriterionDao.insert(rubricCriterionEntity2)

        val result = rubricCriterionDao.findById("1")

        Assert.assertEquals(rubricCriterionEntity, result)
    }
}

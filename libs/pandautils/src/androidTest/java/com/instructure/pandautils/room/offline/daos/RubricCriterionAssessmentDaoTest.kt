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
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.RubricCriterionAssessmentEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RubricCriterionAssessmentDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var rubricCriterionAssessmentDao: RubricCriterionAssessmentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        rubricCriterionAssessmentDao = db.rubricCriterionAssessmentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityBySubmissionId() = runTest {
        val entities = listOf(
            RubricCriterionAssessmentEntity(RubricCriterionAssessment(ratingId = "1"), "1", 1),
            RubricCriterionAssessmentEntity(RubricCriterionAssessment(ratingId = "2"), "2", 2),
        )
        rubricCriterionAssessmentDao.insertAll(entities)

        val result = rubricCriterionAssessmentDao.findByAssignmentId(1)

        Assert.assertEquals(entities.filter { it.assignmentId == 1L }, result)
    }
}

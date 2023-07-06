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
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.RubricCriterionEntity
import com.instructure.pandautils.room.offline.entities.RubricCriterionRatingEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RubricCriterionRatingDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var rubricCriterionRatingDao: RubricCriterionRatingDao
    private lateinit var rubricCriterionDao: RubricCriterionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        rubricCriterionRatingDao = db.rubricCriterionRatingDao()
        rubricCriterionDao = db.rubricCriterionDao()

        runBlocking {
            rubricCriterionDao.insert(RubricCriterionEntity(RubricCriterion("1"), 1))
            rubricCriterionDao.insert(RubricCriterionEntity(RubricCriterion("2"), 1))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityByCourseId() = runTest {
        val entities = listOf(
            RubricCriterionRatingEntity(RubricCriterionRating("1"), "1"),
            RubricCriterionRatingEntity(RubricCriterionRating("2"), "2"),
        )
        rubricCriterionRatingDao.insertAll(entities)

        val result = rubricCriterionRatingDao.findByRubricCriterionId("1")

        Assert.assertEquals(entities.filter { it.rubricCriterionId == "1" }, result)
    }

    @Test
    fun testRubricCriterionCascade() = runTest {
        val rubricCriterionRatingEntity = RubricCriterionRatingEntity(RubricCriterionRating("1"), "1")

        rubricCriterionRatingDao.insert(rubricCriterionRatingEntity)

        rubricCriterionDao.delete(RubricCriterionEntity(RubricCriterion("1"), 1))

        val result = rubricCriterionRatingDao.findByRubricCriterionId("1")

        assert(result.isEmpty())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testRubricCriterionForeignKey() = runTest {
        val rubricCriterionRatingEntity = RubricCriterionRatingEntity(RubricCriterionRating("1"), "3")

        rubricCriterionRatingDao.insert(rubricCriterionRatingEntity)
    }
}

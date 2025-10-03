/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.PlannerItemEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class PlannerItemDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var plannerItemDao: PlannerItemDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        plannerItemDao = db.plannerItemDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        val plannerItems = createPlannerItems(listOf(1L, 2L), 1L)
        val expectedPlannerItem = plannerItems[1]

        plannerItems.forEach {
            plannerItemDao.insert(it)
        }

        val result = plannerItemDao.findById(2L)

        assertEquals(expectedPlannerItem.plannableTitle, result?.plannableTitle)
    }

    @Test
    fun testFindByCourseId() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        courseDao.insert(CourseEntity(Course(2L)))
        val plannerItems = listOf(
            createPlannerItem(1L, 2L, "Item 1"),
            createPlannerItem(2L, 1L, "Item 2"),
            createPlannerItem(3L, 2L, "Item 3")
        )
        val expectedItems = plannerItems.filter { it.courseId == 2L }

        plannerItems.forEach { plannerItemDao.insert(it) }

        val result = plannerItemDao.findByCourseId(2L)
        assertEquals(expectedItems.map { it.plannableTitle }, result.map { it.plannableTitle })
    }

    @Test
    fun testFindByCourseIds() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        courseDao.insert(CourseEntity(Course(2L)))
        courseDao.insert(CourseEntity(Course(3L)))
        val plannerItems = listOf(
            createPlannerItem(1L, 1L, "Item 1"),
            createPlannerItem(2L, 2L, "Item 2"),
            createPlannerItem(3L, 3L, "Item 3"),
            createPlannerItem(4L, 1L, "Item 4")
        )
        val expectedItems = plannerItems.filter { it.courseId in listOf(1L, 2L) }

        plannerItems.forEach { plannerItemDao.insert(it) }

        val result = plannerItemDao.findByCourseIds(listOf(1L, 2L))
        assertEquals(expectedItems.map { it.plannableTitle }.sorted(), result.map { it.plannableTitle }.sorted())
    }

    @Test
    fun testInsertReplace() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        val plannerItem1 = createPlannerItem(1L, 1L, "Item 1")
        val plannerItem2 = createPlannerItem(1L, 1L, "Item 2")

        plannerItemDao.insert(plannerItem1)
        plannerItemDao.insert(plannerItem2)

        val result = plannerItemDao.findById(1L)

        assertEquals(plannerItem2.plannableTitle, result?.plannableTitle)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testForeignKeyConstraint() = runTest {
        plannerItemDao.insert(createPlannerItem(1L, 1L, "Item 1"))
    }

    @Test
    fun testInsertAll() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))

        val plannerItems = createPlannerItems(listOf(1L, 2L, 3L), 1L)
        plannerItemDao.insertAll(plannerItems)

        val result = plannerItemDao.findByCourseId(1L)
        assertEquals(plannerItems.size, result.size)
    }

    @Test
    fun testDeleteAllByCourseId() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        courseDao.insert(CourseEntity(Course(2L)))

        val plannerItems = listOf(
            createPlannerItem(1L, 1L, "Item 1"),
            createPlannerItem(2L, 1L, "Item 2"),
            createPlannerItem(3L, 2L, "Item 3")
        )
        plannerItems.forEach { plannerItemDao.insert(it) }

        val resultBefore = plannerItemDao.findByCourseId(1L)
        assertEquals(2, resultBefore.size)

        plannerItemDao.deleteAllByCourseId(1L)

        val resultAfter = plannerItemDao.findByCourseId(1L)
        Assert.assertTrue(resultAfter.isEmpty())

        val course2Items = plannerItemDao.findByCourseId(2L)
        assertEquals(1, course2Items.size)
    }

    @Test
    fun testUpdate() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        val plannerItem = createPlannerItem(1L, 1L, "Item 1")
        plannerItemDao.insert(plannerItem)

        val updatedItem = plannerItem.copy(plannableTitle = "Updated Item")
        plannerItemDao.update(updatedItem)

        val result = plannerItemDao.findById(1L)
        assertEquals("Updated Item", result?.plannableTitle)
    }

    @Test
    fun testDelete() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        val plannerItem = createPlannerItem(1L, 1L, "Item 1")
        plannerItemDao.insert(plannerItem)

        val resultBefore = plannerItemDao.findById(1L)
        assertEquals(plannerItem.plannableTitle, resultBefore?.plannableTitle)

        plannerItemDao.delete(plannerItem)

        val resultAfter = plannerItemDao.findById(1L)
        assertEquals(null, resultAfter)
    }

    private fun createPlannerItem(id: Long, courseId: Long, title: String): PlannerItemEntity {
        val plannable = Plannable(
            id = id,
            title = title,
            courseId = courseId,
            groupId = null,
            userId = null,
            pointsPossible = 10.0,
            dueAt = Date(),
            assignmentId = id,
            todoDate = null,
            startAt = null,
            endAt = null,
            details = "Details for $title",
            allDay = false
        )
        val plannerItem = PlannerItem(
            courseId = courseId,
            groupId = null,
            userId = null,
            contextType = "course",
            contextName = "Course $courseId",
            plannableType = PlannableType.ASSIGNMENT,
            plannable = plannable,
            plannableDate = Date(),
            htmlUrl = "https://example.com/item/$id",
            submissionState = null,
            newActivity = false
        )
        return PlannerItemEntity(plannerItem, courseId)
    }

    private fun createPlannerItems(ids: List<Long>, courseId: Long): List<PlannerItemEntity> {
        return ids.map { createPlannerItem(it, courseId, "Item $it") }
    }
}
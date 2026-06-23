/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.data.datasource

import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.dao.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.entity.HorizonCourseModuleItemEntity
import com.instructure.horizon.database.entity.HorizonDashboardModuleItemEntity
import com.instructure.horizon.model.DashboardNextModuleItem
import com.instructure.horizon.model.LearningObjectType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class ModuleItemLocalDataSourceTest {

    private val moduleItemDao: HorizonDashboardModuleItemDao = mockk(relaxed = true)
    private val courseModuleDao: HorizonCourseModuleDao = mockk(relaxed = true)
    private val dataSource = ModuleItemLocalDataSource(moduleItemDao, courseModuleDao)

    private val courseId = 100L

    private fun courseItem(
        id: Long,
        moduleId: Long = 1L,
        title: String = "Item $id",
        type: String = "Assignment",
        dueAt: String? = null,
        estimatedDuration: String? = null,
        quizLti: Boolean = false,
    ): HorizonCourseModuleItemEntity = HorizonCourseModuleItemEntity(
        itemId = id,
        moduleId = moduleId,
        courseId = courseId,
        title = title,
        position = 1,
        indent = 0,
        type = type,
        htmlUrl = null,
        url = null,
        contentId = 0L,
        externalUrl = null,
        pageUrl = null,
        published = true,
        unpublishable = false,
        completionRequirementType = "must_submit",
        completionRequirementMinScore = 0.0,
        completionRequirementCompleted = false,
        pointsPossible = null,
        dueAt = dueAt,
        lockedForUser = false,
        lockExplanation = null,
        lockAt = null,
        unlockAt = null,
        hidden = false,
        locked = false,
        quizLti = quizLti,
        estimatedDuration = estimatedDuration,
    )

    private fun dashboardItem(
        id: Long,
        title: String = "Dashboard item $id",
        type: LearningObjectType = LearningObjectType.ASSIGNMENT,
        dueDateMs: Long? = null,
        estimatedDuration: String? = null,
        isQuizLti: Boolean = false,
    ): HorizonDashboardModuleItemEntity = HorizonDashboardModuleItemEntity(
        moduleItemId = id,
        courseId = courseId,
        moduleItemTitle = title,
        moduleItemType = type.name,
        dueDateMs = dueDateMs,
        estimatedDuration = estimatedDuration,
        isQuizLti = isQuizLti,
    )

    @Test
    fun `returns synced course module item when present and does not consult fallback`() = runTest {
        val item = courseItem(id = 7L, title = "Synced item", type = "Assignment", estimatedDuration = "PT5M")
        coEvery { courseModuleDao.getNextModuleItemForCourse(courseId) } returns item

        val result = dataSource.getNextModuleItemForCourse(courseId)

        assertEquals(7L, result?.moduleItemId)
        assertEquals("Synced item", result?.title)
        assertEquals(LearningObjectType.ASSIGNMENT, result?.type)
        assertEquals("PT5M", result?.estimatedDuration)
        coVerify(exactly = 0) { moduleItemDao.getFirstForCourse(any()) }
    }

    @Test
    fun `maps quizLti synced item to ASSESSMENT regardless of type string`() = runTest {
        val item = courseItem(id = 8L, type = "ExternalTool", quizLti = true)
        coEvery { courseModuleDao.getNextModuleItemForCourse(courseId) } returns item

        val result = dataSource.getNextModuleItemForCourse(courseId)

        assertEquals(LearningObjectType.ASSESSMENT, result?.type)
        assertEquals(true, result?.isQuizLti)
    }

    @Test
    fun `falls back to dashboard cache when synced data has no incomplete item`() = runTest {
        coEvery { courseModuleDao.getNextModuleItemForCourse(courseId) } returns null
        val cached = dashboardItem(
            id = 9L,
            title = "Cached item",
            type = LearningObjectType.PAGE,
            dueDateMs = 1_700_000_000_000L,
            estimatedDuration = "PT10M",
        )
        coEvery { moduleItemDao.getFirstForCourse(courseId) } returns cached

        val result = dataSource.getNextModuleItemForCourse(courseId)

        assertEquals(9L, result?.moduleItemId)
        assertEquals("Cached item", result?.title)
        assertEquals(LearningObjectType.PAGE, result?.type)
        assertEquals(Date(1_700_000_000_000L), result?.dueDate)
        assertEquals("PT10M", result?.estimatedDuration)
    }

    @Test
    fun `returns null when neither synced data nor dashboard cache have an item`() = runTest {
        coEvery { courseModuleDao.getNextModuleItemForCourse(courseId) } returns null
        coEvery { moduleItemDao.getFirstForCourse(courseId) } returns null

        val result = dataSource.getNextModuleItemForCourse(courseId)

        assertNull(result)
    }

    @Test
    fun `saveNextModuleItem stores the mapped entity in the dashboard cache`() = runTest {
        val item = DashboardNextModuleItem(
            moduleItemId = 42L,
            courseId = courseId,
            title = "To save",
            type = LearningObjectType.ASSIGNMENT,
            dueDate = Date(1_700_000_000_000L),
            estimatedDuration = "PT15M",
            isQuizLti = true,
        )

        dataSource.saveNextModuleItem(item)

        coVerify(exactly = 1) {
            moduleItemDao.replaceForCourse(
                HorizonDashboardModuleItemEntity(
                    moduleItemId = 42L,
                    courseId = courseId,
                    moduleItemTitle = "To save",
                    moduleItemType = LearningObjectType.ASSIGNMENT.name,
                    dueDateMs = 1_700_000_000_000L,
                    estimatedDuration = "PT15M",
                    isQuizLti = true,
                )
            )
        }
    }
}

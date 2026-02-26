/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.learninglibrary.enroll

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class LearnLearningLibraryEnrollRepositoryTest {
    private val getLearningLibraryManager: GetLearningLibraryManager = mockk(relaxed = true)
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val testUser = User(id = 42L)
    private val testCourse = CourseWithProgress(
        courseId = 1L,
        courseName = "Test Course",
        courseImageUrl = "https://example.com/image.png",
        courseSyllabus = "Course syllabus content",
        progress = 0.0
    )
    private val testCollectionItem = createTestCollectionItem("item1", "1", "Test Course")

    @Before
    fun setup() {
        every { apiPrefs.user } returns testUser
        coEvery { getLearningLibraryManager.getLearningLibraryItem(any(), any()) } returns testCollectionItem
        coEvery { getCoursesManager.getCourseWithProgressById(any(), any()) } returns DataResult.Success(testCourse)
        coEvery { getLearningLibraryManager.enrollLearningLibraryItem(any()) } returns testCollectionItem
    }

    @Test
    fun `loadLearningLibraryItem returns collection item`() = runTest {
        val repository = getRepository()

        val result = repository.loadLearningLibraryItem("item1")

        assertEquals(testCollectionItem, result)
        coVerify { getLearningLibraryManager.getLearningLibraryItem("item1", false) }
    }

    @Test
    fun `loadLearningLibraryItem passes forceNetwork false to manager`() = runTest {
        val repository = getRepository()

        repository.loadLearningLibraryItem("item1")

        coVerify { getLearningLibraryManager.getLearningLibraryItem("item1", false) }
    }

    @Test
    fun `loadCourseDetails returns course with progress`() = runTest {
        val repository = getRepository()

        val result = repository.loadCourseDetails(1L)

        assertEquals(testCourse, result)
    }

    @Test
    fun `loadCourseDetails passes courseId and userId to manager`() = runTest {
        val repository = getRepository()

        repository.loadCourseDetails(1L)

        coVerify { getCoursesManager.getCourseWithProgressById(1L, 42L) }
    }

    @Test
    fun `loadCourseDetails uses -1L as userId when user is null`() = runTest {
        every { apiPrefs.user } returns null
        val repository = getRepository()

        repository.loadCourseDetails(1L)

        coVerify { getCoursesManager.getCourseWithProgressById(1L, -1L) }
    }

    @Test
    fun `loadCourseDetails throws when manager returns failure`() = runTest {
        coEvery { getCoursesManager.getCourseWithProgressById(any(), any()) } returns DataResult.Fail()
        val repository = getRepository()

        var threw = false
        try {
            repository.loadCourseDetails(1L)
        } catch (e: Exception) {
            threw = true
        }

        assertEquals(true, threw)
    }

    @Test
    fun `enrollLearningLibraryItem returns enrolled item`() = runTest {
        val enrolledItem = createTestCollectionItem("item1", "1", "Test Course Enrolled")
        coEvery { getLearningLibraryManager.enrollLearningLibraryItem("item1") } returns enrolledItem
        val repository = getRepository()

        val result = repository.enrollLearningLibraryItem("item1")

        assertEquals(enrolledItem, result)
        coVerify { getLearningLibraryManager.enrollLearningLibraryItem("item1") }
    }

    @Test
    fun `enrollLearningLibraryItem passes itemId to manager`() = runTest {
        val repository = getRepository()

        repository.enrollLearningLibraryItem("item-42")

        coVerify { getLearningLibraryManager.enrollLearningLibraryItem("item-42") }
    }

    private fun getRepository(): LearnLearningLibraryEnrollRepository {
        return LearnLearningLibraryEnrollRepository(getLearningLibraryManager, getCoursesManager, apiPrefs)
    }

    private fun createTestCollectionItem(
        id: String,
        courseId: String,
        courseName: String
    ): LearningLibraryCollectionItem = LearningLibraryCollectionItem(
        id = id,
        libraryId = "library1",
        itemType = CollectionItemType.COURSE,
        displayOrder = 1.0,
        canvasCourse = CanvasCourseInfo(
            courseId = courseId,
            canvasUrl = "https://example.com",
            courseName = courseName,
            courseImageUrl = "https://example.com/image.png",
            moduleCount = 5.0,
            moduleItemCount = 20.0,
            estimatedDurationMinutes = 120.0
        ),
        programId = null,
        programCourseId = null,
        createdAt = Date(),
        updatedAt = Date(),
        isBookmarked = false,
        completionPercentage = 0.0,
        isEnrolledInCanvas = false
    )
}
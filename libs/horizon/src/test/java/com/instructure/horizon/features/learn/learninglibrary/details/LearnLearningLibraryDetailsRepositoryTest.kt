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
package com.instructure.horizon.features.learn.learninglibrary.details

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class LearnLearningLibraryDetailsRepositoryTest {
    private val getLearningLibraryManager: GetLearningLibraryManager = mockk(relaxed = true)

    private val testCollection = EnrolledLearningLibraryCollection(
        id = "collection1",
        name = "Test Collection",
        publicName = "Test Collection Public",
        description = "Test description",
        createdAt = Date(),
        updatedAt = Date(),
        items = listOf(
            createTestCollectionItem(
                id = "item1",
                courseName = "Course 1",
                courseId = "1",
                isBookmarked = false,
                itemType = CollectionItemType.COURSE
            ),
            createTestCollectionItem(
                id = "item2",
                courseName = "Course 2",
                courseId = "2",
                isBookmarked = true,
                itemType = CollectionItemType.PAGE
            ),
            createTestCollectionItem(
                id = "item3",
                courseName = "Assignment 1",
                courseId = "3",
                isBookmarked = false,
                itemType = CollectionItemType.ASSIGNMENT
            )
        )
    )

    @Before
    fun setup() {
        coEvery { getLearningLibraryManager.getEnrolledLearningLibraryCollection(any(), any()) } returns testCollection
    }

    @Test
    fun `getLearningLibraryItems returns collection with items`() = runTest {
        val repository = getRepository()
        val result = repository.getLearningLibraryItems("collection1", false)

        assertEquals("Test Collection", result.name)
        assertEquals(3, result.items.size)
        assertEquals("Course 1", result.items[0].canvasCourse?.courseName)
        coVerify { getLearningLibraryManager.getEnrolledLearningLibraryCollection("collection1", false) }
    }

    @Test
    fun `getLearningLibraryItems with forceRefresh true calls API with force refresh`() = runTest {
        val repository = getRepository()
        repository.getLearningLibraryItems("collection1", true)

        coVerify { getLearningLibraryManager.getEnrolledLearningLibraryCollection("collection1", true) }
    }

    @Test
    fun `getLearningLibraryItems returns empty items when collection has no items`() = runTest {
        val emptyCollection = testCollection.copy(items = emptyList())
        coEvery { getLearningLibraryManager.getEnrolledLearningLibraryCollection(any(), any()) } returns emptyCollection
        val repository = getRepository()

        val result = repository.getLearningLibraryItems("collection1", false)

        assertEquals(0, result.items.size)
    }

    @Test
    fun `toggleLearningLibraryItemIsBookmarked returns new bookmark state true`() = runTest {
        coEvery { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item1") } returns true
        val repository = getRepository()

        val result = repository.toggleLearningLibraryItemIsBookmarked("item1")

        assertTrue(result)
        coVerify { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item1") }
    }

    @Test
    fun `toggleLearningLibraryItemIsBookmarked returns new bookmark state false`() = runTest {
        coEvery { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item2") } returns false
        val repository = getRepository()

        val result = repository.toggleLearningLibraryItemIsBookmarked("item2")

        assertFalse(result)
        coVerify { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item2") }
    }

    @Test
    fun `enrollLearningLibraryItem returns updated item`() = runTest {
        val enrolledItem = createTestCollectionItem(
            id = "item1",
            courseName = "Course 1 Enrolled",
            courseId = "1",
            isBookmarked = false,
            itemType = CollectionItemType.COURSE,
            isEnrolledInCanvas = true
        )
        coEvery { getLearningLibraryManager.enrollLearningLibraryItem("item1") } returns enrolledItem
        val repository = getRepository()

        val result = repository.enrollLearningLibraryItem("item1")

        assertEquals(enrolledItem, result)
        assertEquals("Course 1 Enrolled", result.canvasCourse?.courseName)
        coVerify { getLearningLibraryManager.enrollLearningLibraryItem("item1") }
    }

    private fun getRepository(): LearnLearningLibraryDetailsRepository {
        return LearnLearningLibraryDetailsRepository(getLearningLibraryManager)
    }

    private fun createTestCollectionItem(
        id: String,
        courseName: String,
        courseId: String,
        isBookmarked: Boolean,
        itemType: CollectionItemType,
        isEnrolledInCanvas: Boolean = false
    ): LearningLibraryCollectionItem = LearningLibraryCollectionItem(
        id = id,
        itemType = itemType,
        isBookmarked = isBookmarked,
        completionPercentage = 0.0,
        isEnrolledInCanvas = isEnrolledInCanvas,
        canvasCourse = CanvasCourseInfo(
            courseId = courseId,
            courseName = courseName,
            courseImageUrl = "https://example.com/$courseId.png",
            estimatedDurationMinutes = 60.0,
            moduleCount = 5.0,
            moduleItemCount = 10.0,
            canvasUrl = "https://example.com",
        ),
        libraryId = "library1",
        displayOrder = 1.0,
        programId = null,
        programCourseId = null,
        createdAt = Date(),
        updatedAt = Date()
    )
}

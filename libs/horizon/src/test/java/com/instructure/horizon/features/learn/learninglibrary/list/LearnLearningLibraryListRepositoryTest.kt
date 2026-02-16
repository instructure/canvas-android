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
package com.instructure.horizon.features.learn.learninglibrary.list

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollectionsResponse
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

class LearnLearningLibraryListRepositoryTest {
    private val getLearningLibraryManager: GetLearningLibraryManager = mockk(relaxed = true)

    private val testCollections = listOf(
        EnrolledLearningLibraryCollection(
            id = "collection1",
            name = "Software Engineering",
            items = listOf(
                createTestCollectionItem(
                    id = "item1",
                    courseName = "Intro to Programming",
                    courseId = "1",
                    isBookmarked = false,
                    itemType = CollectionItemType.COURSE
                ),
                createTestCollectionItem(
                    id = "item2",
                    courseName = "Advanced Algorithms",
                    courseId = "2",
                    isBookmarked = true,
                    itemType = CollectionItemType.PAGE
                )
            ),
            publicName = "",
            description = "",
            createdAt = Date(),
            updatedAt = Date()
        ),
        EnrolledLearningLibraryCollection(
            id = "collection2",
            name = "Data Science",
            items = listOf(
                createTestCollectionItem(
                    id = "item3",
                    courseName = "Machine Learning",
                    courseId = "3",
                    isBookmarked = false,
                    itemType = CollectionItemType.COURSE
                )
            ),
            publicName = "",
            description = "",
            createdAt = Date(),
            updatedAt = Date()
        )
    )

    @Before
    fun setup() {
        val response = EnrolledLearningLibraryCollectionsResponse(
            collections = testCollections
        )
        coEvery { getLearningLibraryManager.getEnrolledLearningLibraryCollections(any(), any()) } returns response
    }

    @Test
    fun `getEnrolledLearningLibraries returns list of collections`() = runTest {
        val repository = getRepository()
        val result = repository.getEnrolledLearningLibraries(false)

        assertEquals(2, result.size)
        assertEquals(testCollections, result)
        coVerify { getLearningLibraryManager.getEnrolledLearningLibraryCollections(4, false) }
    }

    @Test
    fun `getEnrolledLearningLibraries with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getEnrolledLearningLibraries(true)

        coVerify { getLearningLibraryManager.getEnrolledLearningLibraryCollections(4, true) }
    }

    @Test
    fun `getEnrolledLearningLibraries returns empty list when no collections`() = runTest {
        val emptyResponse = EnrolledLearningLibraryCollectionsResponse(collections = emptyList())
        coEvery { getLearningLibraryManager.getEnrolledLearningLibraryCollections(any(), any()) } returns emptyResponse
        val repository = getRepository()
        val result = repository.getEnrolledLearningLibraries(false)

        assertEquals(0, result.size)
    }

    @Test
    fun `toggleLearningLibraryItemIsBookmarked returns new bookmark state`() = runTest {
        coEvery { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item1") } returns true
        val repository = getRepository()
        val result = repository.toggleLearningLibraryItemIsBookmarked("item1")

        assertTrue(result)
        coVerify { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item1") }
    }

    @Test
    fun `toggleLearningLibraryItemIsBookmarked returns false when unbookmarking`() = runTest {
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
            courseName = "Intro to Programming",
            courseId = "1",
            isBookmarked = false,
            itemType = CollectionItemType.COURSE,
            isEnrolledInCanvas = true
        )
        coEvery { getLearningLibraryManager.enrollLearningLibraryItem("item1") } returns enrolledItem
        val repository = getRepository()
        val result = repository.enrollLearningLibraryItem("item1")

        assertEquals(enrolledItem, result)
        coVerify { getLearningLibraryManager.enrollLearningLibraryItem("item1") }
    }

    private fun getRepository(): LearnLearningLibraryListRepository {
        return LearnLearningLibraryListRepository(getLearningLibraryManager)
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
            moduleItemCount = 5.0,
            canvasUrl = "",
        ),
        libraryId = "",
        displayOrder = 1.0,
        programId = "",
        programCourseId = "",
        createdAt = Date(),
        updatedAt = Date()
    )
}
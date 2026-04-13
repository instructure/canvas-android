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
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.horizon.data.repository.LearnLearningLibraryRepository
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
    private val learningLibraryRepository: LearnLearningLibraryRepository = mockk(relaxed = true)

    private val testCollections = listOf(
        EnrolledLearningLibraryCollection(
            id = "collection1",
            name = "Software Engineering",
            totalItemCount = 2,
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
            totalItemCount = 1,
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

    private val emptyPageInfo = LearningLibraryPageInfo(
        nextCursor = null,
        previousCursor = null,
        hasNextPage = false,
        hasPreviousPage = false,
        totalCount = 10,
        pageCursors = null
    )

    @Before
    fun setup() {
        coEvery { learningLibraryRepository.getEnrolledLearningLibraries(any(), any()) } returns testCollections
        coEvery { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = emptyList(),
            pageInfo = emptyPageInfo
        )
    }

    @Test
    fun `getEnrolledLearningLibraries returns list of collections`() = runTest {
        val repository = getRepository()
        val result = repository.getEnrolledLearningLibraries()

        assertEquals(2, result.size)
        assertEquals(testCollections, result)
        coVerify { learningLibraryRepository.getEnrolledLearningLibraries(4, false) }
    }

    @Test
    fun `getEnrolledLearningLibraries with forceRefresh passes true to data layer`() = runTest {
        val repository = getRepository()

        repository.getEnrolledLearningLibraries(forceRefresh = true)

        coVerify { learningLibraryRepository.getEnrolledLearningLibraries(4, true) }
    }

    @Test
    fun `getEnrolledLearningLibraries returns empty list when no collections`() = runTest {
        coEvery { learningLibraryRepository.getEnrolledLearningLibraries(any(), any()) } returns emptyList()
        val repository = getRepository()
        val result = repository.getEnrolledLearningLibraries()

        assertEquals(0, result.size)
    }

    @Test
    fun `getLearningLibraryItems returns items with no filters`() = runTest {
        val items = listOf(createTestCollectionItem("item1", "Python", "1", false, CollectionItemType.COURSE))
        coEvery { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = items,
            pageInfo = emptyPageInfo
        )
        val repository = getRepository()

        val result = repository.getLearningLibraryItems()

        assertEquals(1, result.items.size)
        assertEquals(items, result.items)
    }

    @Test
    fun `getLearningLibraryItems with cursor passes cursor to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(afterCursor = "cursor123")

        coVerify { learningLibraryRepository.getLearningLibraryItems(cursor = "cursor123", any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getLearningLibraryItems with searchQuery passes searchQuery to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(searchQuery = "python")

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), searchQuery = "python", any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getLearningLibraryItems with typeFilter passes typeFilter to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(typeFilter = CollectionItemType.COURSE)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), typeFilter = CollectionItemType.COURSE, any(), any(), any(), any()) }
    }

    @Test
    fun `getLearningLibraryItems with null typeFilter passes null to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(typeFilter = null)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), typeFilter = null, any(), any(), any(), any()) }
    }

    @Test
    fun `getLearningLibraryItems with bookmarkedOnly passes bookmarkedOnly to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(bookmarkedOnly = true)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), bookmarkedOnly = true, any(), any(), any()) }
    }

    @Test
    fun `getLearningLibraryItems with completedOnly passes completedOnly to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(completedOnly = true)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), any(), completedOnly = true, any(), any()) }
    }

    @Test
    fun `getLearningLibraryItems returns pagination info`() = runTest {
        val pageInfo = LearningLibraryPageInfo(
            nextCursor = "next_cursor",
            previousCursor = null,
            hasNextPage = true,
            hasPreviousPage = false,
            totalCount = 10,
            pageCursors = null
        )
        coEvery { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = emptyList(),
            pageInfo = pageInfo
        )
        val repository = getRepository()

        val result = repository.getLearningLibraryItems()

        assertTrue(result.pageInfo.hasNextPage)
        assertEquals("next_cursor", result.pageInfo.nextCursor)
    }

    @Test
    fun `getLearningLibraryItems with limit passes limit to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(limit = 5)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), limit = 5, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getLearningLibraryItems with sortBy passes sortBy to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(sortBy = CollectionItemSortOption.NAME_A_Z)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), sortBy = CollectionItemSortOption.NAME_A_Z, any()) }
    }

    @Test
    fun `getLearningLibraryItems with null sortBy passes null sortBy to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(sortBy = null)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), sortBy = null, any()) }
    }

    @Test
    fun `getLearningLibraryItems with forceRefresh passes forceRefresh true to data layer`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryItems(forceRefresh = true)

        coVerify { learningLibraryRepository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), forceRefresh = true) }
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
    fun `getLearningLibraryRecommendedItems passes forceRefresh false to manager`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryRecommendedItems(forceRefresh = false)

        coVerify { getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork = false) }
    }

    @Test
    fun `getLearningLibraryRecommendedItems passes forceRefresh true to manager`() = runTest {
        val repository = getRepository()

        repository.getLearningLibraryRecommendedItems(forceRefresh = true)

        coVerify { getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork = true) }
    }

    private fun getRepository(): LearnLearningLibraryListRepository {
        return LearnLearningLibraryListRepository(learningLibraryRepository, getLearningLibraryManager)
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
        updatedAt = Date(),
        moduleInfo = null,
        canvasEnrollmentId = null
    )
}

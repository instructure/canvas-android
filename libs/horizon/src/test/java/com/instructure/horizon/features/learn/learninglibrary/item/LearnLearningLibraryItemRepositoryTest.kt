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
package com.instructure.horizon.features.learn.learninglibrary.item

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class LearnLearningLibraryItemRepositoryTest {
    private val getLearningLibraryManager: GetLearningLibraryManager = mockk(relaxed = true)

    @Test
    fun `getLearningLibraryItems delegates to manager with default parameters`() = runTest {
        val expectedResponse = createTestResponse(emptyList())
        coEvery {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = null,
                limit = 10,
                bookmarkedOnly = false,
                completedOnly = false,
                searchTerm = null,
                types = null,
                forceNetwork = false
            )
        } returns expectedResponse

        val result = getRepository().getLearningLibraryItems(forceNetwork = false)

        assertEquals(expectedResponse, result)
        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = null,
                limit = 10,
                bookmarkedOnly = false,
                completedOnly = false,
                searchTerm = null,
                types = null,
                forceNetwork = false
            )
        }
    }

    @Test
    fun `getLearningLibraryItems passes afterCursor to manager`() = runTest {
        getRepository().getLearningLibraryItems(afterCursor = "cursor123", forceNetwork = false)

        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = eq("cursor123"),
                limit = any(),
                bookmarkedOnly = any(),
                completedOnly = any(),
                searchTerm = any(),
                types = any(),
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `getLearningLibraryItems passes searchQuery as searchTerm to manager`() = runTest {
        getRepository().getLearningLibraryItems(searchQuery = "python", forceNetwork = false)

        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = any(),
                limit = any(),
                bookmarkedOnly = any(),
                completedOnly = any(),
                searchTerm = eq("python"),
                types = any(),
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `getLearningLibraryItems wraps typeFilter in single-element list`() = runTest {
        getRepository().getLearningLibraryItems(typeFilter = CollectionItemType.PAGE, forceNetwork = false)

        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = any(),
                limit = any(),
                bookmarkedOnly = any(),
                completedOnly = any(),
                searchTerm = any(),
                types = eq(listOf(CollectionItemType.PAGE)),
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `getLearningLibraryItems passes null types when typeFilter is null`() = runTest {
        getRepository().getLearningLibraryItems(typeFilter = null, forceNetwork = false)

        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = any(),
                limit = any(),
                bookmarkedOnly = any(),
                completedOnly = any(),
                searchTerm = any(),
                types = null,
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `getLearningLibraryItems passes bookmarkedOnly true to manager`() = runTest {
        getRepository().getLearningLibraryItems(bookmarkedOnly = true, forceNetwork = false)

        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = any(),
                limit = any(),
                bookmarkedOnly = eq(true),
                completedOnly = any(),
                searchTerm = any(),
                types = any(),
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `getLearningLibraryItems passes completedOnly true to manager`() = runTest {
        getRepository().getLearningLibraryItems(completedOnly = true, forceNetwork = false)

        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = any(),
                limit = any(),
                bookmarkedOnly = any(),
                completedOnly = eq(true),
                searchTerm = any(),
                types = any(),
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `getLearningLibraryItems passes forceNetwork true to manager`() = runTest {
        getRepository().getLearningLibraryItems(forceNetwork = true)

        coVerify {
            getLearningLibraryManager.getLearningLibraryCollectionItems(
                cursor = any(),
                limit = any(),
                bookmarkedOnly = any(),
                completedOnly = any(),
                searchTerm = any(),
                types = any(),
                forceNetwork = eq(true)
            )
        }
    }

    @Test
    fun `getLearningLibraryItems returns response with items`() = runTest {
        val items = listOf(createTestCollectionItem(id = "item1"), createTestCollectionItem(id = "item2"))
        val response = createTestResponse(items)
        coEvery { getLearningLibraryManager.getLearningLibraryCollectionItems(any(), any(), any(), any(), any(), any(), any()) } returns response

        val result = getRepository().getLearningLibraryItems(forceNetwork = false)

        assertEquals(2, result.items.size)
        assertEquals("item1", result.items[0].id)
        assertEquals("item2", result.items[1].id)
    }

    @Test
    fun `getLearningLibraryItems returns response with pagination info`() = runTest {
        val response = createTestResponse(emptyList(), hasNextPage = true, nextCursor = "next123")
        coEvery { getLearningLibraryManager.getLearningLibraryCollectionItems(any(), any(), any(), any(), any(), any(), any()) } returns response

        val result = getRepository().getLearningLibraryItems(forceNetwork = false)

        assertTrue(result.pageInfo.hasNextPage)
        assertEquals("next123", result.pageInfo.nextCursor)
    }

    @Test
    fun `toggleLearningLibraryItemIsBookmarked returns true when bookmarking`() = runTest {
        coEvery { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item1") } returns true

        val result = getRepository().toggleLearningLibraryItemIsBookmarked("item1")

        assertTrue(result)
        coVerify { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item1") }
    }

    @Test
    fun `toggleLearningLibraryItemIsBookmarked returns false when unbookmarking`() = runTest {
        coEvery { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item2") } returns false

        val result = getRepository().toggleLearningLibraryItemIsBookmarked("item2")

        assertFalse(result)
        coVerify { getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked("item2") }
    }

    @Test
    fun `enrollLearningLibraryItem returns updated enrolled item`() = runTest {
        val enrolledItem = createTestCollectionItem(id = "item1", isEnrolledInCanvas = true)
        coEvery { getLearningLibraryManager.enrollLearningLibraryItem("item1") } returns enrolledItem

        val result = getRepository().enrollLearningLibraryItem("item1")

        assertEquals(enrolledItem, result)
        coVerify { getLearningLibraryManager.enrollLearningLibraryItem("item1") }
    }

    private fun getRepository() = LearnLearningLibraryItemRepository(getLearningLibraryManager)

    private fun createTestResponse(
        items: List<LearningLibraryCollectionItem>,
        hasNextPage: Boolean = false,
        nextCursor: String? = null
    ) = LearningLibraryCollectionItemsResponse(
        items = items,
        pageInfo = LearningLibraryPageInfo(
            nextCursor = nextCursor,
            previousCursor = null,
            hasNextPage = hasNextPage,
            hasPreviousPage = false
        )
    )

    private fun createTestCollectionItem(
        id: String = "testItem",
        courseId: String = "1",
        courseName: String = "Test Course",
        isBookmarked: Boolean = false,
        itemType: CollectionItemType = CollectionItemType.COURSE,
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

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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollectionsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.journey.type.CollectionSortMode
import java.util.Date

class FakeGetLearningLibraryManager : GetLearningLibraryManager {

    private val learningLibraryCollections = mutableListOf<LearningLibraryCollection>()
    private val enrolledCollections = mutableListOf<EnrolledLearningLibraryCollection>()
    private val collectionItems = mutableMapOf<String, MutableList<LearningLibraryCollectionItem>>()

    init {
        initializeMockData()
    }

    override suspend fun getLearningLibraryCollections(
        cursor: String?,
        limit: Int?,
        forward: Boolean?,
        search: String?,
        sortMode: CollectionSortMode?,
        forceNetwork: Boolean
    ): LearningLibraryCollectionResponse {
        var filteredCollections = learningLibraryCollections

        if (!search.isNullOrBlank()) {
            filteredCollections = filteredCollections.filter {
                it.name.contains(search, ignoreCase = true) ||
                it.description?.contains(search, ignoreCase = true) == true
            }.toMutableList()
        }

        return LearningLibraryCollectionResponse(
            learningLibraryCollections = filteredCollections,
            pageInfo = LearningLibraryPageInfo(
                nextCursor = null,
                previousCursor = null,
                hasNextPage = false,
                hasPreviousPage = false
            )
        )
    }

    override suspend fun getLearningLibraryCollectionItems(
        cursor: String?,
        limit: Int?,
        forward: Boolean?,
        bookmarkedOnly: Boolean?,
        searchTerm: String?,
        types: List<CollectionItemType>?,
        completedOnly: Boolean?,
        forceNetwork: Boolean
    ): LearningLibraryCollectionItemsResponse {
        var items = collectionItems.values.flatten()

        if (bookmarkedOnly == true) {
            items = items.filter { it.isBookmarked }
        }

        if (!searchTerm.isNullOrBlank()) {
            items = items.filter {
                it.canvasCourse?.courseName?.contains(searchTerm, ignoreCase = true) == true
            }
        }

        if (!types.isNullOrEmpty()) {
            items = items.filter { it.itemType in types }
        }

        if (completedOnly == true) {
            items = items.filter { it.completionPercentage == 100.0 }
        }

        return LearningLibraryCollectionItemsResponse(
            items = items,
            pageInfo = LearningLibraryPageInfo(
                nextCursor = null,
                previousCursor = null,
                hasNextPage = false,
                hasPreviousPage = false
            )
        )
    }

    override suspend fun getEnrolledLearningLibraryCollections(
        itemLimitPerCollection: Int?,
        forceNetwork: Boolean
    ): EnrolledLearningLibraryCollectionsResponse {
        val collections = enrolledCollections.map { collection ->
            val items = if (itemLimitPerCollection != null) {
                collection.items.take(itemLimitPerCollection)
            } else {
                collection.items
            }

            collection.copy(items = items)
        }

        return EnrolledLearningLibraryCollectionsResponse(
            collections = collections
        )
    }

    override suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        collectionItems.values.flatten().find { it.id == itemId }?.let { item ->
            val updatedItem = item.copy(isBookmarked = !item.isBookmarked)

            // Update in collectionItems
            collectionItems.forEach { (collectionId, items) ->
                val index = items.indexOfFirst { it.id == itemId }
                if (index >= 0) {
                    items[index] = updatedItem
                }
            }

            // Update in enrolledCollections
            enrolledCollections.forEachIndexed { collectionIndex, collection ->
                val itemIndex = collection.items.indexOfFirst { it.id == itemId }
                if (itemIndex >= 0) {
                    val updatedItems = collection.items.toMutableList()
                    updatedItems[itemIndex] = updatedItem
                    enrolledCollections[collectionIndex] = collection.copy(items = updatedItems)
                }
            }

            return updatedItem.isBookmarked
        }

        return false
    }

    override suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        val item = collectionItems.values.flatten().find { it.id == itemId }
            ?: throw IllegalArgumentException("Item not found: $itemId")

        val updatedItem = item.copy(
            isEnrolledInCanvas = true,
            completionPercentage = 0.0
        )

        // Update in collectionItems
        collectionItems.forEach { (collectionId, items) ->
            val index = items.indexOfFirst { it.id == itemId }
            if (index >= 0) {
                items[index] = updatedItem
            }
        }

        // Update in enrolledCollections
        enrolledCollections.forEachIndexed { collectionIndex, collection ->
            val itemIndex = collection.items.indexOfFirst { it.id == itemId }
            if (itemIndex >= 0) {
                val updatedItems = collection.items.toMutableList()
                updatedItems[itemIndex] = updatedItem
                enrolledCollections[collectionIndex] = collection.copy(items = updatedItems)
            }
        }

        return updatedItem
    }

    private fun initializeMockData() {
        val now = Date()
        val courses = MockCanvas.data?.courses?.values?.toList() ?: emptyList()

        // Create Learning Library Collections
        val collection1 = LearningLibraryCollection(
            id = "collection1",
            name = "Introduction to Programming",
            publicName = "Programming Basics",
            description = "Learn the fundamentals of programming",
            rootAccountUuid = "root-account-1",
            accountId = "account-1",
            deletedAt = null,
            createdAt = now,
            updatedAt = now
        )

        val collection2 = LearningLibraryCollection(
            id = "collection2",
            name = "Advanced Web Development",
            publicName = "Web Development",
            description = "Master modern web technologies",
            rootAccountUuid = "root-account-1",
            accountId = "account-1",
            deletedAt = null,
            createdAt = now,
            updatedAt = now
        )

        val collection3 = LearningLibraryCollection(
            id = "collection3",
            name = "Data Science Fundamentals",
            publicName = "Data Science",
            description = "Introduction to data science and analytics",
            rootAccountUuid = "root-account-1",
            accountId = "account-1",
            deletedAt = null,
            createdAt = now,
            updatedAt = now
        )

        learningLibraryCollections.addAll(listOf(collection1, collection2, collection3))

        // Create Collection Items
        val items1 = mutableListOf<LearningLibraryCollectionItem>()
        if (courses.isNotEmpty()) {
            items1.add(
                LearningLibraryCollectionItem(
                    id = "item1",
                    libraryId = "collection1",
                    itemType = CollectionItemType.COURSE,
                    displayOrder = 1.0,
                    canvasCourse = CanvasCourseInfo(
                        courseId = courses[0].id.toString(),
                        canvasUrl = "https://mock-data.instructure.com/courses/${courses[0].id}",
                        courseName = "Python Basics",
                        courseImageUrl = "https://example.com/python.png",
                        moduleCount = 5.0,
                        moduleItemCount = 25.0,
                        estimatedDurationMinutes = 300.0
                    ),
                    programId = null,
                    programCourseId = null,
                    createdAt = now,
                    updatedAt = now,
                    isBookmarked = false,
                    completionPercentage = 0.0,
                    isEnrolledInCanvas = true
                )
            )

            items1.add(
                LearningLibraryCollectionItem(
                    id = "item2",
                    libraryId = "collection1",
                    itemType = CollectionItemType.PAGE,
                    displayOrder = 2.0,
                    canvasCourse = null,
                    programId = null,
                    programCourseId = null,
                    createdAt = now,
                    updatedAt = now,
                    isBookmarked = true,
                    completionPercentage = null,
                    isEnrolledInCanvas = null
                )
            )
        }

        val items2 = mutableListOf<LearningLibraryCollectionItem>()
        if (courses.size > 1) {
            items2.add(
                LearningLibraryCollectionItem(
                    id = "item3",
                    libraryId = "collection2",
                    itemType = CollectionItemType.COURSE,
                    displayOrder = 1.0,
                    canvasCourse = CanvasCourseInfo(
                        courseId = courses[1].id.toString(),
                        canvasUrl = "https://mock-data.instructure.com/courses/${courses[1].id}",
                        courseName = "React Advanced",
                        courseImageUrl = "https://example.com/react.png",
                        moduleCount = 8.0,
                        moduleItemCount = 40.0,
                        estimatedDurationMinutes = 480.0
                    ),
                    programId = null,
                    programCourseId = null,
                    createdAt = now,
                    updatedAt = now,
                    isBookmarked = true,
                    completionPercentage = 50.0,
                    isEnrolledInCanvas = true
                )
            )
        }

        val items3 = mutableListOf<LearningLibraryCollectionItem>()
        if (courses.size > 2) {
            items3.add(
                LearningLibraryCollectionItem(
                    id = "item4",
                    libraryId = "collection3",
                    itemType = CollectionItemType.COURSE,
                    displayOrder = 1.0,
                    canvasCourse = CanvasCourseInfo(
                        courseId = courses[2].id.toString(),
                        canvasUrl = "https://mock-data.instructure.com/courses/${courses[2].id}",
                        courseName = "Machine Learning",
                        courseImageUrl = "https://example.com/ml.png",
                        moduleCount = 10.0,
                        moduleItemCount = 50.0,
                        estimatedDurationMinutes = 600.0
                    ),
                    programId = null,
                    programCourseId = null,
                    createdAt = now,
                    updatedAt = now,
                    isBookmarked = false,
                    completionPercentage = 100.0,
                    isEnrolledInCanvas = true
                )
            )

            items3.add(
                LearningLibraryCollectionItem(
                    id = "item5",
                    libraryId = "collection3",
                    itemType = CollectionItemType.EXTERNAL_URL,
                    displayOrder = 2.0,
                    canvasCourse = null,
                    programId = null,
                    programCourseId = null,
                    createdAt = now,
                    updatedAt = now,
                    isBookmarked = false,
                    completionPercentage = null,
                    isEnrolledInCanvas = null
                )
            )
        }

        collectionItems["collection1"] = items1
        collectionItems["collection2"] = items2
        collectionItems["collection3"] = items3

        // Create Enrolled Collections
        enrolledCollections.add(
            EnrolledLearningLibraryCollection(
                id = collection1.id,
                name = collection1.name,
                publicName = collection1.publicName,
                description = collection1.description,
                createdAt = collection1.createdAt,
                updatedAt = collection1.updatedAt,
                items = items1
            )
        )

        enrolledCollections.add(
            EnrolledLearningLibraryCollection(
                id = collection2.id,
                name = collection2.name,
                publicName = collection2.publicName,
                description = collection2.description,
                createdAt = collection2.createdAt,
                updatedAt = collection2.updatedAt,
                items = items2
            )
        )

        enrolledCollections.add(
            EnrolledLearningLibraryCollection(
                id = collection3.id,
                name = collection3.name,
                publicName = collection3.publicName,
                description = collection3.description,
                createdAt = collection3.createdAt,
                updatedAt = collection3.updatedAt,
                items = items3
            )
        )
    }
}

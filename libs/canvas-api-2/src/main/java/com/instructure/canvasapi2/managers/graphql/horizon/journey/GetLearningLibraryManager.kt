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
package com.instructure.canvasapi2.managers.graphql.horizon.journey

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.di.JourneyApolloClient
import com.instructure.canvasapi2.enqueueMutation
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollectionsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.toApolloType
import com.instructure.journey.EnrollLearningLibraryItemMutation
import com.instructure.journey.GetEnrolledLearningLibraryCollectionQuery
import com.instructure.journey.GetEnrolledLearningLibraryCollectionsQuery
import com.instructure.journey.GetLearningLibraryCollectionItemsQuery
import com.instructure.journey.GetLearningLibraryCollectionsQuery
import com.instructure.journey.ToggleLearningLibraryItemIsBookmarkedMutation
import com.instructure.journey.type.CollectionSortMode
import com.instructure.journey.type.EnrollLearnerInCollectionItemInput
import com.instructure.journey.type.ToggleCollectionItemBookmarkInput
import javax.inject.Inject

interface GetLearningLibraryManager {
    suspend fun getLearningLibraryCollections(
        cursor: String? = null,
        limit: Int? = null,
        forward: Boolean? = null,
        search: String? = null,
        sortMode: CollectionSortMode? = null,
        forceNetwork: Boolean = false
    ): LearningLibraryCollectionResponse

    suspend fun getLearningLibraryCollectionItems(
        cursor: String? = null,
        limit: Int? = null,
        forward: Boolean? = null,
        bookmarkedOnly: Boolean? = null,
        searchTerm: String? = null,
        types: List<CollectionItemType>? = null,
        completedOnly: Boolean? = null,
        forceNetwork: Boolean = false
    ): LearningLibraryCollectionItemsResponse

    suspend fun getEnrolledLearningLibraryCollections(
        itemLimitPerCollection: Int? = null,
        forceNetwork: Boolean = false
    ): EnrolledLearningLibraryCollectionsResponse

    suspend fun getEnrolledLearningLibraryCollection(
        id: String,
        forceNetwork: Boolean = false
    ): EnrolledLearningLibraryCollection

    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean

    suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem
}

class GetLearningLibraryManagerImpl @Inject constructor(
    @JourneyApolloClient private val journeyClient: ApolloClient,
): GetLearningLibraryManager {
    override suspend fun getLearningLibraryCollections(
        cursor: String?,
        limit: Int?,
        forward: Boolean?,
        search: String?,
        sortMode: CollectionSortMode?,
        forceNetwork: Boolean
    ): LearningLibraryCollectionResponse {
        val query = GetLearningLibraryCollectionsQuery(
            Optional.presentIfNotNull(cursor),
            Optional.presentIfNotNull(limit),
            Optional.presentIfNotNull(forward),
            Optional.presentIfNotNull(search),
            Optional.presentIfNotNull(sortMode)
        )
        val result = journeyClient.enqueueQuery(query, forceNetwork = forceNetwork).dataOrThrow().learningLibraryCollections

        return LearningLibraryCollectionResponse(
            learningLibraryCollections = result.collections.map { collection ->
                LearningLibraryCollection(
                    id = collection.id,
                    name = collection.name,
                    publicName = collection.publicName,
                    description = collection.description,
                    rootAccountUuid = collection.rootAccountUuid,
                    accountId = collection.accountId,
                    deletedAt = collection.deletedAt,
                    createdAt = collection.createdAt,
                    updatedAt = collection.updatedAt
                )
            },
            pageInfo = LearningLibraryPageInfo(
                nextCursor = result.pageInfo.nextCursor,
                previousCursor = result.pageInfo.previousCursor,
                hasNextPage = result.pageInfo.hasNextPage,
                hasPreviousPage = result.pageInfo.hasPreviousPage
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
        val query = GetLearningLibraryCollectionItemsQuery(
            Optional.presentIfNotNull(cursor),
            Optional.presentIfNotNull(limit),
            Optional.presentIfNotNull(forward),
            Optional.presentIfNotNull(bookmarkedOnly),
            Optional.presentIfNotNull(searchTerm),
            Optional.presentIfNotNull(types?.map { it.toApolloType() }),
            Optional.presentIfNotNull(completedOnly)
        )
        val result = journeyClient.enqueueQuery(query, forceNetwork = forceNetwork).dataOrThrow().learningLibraryCollectionItems

        return LearningLibraryCollectionItemsResponse(
            items = result.items.map { item ->
                LearningLibraryCollectionItem(
                    id = item.id,
                    libraryId = item.libraryId,
                    itemType = CollectionItemType.valueOf(item.itemType.name),
                    displayOrder = item.displayOrder,
                    canvasCourse = item.canvasCourse?.let { course ->
                        CanvasCourseInfo(
                            courseId = course.courseId,
                            canvasUrl = course.canvasUrl,
                            courseName = course.courseName,
                            courseImageUrl = course.courseImageUrl,
                            moduleCount = course.moduleCount,
                            moduleItemCount = course.moduleItemCount,
                            estimatedDurationMinutes = course.estimatedDurationMinutes
                        )
                    },
                    programId = item.programId,
                    programCourseId = item.programCourseId,
                    createdAt = item.createdAt,
                    updatedAt = item.updatedAt,
                    isBookmarked = item.isBookmarked,
                    completionPercentage = item.completionPercentage,
                    isEnrolledInCanvas = item.isEnrolledInCanvas
                )
            },
            pageInfo = LearningLibraryPageInfo(
                nextCursor = result.pageInfo.nextCursor,
                previousCursor = result.pageInfo.previousCursor,
                hasNextPage = result.pageInfo.hasNextPage,
                hasPreviousPage = result.pageInfo.hasPreviousPage
            )
        )
    }

    override suspend fun getEnrolledLearningLibraryCollections(
        itemLimitPerCollection: Int?,
        forceNetwork: Boolean
    ): EnrolledLearningLibraryCollectionsResponse {
        val query = GetEnrolledLearningLibraryCollectionsQuery(
            Optional.presentIfNotNull(itemLimitPerCollection)
        )
        val result = journeyClient.enqueueQuery(query, forceNetwork = forceNetwork).dataOrThrow().enrolledLearningLibraryCollections

        return EnrolledLearningLibraryCollectionsResponse(
            collections = result.collections.map { collection ->
                EnrolledLearningLibraryCollection(
                    id = collection.id,
                    name = collection.name,
                    publicName = collection.publicName,
                    description = collection.description,
                    createdAt = collection.createdAt,
                    updatedAt = collection.updatedAt,
                    items = collection.items.map { item ->
                        LearningLibraryCollectionItem(
                            id = item.id,
                            libraryId = item.libraryId,
                            itemType = CollectionItemType.valueOf(item.itemType.name),
                            displayOrder = item.displayOrder,
                            canvasCourse = item.canvasCourse?.let { course ->
                                CanvasCourseInfo(
                                    courseId = course.courseId,
                                    canvasUrl = course.canvasUrl,
                                    courseName = course.courseName,
                                    courseImageUrl = course.courseImageUrl,
                                    moduleCount = course.moduleCount,
                                    moduleItemCount = course.moduleItemCount,
                                    estimatedDurationMinutes = course.estimatedDurationMinutes
                                )
                            },
                            programId = item.programId,
                            programCourseId = item.programCourseId,
                            createdAt = item.createdAt,
                            updatedAt = item.updatedAt,
                            isBookmarked = item.isBookmarked,
                            completionPercentage = item.completionPercentage,
                            isEnrolledInCanvas = item.isEnrolledInCanvas
                        )
                    }
                )
            }
        )
    }

    override suspend fun getEnrolledLearningLibraryCollection(
        id: String,
        forceNetwork: Boolean
    ): EnrolledLearningLibraryCollection {
        val query = GetEnrolledLearningLibraryCollectionQuery(id)
        val result = journeyClient.enqueueQuery(query, forceNetwork = forceNetwork).dataOrThrow().enrolledLearningLibraryCollection

        return EnrolledLearningLibraryCollection(
            id = result.id,
            name = result.name,
            publicName = result.publicName,
            description = result.description,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt,
            items = result.items.map { item ->
                LearningLibraryCollectionItem(
                    id = item.id,
                    libraryId = item.libraryId,
                    itemType = CollectionItemType.valueOf(item.itemType.name),
                    displayOrder = item.displayOrder,
                    canvasCourse = item.canvasCourse?.let { course ->
                        CanvasCourseInfo(
                            courseId = course.courseId,
                            canvasUrl = course.canvasUrl,
                            courseName = course.courseName,
                            courseImageUrl = course.courseImageUrl,
                            moduleCount = course.moduleCount,
                            moduleItemCount = course.moduleItemCount,
                            estimatedDurationMinutes = course.estimatedDurationMinutes
                        )
                    },
                    programId = item.programId,
                    programCourseId = item.programCourseId,
                    createdAt = item.createdAt,
                    updatedAt = item.updatedAt,
                    isBookmarked = item.isBookmarked,
                    completionPercentage = item.completionPercentage,
                    isEnrolledInCanvas = item.isEnrolledInCanvas
                )
            }
        )
    }

    override suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        val mutation = ToggleLearningLibraryItemIsBookmarkedMutation(
            ToggleCollectionItemBookmarkInput(itemId)
        )
        val result = journeyClient.enqueueMutation(mutation).dataOrThrow().toggleCollectionItemBookmark

        return result.isBookmarked
    }

    override suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        val mutation = EnrollLearningLibraryItemMutation(
            EnrollLearnerInCollectionItemInput(itemId)
        )
        val result = journeyClient.enqueueMutation(mutation).dataOrThrow().enrollLearnerInCollectionItem

        return LearningLibraryCollectionItem(
            id = result.item.id,
            libraryId = result.item.libraryId,
            itemType = CollectionItemType.valueOf(result.item.itemType.name),
            displayOrder = result.item.displayOrder,
            canvasCourse = result.item.canvasCourse?.let { course ->
                CanvasCourseInfo(
                    courseId = course.courseId,
                    canvasUrl = course.canvasUrl,
                    courseName = course.courseName,
                    courseImageUrl = course.courseImageUrl,
                    moduleCount = course.moduleCount,
                    moduleItemCount = course.moduleItemCount,
                    estimatedDurationMinutes = course.estimatedDurationMinutes
                )
            },
            programId = result.item.programId,
            programCourseId = result.item.programCourseId,
            createdAt = result.item.createdAt,
            updatedAt = result.item.updatedAt,
            isBookmarked = result.item.isBookmarked,
            completionPercentage = result.item.completionPercentage,
            isEnrolledInCanvas = result.item.isEnrolledInCanvas
        )
    }
}
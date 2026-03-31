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
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.toLearnItemSortOption
import com.instructure.canvasapi2.models.journey.mycontent.CourseEnrollmentItem
import com.instructure.canvasapi2.models.journey.mycontent.LearnItem
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import com.instructure.canvasapi2.models.journey.mycontent.ProgramEnrollmentItem
import com.instructure.journey.GetLearnItemsQuery
import javax.inject.Inject

interface MyContentManager {
    suspend fun getLearnItems(
        cursor: String? = null,
        limit: Int? = null,
        forward: Boolean? = null,
        searchTerm: String? = null,
        itemTypes: List<LearnItemType>? = null,
        status: List<LearnItemStatus>? = null,
        sortBy: CollectionItemSortOption? = null,
        forceNetwork: Boolean = false,
    ): LearnItemsResponse
}

class MyContentManagerImpl @Inject constructor(
    @JourneyApolloClient private val journeyClient: ApolloClient,
) : MyContentManager {
    override suspend fun getLearnItems(
        cursor: String?,
        limit: Int?,
        forward: Boolean?,
        searchTerm: String?,
        itemTypes: List<LearnItemType>?,
        status: List<LearnItemStatus>?,
        sortBy: CollectionItemSortOption?,
        forceNetwork: Boolean,
    ): LearnItemsResponse {
        val query = GetLearnItemsQuery(
            cursor = Optional.presentIfNotNull(cursor),
            limit = Optional.presentIfNotNull(limit),
            forward = Optional.presentIfNotNull(forward),
            searchTerm = Optional.presentIfNotNull(searchTerm),
            itemTypes = Optional.presentIfNotNull(itemTypes?.map { it.toApolloType() }),
            status = Optional.presentIfNotNull(status?.map { it.toApolloType() }),
            sortBy = Optional.presentIfNotNull(sortBy?.toLearnItemSortOption()),
        )
        val result = journeyClient.enqueueQuery(query, forceNetwork = forceNetwork).dataOrThrow().learnItems

        return LearnItemsResponse(
            items = result.items.mapNotNull { item ->
                item.onProgramEnrollmentItemGQL?.let { program ->
                    ProgramEnrollmentItem(
                        id = program.id,
                        name = program.name,
                        position = program.position,
                        enrolledAt = program.enrolledAt,
                        completionPercentage = program.completionPercentage,
                        startDate = program.startDate,
                        endDate = program.endDate,
                        status = program.status,
                        description = program.description,
                        variant = program.variant,
                        estimatedDurationMinutes = program.estimatedDurationMinutes,
                        courseCount = program.courseCount,
                    )
                } ?: item.onCourseEnrollmentItemGQL?.let { course ->
                    CourseEnrollmentItem(
                        id = course.id,
                        name = course.name,
                        position = course.position,
                        enrolledAt = course.enrolledAt,
                        completionPercentage = course.completionPercentage,
                        startAt = course.startAt,
                        endAt = course.endAt,
                        requirementCount = course.requirementCount,
                        requirementCompletedCount = course.requirementCompletedCount,
                        completedAt = course.completedAt,
                        grade = course.grade,
                        imageUrl = course.imageUrl,
                        workflowState = course.workflowState,
                        lastActivityAt = course.lastActivityAt,
                    )
                }
            },
            pageInfo = LearningLibraryPageInfo(
                nextCursor = result.pageInfo.nextCursor,
                previousCursor = result.pageInfo.previousCursor,
                hasNextPage = result.pageInfo.hasNextPage,
                hasPreviousPage = result.pageInfo.hasPreviousPage,
                totalCount = result.pageInfo.totalCount,
                pageCursors = null,
            ),
        )
    }
}
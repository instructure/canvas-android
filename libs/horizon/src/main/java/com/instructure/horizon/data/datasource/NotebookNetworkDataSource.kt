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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.LearningObjectFilter
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import com.instructure.redwood.type.OrderDirection
import javax.inject.Inject

class NotebookNetworkDataSource @Inject constructor(
    private val redwoodApiManager: RedwoodApiManager,
    private val horizonGetCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getNotes(
        after: String? = null,
        before: String? = null,
        itemCount: Int = DEFAULT_PAGE_SIZE,
        filterType: NotebookType? = null,
        courseId: Long? = null,
        objectTypeAndId: Pair<String, String>? = null,
        orderDirection: OrderDirection? = null,
        forceNetwork: Boolean = false,
    ): QueryNotesQuery.Notes {
        val filterInput = buildFilter(filterType, courseId, objectTypeAndId)
        val orderByInput = OrderByInput(direction = Optional.presentIfNotNull(orderDirection))

        return if (before != null) {
            redwoodApiManager.getNotes(
                lastN = itemCount,
                before = before,
                filter = filterInput,
                orderBy = orderByInput,
                forceNetwork = forceNetwork,
            )
        } else {
            redwoodApiManager.getNotes(
                firstN = itemCount,
                after = after,
                filter = filterInput,
                orderBy = orderByInput,
                forceNetwork = forceNetwork,
            )
        }
    }

    suspend fun getAllNotesForCourse(courseId: Long): List<QueryNotesQuery.Edge> {
        val all = mutableListOf<QueryNotesQuery.Edge>()
        var cursor: String? = null
        do {
            val page = redwoodApiManager.getNotes(
                filter = NoteFilterInput(courseId = Optional.present(courseId.toString())),
                firstN = SYNC_PAGE_SIZE,
                after = cursor,
                forceNetwork = true,
            )
            page.edges?.let { all.addAll(it) }
            cursor = if (page.pageInfo.hasNextPage) page.pageInfo.endCursor else null
        } while (cursor != null)
        return all
    }

    suspend fun getCourses(forceNetwork: Boolean): List<CourseWithProgress> {
        return horizonGetCoursesManager.getCoursesWithProgress(
            userId = apiPrefs.user?.id ?: 0L,
            forceNetwork = forceNetwork,
        ).dataOrNull.orEmpty()
    }

    suspend fun deleteNote(noteId: String) {
        redwoodApiManager.deleteNote(noteId)
    }

    suspend fun createNote(
        courseId: String,
        objectId: String,
        objectType: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData?,
    ) {
        redwoodApiManager.createNote(
            courseId = courseId,
            objectId = objectId,
            objectType = objectType,
            userText = userText,
            notebookType = notebookType,
            highlightData = highlightData,
        )
    }

    suspend fun updateNote(
        id: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData?,
    ) {
        redwoodApiManager.updateNote(
            id = id,
            userText = userText,
            notebookType = notebookType,
            highlightData = highlightData,
        )
    }

    private fun buildFilter(
        filterType: NotebookType?,
        courseId: Long?,
        objectTypeAndId: Pair<String, String>?,
    ): NoteFilterInput = NoteFilterInput(
        reactions = if (filterType != null) Optional.present(listOf(filterType.name)) else Optional.absent(),
        courseId = Optional.presentIfNotNull(courseId?.toString()),
        learningObject = if (objectTypeAndId != null) {
            Optional.present(LearningObjectFilter(type = objectTypeAndId.first, id = objectTypeAndId.second))
        } else {
            Optional.absent()
        },
    )

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val SYNC_PAGE_SIZE = 100
    }
}

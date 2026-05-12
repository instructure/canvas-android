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
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.horizon.data.datasource.NotebookLocalDataSource
import com.instructure.horizon.data.datasource.NotebookNetworkDataSource
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.common.model.mapToNotes
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.redwood.type.OrderDirection
import javax.inject.Inject

data class NotebookPage(
    val notes: List<Note>,
    val hasNextPage: Boolean,
    val endCursor: String?,
)

class NotebookRepository @Inject constructor(
    private val networkDataSource: NotebookNetworkDataSource,
    private val localDataSource: NotebookLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getNotes(
        after: String? = null,
        before: String? = null,
        itemCount: Int = NotebookNetworkDataSource.DEFAULT_PAGE_SIZE,
        filterType: NotebookType? = null,
        courseId: Long? = null,
        objectTypeAndId: Pair<String, String>? = null,
        orderDirection: OrderDirection? = null,
        forceNetwork: Boolean = false,
    ): NotebookPage {
        return if (shouldFetchFromNetwork()) {
            val response = networkDataSource.getNotes(
                after = after,
                before = before,
                itemCount = itemCount,
                filterType = filterType,
                courseId = courseId,
                objectTypeAndId = objectTypeAndId,
                orderDirection = orderDirection,
                forceNetwork = forceNetwork,
            )
            if (shouldSync()) {
                val entities = response.edges.orEmpty().map { NotebookLocalDataSource.toEntity(it) }
                localDataSource.upsertNotes(entities)
            }
            NotebookPage(
                notes = response.mapToNotes(),
                hasNextPage = response.pageInfo.hasNextPage,
                endCursor = response.pageInfo.endCursor,
            )
        } else {
            val offset = NotebookLocalDataSource.decodeOfflineCursor(after)
            val page = localDataSource.getNotes(
                courseId = courseId,
                filterType = filterType,
                objectTypeAndId = objectTypeAndId,
                orderDirection = orderDirection,
                offset = offset,
                limit = itemCount,
            )
            NotebookPage(
                notes = page.notes,
                hasNextPage = page.hasNextPage,
                endCursor = if (page.hasNextPage) NotebookLocalDataSource.encodeOfflineCursor(page.nextOffset) else null,
            )
        }
    }

    suspend fun getCourses(forceNetwork: Boolean = false): List<CourseWithProgress> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getCourses(forceNetwork)
        } else {
            localDataSource.getCourses()
        }
    }

    suspend fun deleteNote(noteId: String) {
        if (shouldFetchFromNetwork()) {
            networkDataSource.deleteNote(noteId)
        }
        localDataSource.deleteNote(noteId)
    }
}

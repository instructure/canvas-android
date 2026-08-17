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
package com.instructure.canvasapi2.managers.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.StudyNotesForPageQuery
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.models.mapToStudyNote

interface StudyNoteManager {

    suspend fun getPageStudyNotes(courseId: Long, pageId: String, forceNetwork: Boolean = false): List<StudyNote>
}

class StudyNoteManagerImpl(private val apolloClient: ApolloClient) : StudyNoteManager {

    override suspend fun getPageStudyNotes(courseId: Long, pageId: String, forceNetwork: Boolean): List<StudyNote> {
        val notes = mutableListOf<StudyNote>()
        var cursor: Optional<String?> = Optional.absent()
        var hasNextPage = true

        while (hasNextPage) {
            val query = StudyNotesForPageQuery(
                courseId = courseId.toString(),
                pageId = pageId,
                pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE,
                after = cursor
            )

            val connection = apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors.studyNotesConnection

            notes += connection?.nodes.orEmpty().mapNotNull { it?.mapToStudyNote() }

            hasNextPage = connection?.pageInfo?.hasNextPage == true
            cursor = Optional.presentIfNotNull(connection?.pageInfo?.endCursor)
        }

        return notes
    }
}

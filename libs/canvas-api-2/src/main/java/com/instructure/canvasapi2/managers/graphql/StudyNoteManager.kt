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
import com.instructure.canvasapi2.StudyNotesForCourseQuery
import com.instructure.canvasapi2.StudyNotesForPageQuery
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.models.CourseTaggedStudyNote
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.models.mapToStudyNote
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface StudyNoteManager {

    suspend fun getPageStudyNotes(courseId: Long, pageId: String, forceNetwork: Boolean = false): List<StudyNote>

    /** Every active note the current user has in [courseId], regardless of which page/assignment/quiz it's on. */
    suspend fun getCourseStudyNotes(courseId: Long, forceNetwork: Boolean = false): List<StudyNote>

    /**
     * Every active note the current user has across all of [courseIds], tagged with the id of the course
     * each one came from. There is no single-call, courseId-less backend field for this today (see the
     * Notebook Course/Global levels plan) - this is a client-side fan-out of one [getCourseStudyNotes]
     * call per course, run concurrently. A course whose call fails (e.g. the user lost access, a
     * transient network error) is dropped rather than failing the whole fan-out - see the impl.
     */
    suspend fun getGlobalStudyNotes(courseIds: List<Long>, forceNetwork: Boolean = false): List<CourseTaggedStudyNote>
}

class StudyNoteManagerImpl(private val apolloClient: ApolloClient) : StudyNoteManager {

    override suspend fun getPageStudyNotes(courseId: Long, pageId: String, forceNetwork: Boolean): List<StudyNote> =
        paginate { cursor ->
            val query = StudyNotesForPageQuery(
                courseId = courseId.toString(),
                pageId = pageId,
                pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE,
                after = cursor
            )
            val connection = apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors.studyNotesConnection
            PageResult(
                notes = connection?.nodes.orEmpty().mapNotNull { it?.mapToStudyNote() },
                hasNextPage = connection?.pageInfo?.hasNextPage == true,
                endCursor = connection?.pageInfo?.endCursor
            )
        }

    override suspend fun getCourseStudyNotes(courseId: Long, forceNetwork: Boolean): List<StudyNote> =
        paginate { cursor ->
            val query = StudyNotesForCourseQuery(
                courseId = courseId.toString(),
                pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE,
                after = cursor
            )
            val connection = apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors.studyNotesConnection
            PageResult(
                notes = connection?.nodes.orEmpty().mapNotNull { it?.mapToStudyNote() },
                hasNextPage = connection?.pageInfo?.hasNextPage == true,
                endCursor = connection?.pageInfo?.endCursor
            )
        }

    /** One page of a cursor-paginated connection - shared shape for [getPageStudyNotes]'s and
     * [getCourseStudyNotes]'s otherwise-identical paging loops (see [paginate]). */
    private data class PageResult(val notes: List<StudyNote>, val hasNextPage: Boolean, val endCursor: String?)

    /**
     * Drives a cursor-paginated connection to completion, accumulating every page's notes.
     * Guards on [PageResult.endCursor] being non-null in addition to [PageResult.hasNextPage]: a
     * server response that (incorrectly) reports `hasNextPage = true` with a null cursor would
     * otherwise re-request the same first page forever.
     */
    private suspend fun paginate(fetchPage: suspend (cursor: Optional<String?>) -> PageResult): List<StudyNote> {
        val notes = mutableListOf<StudyNote>()
        var cursor: Optional<String?> = Optional.absent()
        var hasNextPage = true

        while (hasNextPage) {
            val page = fetchPage(cursor)
            notes += page.notes
            hasNextPage = page.hasNextPage && page.endCursor != null
            cursor = Optional.presentIfNotNull(page.endCursor)
        }

        return notes
    }

    override suspend fun getGlobalStudyNotes(courseIds: List<Long>, forceNetwork: Boolean): List<CourseTaggedStudyNote> =
        coroutineScope {
            // Bounded to MAX_CONCURRENT_COURSE_FETCHES concurrent requests at a time - an unbounded
            // per-course fan-out would fire one request per enrolled course (30-40+ for a long-lived
            // account) on every open, since forceNetwork is intentionally always true here (notes can
            // change on the web, so this fan-out must not read from the Apollo cache).
            courseIds
                .chunked(MAX_CONCURRENT_COURSE_FETCHES)
                .flatMap { chunk ->
                    chunk
                        .map { courseId ->
                            async {
                                // coroutineScope + async + awaitAll fails entirely if any one child throws -
                                // catch per course so a single failing course (lost access, transient error)
                                // is dropped instead of corrupting every other course's already-fetched
                                // notes. CancellationException must propagate, not be swallowed, so this
                                // fan-out stays cancellable (e.g. the tray dismissing mid-fetch).
                                try {
                                    getCourseStudyNotes(courseId, forceNetwork).map { CourseTaggedStudyNote(courseId, it) }
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            }
                        }
                        .awaitAll()
                        .flatten()
                }
        }

    companion object {
        private const val MAX_CONCURRENT_COURSE_FETCHES = 6
    }
}

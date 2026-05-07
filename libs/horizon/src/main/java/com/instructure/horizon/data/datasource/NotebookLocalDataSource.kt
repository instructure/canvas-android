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

import com.google.gson.Gson
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteObjectType
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteReaction
import com.instructure.horizon.database.dao.HorizonDashboardEnrollmentDao
import com.instructure.horizon.database.dao.HorizonNoteDao
import com.instructure.horizon.database.entity.HorizonDashboardEnrollmentEntity
import com.instructure.horizon.database.entity.HorizonNoteEntity
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.pandautils.utils.toJson
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.OrderDirection
import java.util.Date
import javax.inject.Inject

data class LocalNotesPage(
    val notes: List<Note>,
    val hasNextPage: Boolean,
    val nextOffset: Int,
)

class NotebookLocalDataSource @Inject constructor(
    private val noteDao: HorizonNoteDao,
    private val dashboardEnrollmentDao: HorizonDashboardEnrollmentDao,
) {
    suspend fun replaceNotesForCourse(courseId: Long, notes: List<HorizonNoteEntity>) {
        noteDao.replaceForCourse(courseId, notes)
    }

    suspend fun upsertNotes(notes: List<HorizonNoteEntity>) {
        if (notes.isNotEmpty()) noteDao.upsertAll(notes)
    }

    suspend fun deleteNote(noteId: String) {
        noteDao.deleteById(noteId)
    }

    suspend fun deleteNotesByCourseId(courseId: Long) {
        noteDao.deleteByCourseId(courseId)
    }

    suspend fun getNotes(
        courseId: Long?,
        filterType: NotebookType?,
        objectTypeAndId: Pair<String, String>?,
        orderDirection: OrderDirection?,
        offset: Int,
        limit: Int,
    ): LocalNotesPage {
        val ascending = orderDirection == OrderDirection.ascending
        val objectType = objectTypeAndId?.first
        val objectId = objectTypeAndId?.second
        val reaction = filterType?.name

        val rows = noteDao.query(
            courseId = courseId,
            objectType = objectType,
            objectId = objectId,
            reaction = reaction,
            ascending = ascending,
            limit = limit,
            offset = offset,
        )
        val total = noteDao.count(
            courseId = courseId,
            objectType = objectType,
            objectId = objectId,
            reaction = reaction,
        )
        val nextOffset = offset + rows.size
        return LocalNotesPage(
            notes = rows.map { it.toNote() },
            hasNextPage = nextOffset < total,
            nextOffset = nextOffset,
        )
    }

    suspend fun getCourses(): List<CourseWithProgress> {
        return dashboardEnrollmentDao.getAll().map { it.toCourseWithProgress() }
    }

    private fun HorizonNoteEntity.toNote(): Note = Note(
        id = id,
        courseId = courseId,
        objectId = objectId,
        objectType = NoteObjectType.fromValue(objectType) ?: NoteObjectType.PAGE,
        userText = userText,
        highlightedText = parseHighlightedData(highlightedDataJson),
        type = runCatching { NotebookType.valueOf(reaction) }.getOrDefault(NotebookType.Important),
        updatedAt = Date(updatedAt),
    )

    private fun HorizonDashboardEnrollmentEntity.toCourseWithProgress() = CourseWithProgress(
        courseId = courseId,
        courseName = courseName,
        courseImageUrl = courseImageUrl,
        courseSyllabus = courseSyllabus,
        progress = completionPercentage,
    )

    companion object {
        const val OFFLINE_CURSOR_PREFIX = "offline:"

        fun encodeOfflineCursor(offset: Int): String = "$OFFLINE_CURSOR_PREFIX$offset"

        fun decodeOfflineCursor(cursor: String?): Int {
            if (cursor == null) return 0
            return cursor.removePrefix(OFFLINE_CURSOR_PREFIX).toIntOrNull() ?: 0
        }

        fun parseHighlightedData(json: String): NoteHighlightedData = try {
            Gson().fromJson(json, NoteHighlightedData::class.java) ?: emptyHighlight()
        } catch (e: Exception) {
            emptyHighlight()
        }

        fun toEntity(edge: QueryNotesQuery.Edge): HorizonNoteEntity {
            val node = edge.node
            return HorizonNoteEntity(
                id = node.id,
                courseId = node.courseId.toLongOrNull() ?: 0L,
                objectId = node.objectId,
                objectType = node.objectType,
                userText = node.userText.orEmpty(),
                reaction = when (node.reaction?.firstOrNull()) {
                    NoteReaction.Confusing.value -> NotebookType.Confusing.name
                    else -> NotebookType.Important.name
                },
                highlightedDataJson = node.highlightData?.toJson() ?: "",
                updatedAt = node.updatedAt.time,
            )
        }

        private fun emptyHighlight() = NoteHighlightedData(
            selectedText = "",
            range = NoteHighlightedDataRange(0, 0, "", ""),
            textPosition = NoteHighlightedDataTextPosition(0, 0),
        )
    }
}

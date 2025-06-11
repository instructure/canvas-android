package com.instructure.horizon.features.notebook.common.model

import com.google.gson.Gson
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.NoteObjectType
import com.instructure.canvasapi2.managers.NoteReaction
import com.instructure.pandautils.utils.toJson
import com.instructure.redwood.QueryNotesQuery
import java.util.Date

data class Note(
    val id: String,
    val highlightedText: NoteHighlightedData,
    val type: NotebookType,
    val userText: String,
    val updatedAt: Date,
    val courseId: Long,
    val objectType: NoteObjectType,
    val objectId: String,
)

fun QueryNotesQuery.Notes.mapToNotes(): List<Note> {
    val notes = this.edges?.map { edge ->
        val note = edge.node

        Note(
            id = note.id,
            courseId = note.courseId.toLong(),
            objectId = note.objectId,
            objectType = NoteObjectType.fromValue(note.objectType)!!,
            userText = note.userText.orEmpty(),
            highlightedText = try {
                Gson().fromJson(note.highlightData?.toJson(), NoteHighlightedData::class.java)
            } catch (e: Exception) {
                NoteHighlightedData(selectedText = "", range = NoteHighlightedDataTextPosition(0, 0, "", ""))
            },
            updatedAt = note.updatedAt,
            type = when (note.reaction?.firstOrNull()) {
                NoteReaction.Important.value -> NotebookType.Important
                NoteReaction.Confusing.value -> NotebookType.Confusing
                else -> NotebookType.Important
            },
        )
    }.orEmpty()

    return notes
}
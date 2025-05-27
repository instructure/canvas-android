package com.instructure.horizon.features.notebook.common.model

import com.instructure.canvasapi2.managers.NoteObjectType
import java.util.Date

data class Note(
    val id: String,
    val highlightedText: String,
    val userText: String,
    val updatedAt: Date,
    val courseId: Long,
    val objectType: NoteObjectType,
    val objectId: String,
)
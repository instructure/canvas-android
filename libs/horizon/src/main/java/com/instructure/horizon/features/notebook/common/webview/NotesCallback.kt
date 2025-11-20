package com.instructure.horizon.features.notebook.common.webview

import java.util.Date

data class NotesCallback(
    val onNoteSelected: (
        noteId: String,
        noteType: String,
        selectedText: String,
        userComment: String,
        startContainer: String,
        startOffset: Int,
        endContainer: String,
        endOffset: Int,
        textSelectionStart: Int,
        textSelectionEnd: Int,
        updatedAt: String,
    ) -> Unit,
    val onNoteAdded: (
        selectedText: String,
        noteType: String?,
        startContainer: String,
        startOffset: Int,
        endContainer: String,
        endOffset: Int,
        textSelectionStart: Int,
        textSelectionEnd: Int
    ) -> Unit,
)

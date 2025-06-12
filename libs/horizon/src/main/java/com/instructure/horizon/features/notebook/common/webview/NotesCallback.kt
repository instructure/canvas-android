package com.instructure.horizon.features.notebook.common.webview

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
    ) -> Unit,
    val onNoteAdded: (
        selectedText: String,
        startContainer: String,
        startOffset: Int,
        endContainer: String,
        endOffset: Int,
    ) -> Unit,
)

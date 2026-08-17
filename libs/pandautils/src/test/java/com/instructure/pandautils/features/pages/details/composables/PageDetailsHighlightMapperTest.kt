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
package com.instructure.pandautils.features.pages.details.composables

import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.models.StudyNoteHighlightData
import com.instructure.canvasapi2.models.StudyNoteHighlightRange
import com.instructure.canvasapi2.models.StudyNoteHighlightTextPosition
import com.instructure.pandautils.features.notes.webview.toWebViewHighlight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class PageDetailsHighlightMapperTest {

    @Test
    fun `maps a fully populated StudyNote to a WebViewHighlight`() {
        val note = studyNote(
            id = "1",
            userText = "My comment",
            reactions = listOf("Important"),
            highlightData = StudyNoteHighlightData(
                selectedText = "Selected text",
                range = StudyNoteHighlightRange(startOffset = 1, endOffset = 5, startContainer = "/p[1]", endContainer = "/p[2]"),
                textPosition = StudyNoteHighlightTextPosition(start = 10, end = 20),
                pageLastModifiedAt = "2026-01-01"
            ),
            updatedAt = null
        )

        val highlight = note.toWebViewHighlight()

        assertEquals("1", highlight?.id)
        assertEquals("Selected text", highlight?.selectedText)
        assertEquals("My comment", highlight?.userText)
        assertEquals("Important", highlight?.reactionLabel)
        assertEquals(1, highlight?.range?.startOffset)
        assertEquals(5, highlight?.range?.endOffset)
        assertEquals("/p[1]", highlight?.range?.startContainer)
        assertEquals("/p[2]", highlight?.range?.endContainer)
        assertEquals(10, highlight?.textPosition?.start)
        assertEquals(20, highlight?.textPosition?.end)
    }

    @Test
    fun `returns null when highlightData is null`() {
        val note = studyNote(highlightData = null)

        assertNull(note.toWebViewHighlight())
    }

    @Test
    fun `returns null when range is null - a valid but non-anchorable note, for example a selection spanning too many elements for the anchoring library to compute a DOM range for`() {
        val note = studyNote(
            highlightData = StudyNoteHighlightData(
                selectedText = "Selected text",
                range = null,
                textPosition = StudyNoteHighlightTextPosition(start = 10, end = 20),
                pageLastModifiedAt = "2026-01-01"
            )
        )

        assertNull(note.toWebViewHighlight())
    }

    @Test
    fun `returns null when textPosition is null`() {
        val note = studyNote(
            highlightData = StudyNoteHighlightData(
                selectedText = "Selected text",
                range = StudyNoteHighlightRange(startOffset = 1, endOffset = 5, startContainer = "/p[1]", endContainer = "/p[2]"),
                textPosition = null,
                pageLastModifiedAt = "2026-01-01"
            )
        )

        assertNull(note.toWebViewHighlight())
    }

    @Test
    fun `falls back to an empty reaction label when there are no reactions`() {
        val note = studyNote(reactions = emptyList())

        assertEquals("", note.toWebViewHighlight()?.reactionLabel)
    }

    private fun studyNote(
        id: String = "1",
        userText: String = "",
        reactions: List<String> = listOf("Confusing"),
        highlightData: StudyNoteHighlightData? = StudyNoteHighlightData(
            selectedText = "Selected text",
            range = StudyNoteHighlightRange(startOffset = 1, endOffset = 5, startContainer = "/p[1]", endContainer = "/p[2]"),
            textPosition = StudyNoteHighlightTextPosition(start = 10, end = 20),
            pageLastModifiedAt = "2026-01-01"
        ),
        // null by default: Date.toNoteLocalisedDateFormat() calls into android.text.format.DateFormat,
        // which isn't available in a plain (non-Robolectric) JVM unit test.
        updatedAt: Date? = null
    ) = StudyNote(
        id = id,
        userText = userText,
        reactions = reactions,
        highlightData = highlightData,
        learningObjectId = "1423",
        learningObjectType = LearningObjectType.WikiPage,
        createdAt = Date(),
        updatedAt = updatedAt
    )
}

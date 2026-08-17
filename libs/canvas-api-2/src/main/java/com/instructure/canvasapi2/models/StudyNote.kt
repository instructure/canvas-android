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
package com.instructure.canvasapi2.models

import com.google.gson.Gson
import com.instructure.canvasapi2.StudyNotesForPageQuery
import java.util.Date

data class StudyNote(
    val id: String,
    val userText: String,
    val reactions: List<String>,
    val highlightData: StudyNoteHighlightData?,
    val learningObjectId: String,
    val learningObjectType: LearningObjectType,
    val createdAt: Date?,
    val updatedAt: Date?
)

enum class LearningObjectType { WikiPage, Assignment, Quiz }

data class StudyNoteHighlightData(
    val selectedText: String,
    val range: StudyNoteHighlightRange?,
    val textPosition: StudyNoteHighlightTextPosition?,
    val pageLastModifiedAt: String?
)

data class StudyNoteHighlightRange(
    val startOffset: Int,
    val endOffset: Int,
    val startContainer: String,
    val endContainer: String
)

data class StudyNoteHighlightTextPosition(
    val start: Int,
    val end: Int
)

private val gson = Gson()

fun StudyNotesForPageQuery.Node.mapToStudyNote(): StudyNote {
    return StudyNote(
        id = _id,
        userText = userText.orEmpty(),
        reactions = reactions,
        highlightData = highlightData.toStudyNoteHighlightData(),
        learningObjectId = learningObjectId,
        learningObjectType = learningObjectType.rawValue.toLearningObjectType(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun String.toLearningObjectType(): LearningObjectType {
    return LearningObjectType.entries.find { it.name == this } ?: LearningObjectType.WikiPage
}

private fun Any?.toStudyNoteHighlightData(): StudyNoteHighlightData? {
    this ?: return null

    return try {
        gson.fromJson(gson.toJsonTree(this), StudyNoteHighlightData::class.java)
    } catch (e: Exception) {
        null
    }
}

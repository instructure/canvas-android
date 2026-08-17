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

/**
 * Typed view of [StudyNote.reactions], whose wire format is an untyped list of free-form strings.
 * Today the backend only ever emits "Important" / "Confusing"; anything else maps to [OTHER].
 * [apiValue] is the single source of truth for those wire strings - do not re-literal them anywhere
 * (including inside injected JavaScript, see JSTextSelectionInterface). Note: the user-facing label
 * for CONFUSING is "Unclear", deliberately different from the wire value - see NoteTypeUi.kt.
 */
enum class NoteType(val apiValue: String) {
    IMPORTANT("Important"),
    CONFUSING("Confusing"),
    OTHER("");

    companion object {
        fun fromApiValue(value: String?): NoteType =
            entries.firstOrNull { value != null && it.apiValue.equals(value, ignoreCase = true) } ?: OTHER

        /** A note carries at most one meaningful reaction today; extra entries are ignored. */
        fun fromReactions(reactions: List<String>): NoteType = fromApiValue(reactions.firstOrNull())
    }
}

val StudyNote.noteType: NoteType get() = NoteType.fromReactions(reactions)

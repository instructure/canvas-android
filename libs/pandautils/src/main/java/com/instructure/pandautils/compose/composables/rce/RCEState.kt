/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables.rce

data class RCEState(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val numberedList: Boolean = false,
    val bulletedList: Boolean = false,
    val colorPicker: Boolean = false,
)

enum class RCEAction {
    UNDO,
    REDO,
    BOLD,
    ITALIC,
    UNDERLINE,
    NUMBERED_LIST,
    BULLETED_LIST,
    COLOR_PICKER,
    INSERT_IMAGE,
    INSERT_LINK
}

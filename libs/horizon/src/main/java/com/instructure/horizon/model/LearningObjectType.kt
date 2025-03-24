/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.instructure.horizon.R

enum class LearningObjectType(@StringRes val stringRes: Int, @DrawableRes val iconRes: Int, val apiString: String) {
    ASSIGNMENT(R.string.learningObject_assignment, R.drawable.edit_document_assignment, "Assignment"),
    PAGE(R.string.learningObject_page, R.drawable.text_snippet, "Page"),
    FILE(R.string.learningObject_File, R.drawable.attach_file, "File"),
    EXTERNAL_TOOL(R.string.learningObject_externalTool, R.drawable.note_alt, "ExternalTool"),
    EXTERNAL_URL(R.string.learningObject_externalLink, R.drawable.link, "ExternalUrl"),
    ASSESSMENT(R.string.learningObject_assessment, R.drawable.fact_check, "Quiz"), // TODO Double check this
    UNKNOWN(R.string.learningObject_page, R.drawable.text_snippet, "");

    companion object {
        fun fromApiString(apiString: String): LearningObjectType {
            return entries.firstOrNull { it.apiString == apiString } ?: UNKNOWN
        }
    }
}